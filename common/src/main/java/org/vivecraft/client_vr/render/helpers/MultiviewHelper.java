package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MVTextureTarget;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.GlCommandEncoderExtension;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiviewHelper {
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    public static final String UNIFORM_EYE_PROJ = "VivecraftEyeProj1";
    public static final String UNIFORM_EYE_VIEW = "VivecraftEyeView1";

    public static boolean COMPILING = false;
    public static boolean ACTIVE = false;

    private static final String HEADER = """

        #extension GL_OVR_multiview2 : require
        layout(num_views = 2) in;
        uniform mat4 %1$s;
        uniform mat4 %2$s;
        #define EYE_PROJ(P) (gl_ViewID_OVR == 1u ? %1$s * (P) : (P))
        #define EYE_VIEW (gl_ViewID_OVR == 1u ? %2$s : mat4(1.0))
        """.formatted(UNIFORM_EYE_PROJ, UNIFORM_EYE_VIEW);
    private static final Pattern VERSION = Pattern.compile("^#version\\s+\\d+.*$", Pattern.MULTILINE);

    private static boolean failed = false;

    // right eye projection relative to the left, and right eye view relative to the left
    private static final Matrix4f EYE_PROJ = new Matrix4f();
    private static final Matrix4f EYE_VIEW = new Matrix4f();

    private static final Int2ObjectMap<int[]> PROGRAMS = new Int2ObjectOpenHashMap<>();

    private static MVTextureTarget target;

    public static boolean shouldUseMultiview() {
        if (!DATA_HOLDER.vrSettings.enableMultiview || failed) return false;
        if (!GL.getCapabilities().GL_OVR_multiview2) return false;
        if (ShadersHelper.isShaderActive()) return false;
        // fabulous copies and samples the main depth/color, which doesn't work with the array textures
        if (Minecraft.useShaderTransparency()) return false;
        return DATA_HOLDER.vrRenderer != null && DATA_HOLDER.vrRenderer.framebufferVrRender != null &&
            DATA_HOLDER.vrRenderer.eyeProj[0] != null && DATA_HOLDER.vrRenderer.eyeProj[1] != null;
    }

    public static String stripComments(String src) {
        return src.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("//[^\\n]*", "");
    }

    public static boolean isVertexShader(String src) {
        return stripComments(src).contains("gl_Position");
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

        Matcher version = VERSION.matcher(out);
        if (version.find()) {
            return out.substring(0, version.end()) + HEADER + out.substring(version.end());
        }
        return HEADER + out;
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
        ACTIVE = false;
    }

    public static MVTextureTarget getTarget() {
        RenderTarget source = DATA_HOLDER.vrRenderer.framebufferVrRender;
        if (target == null || target.width != source.width || target.height != source.height) {
            if (target != null) {
                target.destroyBuffers();
            }
            target = new MVTextureTarget("Multiview 3D Render", source.width, source.height);
            VRSettings.LOGGER.info("Vivecraft: {}", target);
        }
        return target;
    }

    public static Matrix4f getCullingProjection(Matrix4f projection) {
        Matrix4f right = DATA_HOLDER.vrRenderer.eyeProj[1];
        if (right == null) return projection;
        float l = Math.min((projection.m20() - 1F) / projection.m00(), (right.m20() - 1F) / right.m00());
        float r = Math.max((projection.m20() + 1F) / projection.m00(), (right.m20() + 1F) / right.m00());
        float d = Math.min((projection.m21() - 1F) / projection.m11(), (right.m21() - 1F) / right.m11());
        float u = Math.max((projection.m21() + 1F) / projection.m11(), (right.m21() + 1F) / right.m11());
        return new Matrix4f(projection)
            .m00(2F / (r - l)).m20((r + l) / (r - l))
            .m11(2F / (u - d)).m21((u + d) / (u - d));
    }

    public static void setupEyeMatrices() {
        Matrix4f leftProj = DATA_HOLDER.vrRenderer.eyeProj[0];
        Matrix4f rightProj = DATA_HOLDER.vrRenderer.eyeProj[1];
        EYE_PROJ.set(rightProj).mul(new Matrix4f(leftProj).invert());

        VRData data = DATA_HOLDER.vrPlayer.getVRDataWorld();
        VRData.VRDevicePose left = data.getEye(RenderPass.LEFT);
        VRData.VRDevicePose right = data.getEye(RenderPass.RIGHT);
        Vec3 offset = left.getPosition().subtract(right.getPosition());
        EYE_VIEW.set(right.getMatrix().invert()
            .translate((float) offset.x, (float) offset.y, (float) offset.z)
            .mul(left.getMatrix()));

        // make sure the next draw rebinds its program, so the new matrices get uploaded
        ((GlCommandEncoderExtension) RenderSystem.getDevice().createCommandEncoder()).vivecraft$resetLastProgram();
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

    public static void copyLayer(RenderPass eye) {
        target.copyLayerTo(eye == RenderPass.RIGHT ? 1 : 0, DATA_HOLDER.vrRenderer.framebufferVrRender);
    }
}
