package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MVTextureTarget;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.GlCommandEncoderExtension;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiviewHelper {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    public static final String UNIFORM_EYE_PROJ = "VivecraftEyeProj1";
    public static final String UNIFORM_EYE_VIEW = "VivecraftEyeView1";
    public static final String UNIFORM_BLOCK = "VivecraftMultiview";
    private static final int UNIFORM_BLOCK_SIZE = 2 * 16 * Float.BYTES;

    public static boolean VULKAN_MULTIVIEW_SUPPORTED = false;

    public static boolean COMPILING = false;
    public static boolean ACTIVE = false;

    private static final String GL_HEADER = """

        #extension GL_OVR_multiview2 : require
        layout(num_views = 2) in;
        uniform mat4 %1$s;
        uniform mat4 %2$s;
        #define EYE_PROJ(P) (gl_ViewID_OVR == 1u ? %1$s * (P) : (P))
        #define EYE_VIEW (gl_ViewID_OVR == 1u ? %2$s : mat4(1.0))
        """.formatted(UNIFORM_EYE_PROJ, UNIFORM_EYE_VIEW);
    private static final String VULKAN_HEADER = """

        #extension GL_EXT_multiview : require
        layout(std140) uniform %3$s {
            mat4 %1$s;
            mat4 %2$s;
        };
        #define EYE_PROJ(P) (gl_ViewIndex == 1 ? %1$s * (P) : (P))
        #define EYE_VIEW (gl_ViewIndex == 1 ? %2$s : mat4(1.0))
        """.formatted(UNIFORM_EYE_PROJ, UNIFORM_EYE_VIEW, UNIFORM_BLOCK);
    private static final Pattern VERSION = Pattern.compile("^#version\\s+\\d+.*$", Pattern.MULTILINE);

    private static boolean failed = false;

    private static final Matrix4f EYE_PROJ = new Matrix4f();
    private static final Matrix4f EYE_VIEW = new Matrix4f();

    private static final Int2ObjectMap<int[]> PROGRAMS = new Int2ObjectOpenHashMap<>();

    private static MVTextureTarget target;
    private static GpuBuffer eyeUniformBuffer;

    public static boolean shouldUseMultiview() {
        return getDisabledReason() == null;
    }

    private static String getDisabledReason() {
        if (!DATA_HOLDER.vrSettings.enableMultiview) return "setting off";
        if (failed) return "a shader failed to compile";
        if (RenderSystem.getDevice().backend instanceof GlDevice) {
            if (!GL.getCapabilities().GL_OVR_multiview2) return "GL_OVR_multiview2 not supported";
        } else if (isVulkan()) {
            if (!VULKAN_MULTIVIEW_SUPPORTED) return "vulkan multiview feature not supported";
        } else {
            return "unsupported backend";
        }
        if (ShadersHelper.isShaderActive()) return "shaderpack active";
        if (!isVulkan()) {
            if (MC.options.improvedTransparency().get()) return "improved transparency on";
            if (MC.level != null && MC.gameRenderer.gameRenderState().levelRenderState.shouldShowEntityOutlines) {
                return "entity outlines visible";
            }
        }
        if (DATA_HOLDER.vrRenderer == null || DATA_HOLDER.vrRenderer.framebufferVrRender == null ||
            DATA_HOLDER.vrRenderer.eyeReverseProj[0] == null || DATA_HOLDER.vrRenderer.eyeReverseProj[1] == null)
        {
            return "renderer not ready";
        }
        return null;
    }

    private static boolean isVulkan() {
        return RenderSystem.getDevice().backend instanceof com.mojang.blaze3d.vulkan.VulkanDevice;
    }

    public static boolean isMultiviewTexture(GpuTexture texture) {
        return texture instanceof MVGlTexture || (texture.getDepthOrLayers() == MVGlTexture.VIEWS &&
            (texture.usage() & GpuTexture.USAGE_CUBEMAP_COMPATIBLE) == 0
        );
    }

    public static boolean isMultiviewView(GpuTextureView view) {
        return view != null && isMultiviewTexture(view.texture()) && !MVTextureTarget.LAYER_VIEWS.contains(view);
    }

    public static boolean isLayeredPostActive() {
        return ACTIVE && isVulkan();
    }

    private static final java.util.Map<String, MVTextureTarget> EXTRA_TARGETS = new java.util.HashMap<>();

    public static MVTextureTarget getExtraTarget(String name) {
        MVTextureTarget main = getTarget();
        MVTextureTarget extra = EXTRA_TARGETS.get(name);
        if (extra == null || extra.width != main.width || extra.height != main.height) {
            if (extra != null) {
                extra.destroyBuffers();
            }
            extra = new MVTextureTarget("Multiview " + name, main.width, main.height, GpuFormat.RGBA8_UNORM);
            EXTRA_TARGETS.put(name, extra);
        }
        return extra;
    }

    public static String stripComments(String src) {
        return src.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("//[^\\n]*", "");
    }

    public static boolean isVertexShader(String src) {
        return stripComments(src).contains("gl_Position");
    }

    private static final String[][] MATRIX_UNIFORMS = {
        {"ProjMat", "ModelViewMat"},
        {"u_ProjectionMatrix", "u_ModelViewMatrix"}
    };

    public static String rewriteVertexShader(String src) {
        String stripped = stripComments(src);
        for (String[] names : MATRIX_UNIFORMS) {
            if (Pattern.compile("\\b" + Pattern.quote(names[0]) + "\\b").matcher(stripped).find()) {
                return rewriteVertexShader(src, names[0], names[1]);
            }
        }
        return rewriteVertexShader(src, MATRIX_UNIFORMS[0][0], MATRIX_UNIFORMS[0][1]);
    }

    public static String rewriteVertexShader(String src, String projection, String modelView) {
        String proj = Pattern.quote(projection);
        String mv = Pattern.quote(modelView);
        String out = stripComments(src);
        out = Pattern.compile("\\b" + proj + "\\s*\\*\\s*VIEW_SCALE\\s*\\*\\s*" + mv + "\\b").matcher(out)
            .replaceAll(Matcher.quoteReplacement(
                "EYE_PROJ(" + projection + ") * VIEW_SCALE * EYE_VIEW * " + modelView));
        out = Pattern.compile("\\b" + proj + "\\s*\\*\\s*" + mv + "\\b").matcher(out)
            .replaceAll(Matcher.quoteReplacement("EYE_PROJ(" + projection + ") * EYE_VIEW * " + modelView));

        if (Pattern.compile("(?<!\\bmat4\\s{1,16})(?<!EYE_PROJ\\()\\b" + proj + "\\b").matcher(out).find()) {
            throw new IllegalStateException("unrecognized use of " + projection);
        }

        String header = isVulkan() ? VULKAN_HEADER : GL_HEADER;
        Matcher version = VERSION.matcher(out);
        if (version.find()) {
            return out.substring(0, version.end()) + header + out.substring(version.end());
        }
        return header + out;
    }

    public static void registerProgram(GlProgram program) {
        registerProgram(program.getProgramId());
    }

    public static void registerProgram(int program) {
        PROGRAMS.put(program, new int[]{
            GL20.glGetUniformLocation(program, UNIFORM_EYE_PROJ),
            GL20.glGetUniformLocation(program, UNIFORM_EYE_VIEW)
        });
    }

    public static void unregisterProgram(GlProgram program) {
        unregisterProgram(program.getProgramId());
    }

    public static void unregisterProgram(int program) {
        PROGRAMS.remove(program);
    }

    public static void compileFailed(String name) {
        if (!failed) {
            VRSettings.LOGGER.error("Vivecraft: failed to compile multiview variant of '{}', disabling multiview",
                name);
        }
        failed = true;
    }

    public static void onPipelinesCleared() {
        PROGRAMS.clear();
        failed = false;
    }

    public static void destroy() {
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
        if (eyeUniformBuffer != null) {
            eyeUniformBuffer.close();
            eyeUniformBuffer = null;
        }
        EXTRA_TARGETS.values().forEach(MVTextureTarget::destroyBuffers);
        EXTRA_TARGETS.clear();
        ACTIVE = false;
    }

    public static MVTextureTarget getTarget() {
        RenderTarget source = DATA_HOLDER.vrRenderer.framebufferVrRender;
        if (target == null || target.width != source.width || target.height != source.height) {
            if (target != null) {
                target.destroyBuffers();
            }
            target = new MVTextureTarget("Multiview 3D Render", source.width, source.height, GpuFormat.RGBA8_UNORM);
            VRSettings.LOGGER.info("Vivecraft: {}", target);
        }
        return target;
    }

    public static Matrix4f getCullingProjection(Matrix4f projection) {
        Matrix4f left = DATA_HOLDER.vrRenderer.eyeReverseProj[0];
        Matrix4f right = DATA_HOLDER.vrRenderer.eyeReverseProj[1];
        if (left == null || right == null) return projection;
        float l = Math.min((left.m20() - 1F) / left.m00(), (right.m20() - 1F) / right.m00());
        float r = Math.max((left.m20() + 1F) / left.m00(), (right.m20() + 1F) / right.m00());
        float d = Math.min((left.m21() - 1F) / left.m11(), (right.m21() - 1F) / right.m11());
        float u = Math.max((left.m21() + 1F) / left.m11(), (right.m21() + 1F) / right.m11());
        return new Matrix4f(projection)
            .m00(2F / (r - l)).m20((r + l) / (r - l))
            .m11(2F / (u - d)).m21((u + d) / (u - d));
    }

    public static void setupEyeMatrices() {
        Matrix4f leftProj = DATA_HOLDER.vrRenderer.eyeReverseProj[0];
        Matrix4f rightProj = DATA_HOLDER.vrRenderer.eyeReverseProj[1];
        EYE_PROJ.set(rightProj).mul(new Matrix4f(leftProj).invert());

        VRData data = DATA_HOLDER.vrPlayer.getVRDataWorld();
        VRData.VRDevicePose left = data.getEye(RenderPass.LEFT);
        VRData.VRDevicePose right = data.getEye(RenderPass.RIGHT);
        Vec3 offset = left.getPosition().subtract(right.getPosition());
        EYE_VIEW.set(right.getMatrix().invert()
            .translate((float) offset.x, (float) offset.y, (float) offset.z)
            .mul(left.getMatrix()));

        if (isVulkan()) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer eyes = stack.malloc(UNIFORM_BLOCK_SIZE);
                EYE_PROJ.get(0, eyes);
                EYE_VIEW.get(16 * Float.BYTES, eyes);
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(getEyeUniformBuffer().slice(), eyes);
            }
        } else {
            ((GlCommandEncoderExtension) RenderSystem.getDevice().backend.createCommandEncoder())
                .vivecraft$resetLastProgram();
        }
    }

    public static GpuBuffer getEyeUniformBuffer() {
        if (eyeUniformBuffer == null || eyeUniformBuffer.isClosed()) {
            eyeUniformBuffer = RenderSystem.getDevice().createBuffer(() -> "Vivecraft Multiview Eyes",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, UNIFORM_BLOCK_SIZE);
        }
        return eyeUniformBuffer;
    }

    public static void uploadEyeUniforms(GlProgram program) {
        uploadEyeUniforms(program.getProgramId());
    }

    public static void uploadEyeUniforms(int program) {
        int[] locations = PROGRAMS.get(program);
        if (locations == null) return;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (locations[0] != -1) {
                GL20.glUniformMatrix4fv(locations[0], false, EYE_PROJ.get(stack.mallocFloat(16)));
            }
            if (locations[1] != -1) {
                GL20.glUniformMatrix4fv(locations[1], false, EYE_VIEW.get(stack.mallocFloat(16)));
            }
        }
    }

    public static @org.jspecify.annotations.Nullable RenderTarget getLayerSource(RenderPass eye) {
        if (target == null || DATA_HOLDER.vrSettings.useFsaa || DATA_HOLDER.grabScreenShot) {
            return null;
        }
        return target.getLayerTarget(eye == RenderPass.RIGHT ? 1 : 0);
    }

    public static void copyLayer(RenderPass eye) {
        target.copyLayerTo(eye == RenderPass.RIGHT ? 1 : 0, DATA_HOLDER.vrRenderer.framebufferVrRender);
    }

    public static void attachMultiview(int fbo, int colorIds, int depthId, int views) {
        attachMultiview(fbo, new int[]{colorIds}, depthId, views);
    }

    public static void attachMultiview(int fbo, int[] colorIds, int depthId, int views) {
        int previous = GlStateManager.getFrameBuffer(GL30.GL_DRAW_FRAMEBUFFER);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer drawBuffers = stack.mallocInt(Math.max(1, colorIds.length));
            for (int i = 0; i < colorIds.length; i++) {
                if (colorIds[i] != 0) {
                    OVRMultiview.glFramebufferTextureMultiviewOVR(GL30.GL_DRAW_FRAMEBUFFER,
                        GL30.GL_COLOR_ATTACHMENT0 + i, colorIds[i], 0, 0, views);
                    drawBuffers.put(i, GL30.GL_COLOR_ATTACHMENT0 + i);
                } else {
                    drawBuffers.put(i, GL11.GL_NONE);
                }
            }
            if (colorIds.length == 0) {
                drawBuffers.put(0, GL11.GL_NONE);
            }
            GL20.glDrawBuffers(drawBuffers);
        }
        if (depthId != 0) {
            OVRMultiview.glFramebufferTextureMultiviewOVR(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                depthId, 0, 0, views);
        }
        int status = GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previous);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Multiview framebuffer incomplete: 0x" + Integer.toHexString(status));
        }
    }
}
