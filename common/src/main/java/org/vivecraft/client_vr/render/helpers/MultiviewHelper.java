package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MVTextureTarget;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiviewHelper {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    public static final String UNIFORM_EYE_PROJ = "VivecraftEyeProj1";
    public static final String UNIFORM_EYE_VIEW = "VivecraftEyeView1";

    public static boolean COMPILING = false;
    public static boolean ACTIVE = false;

    public static final Set<Program> PROGRAMS = Collections.newSetFromMap(new IdentityHashMap<>());

    private static final Map<ShaderInstance, ShaderInstance> SHADERS = new IdentityHashMap<>();
    private static final Set<ShaderInstance> MV_SHADERS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final List<int[]> UNIFORM_LOCATIONS = new ArrayList<>();
    private static final List<int[]> EXTERNAL_UNIFORM_LOCATIONS = new ArrayList<>();

    private static final String HEADER = """

        #extension GL_OVR_multiview2 : require
        layout(num_views = 2) in;
        uniform mat4 %1$s;
        uniform mat4 %2$s;
        #define EYE_PROJ(P) (gl_ViewID_OVR == 1u ? %1$s * (P) : (P))
        #define EYE_VIEW (gl_ViewID_OVR == 1u ? %2$s : mat4(1.0))
        """.formatted(UNIFORM_EYE_PROJ, UNIFORM_EYE_VIEW);
    private static final Pattern VERSION = Pattern.compile("^#version\\s+\\d+.*$", Pattern.MULTILINE);
    private static boolean shadersBuilt = false;
    private static boolean failed = false;

    private static final Matrix4f UPLOADED_PROJ = new Matrix4f();
    private static final Matrix4f UPLOADED_VIEW = new Matrix4f();
    private static boolean uniformsUploaded = false;

    private static MVTextureTarget target;
    private static int readFramebuffer = -1;

    public static boolean shouldUseMultiview() {
        if (!DATA_HOLDER.vrSettings.enableMultiview || failed) return false;
        if (!GL.getCapabilities().GL_OVR_multiview2) return false;
        if (ShadersHelper.isShaderActive()) return false;
        if (Minecraft.useShaderTransparency()) return false;
        if (DATA_HOLDER.vrSettings.useFsaa) return false;
        if (VRShaders.POST_PROCESSING_MV_SHADER == null) return false;
        if (DATA_HOLDER.vrRenderer == null || DATA_HOLDER.vrRenderer.framebufferVrRender == null) return false;
        if (!shadersBuilt) {
            buildShaders();
        }
        return !failed;
    }

    public static ShaderInstance swap(ShaderInstance shader) {
        if (!ACTIVE || shader == null || MV_SHADERS.contains(shader)) return shader;
        ShaderInstance mv = SHADERS.get(shader);
        if (mv == null) {
            VRSettings.LOGGER.error("Vivecraft: no MultiView variant for shader '{}', disabling MultiView",
                shader.getName());
            failed = true;
            return shader;
        }
        return mv;
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
            throw new IllegalStateException("Unrecognized shader for MultiView, try without it!\n" + src);
        }

        Matcher version = VERSION.matcher(out);
        if (version.find()) {
            return out.substring(0, version.end()) + HEADER + out.substring(version.end());
        }
        return HEADER + out;
    }

    public static void registerProgram(int program) {
        int[] locations = new int[]{
            program,
            GL20.glGetUniformLocation(program, UNIFORM_EYE_PROJ),
            GL20.glGetUniformLocation(program, UNIFORM_EYE_VIEW)
        };
        EXTERNAL_UNIFORM_LOCATIONS.add(locations);
        if (uniformsUploaded) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                upload(locations, UPLOADED_PROJ.get(stack.mallocFloat(16)),
                    UPLOADED_VIEW.get(stack.mallocFloat(16)));
            }
            ProgramManager.glUseProgram(0);
        }
    }

    public static void unregisterProgram(int program) {
        EXTERNAL_UNIFORM_LOCATIONS.removeIf(locations -> locations[0] == program);
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

    private static void buildShaders() {
        shadersBuilt = true;
        List<ShaderInstance> sources = new ArrayList<>(
            ((GameRendererExtension) MC.gameRenderer).vivecraft$getShaders().values());
        sources.add(VRShaders.RENDERTYPE_END_PORTAL_VR_SHADER);
        sources.add(VRShaders.RENDERTYPE_END_GATEWAY_VR_SHADER);

        COMPILING = true;
        try {
            for (ShaderInstance shader : sources) {
                if (shader == null || SHADERS.containsKey(shader)) continue;
                ShaderInstance mv = new ShaderInstance(MC.getResourceManager(), shader.getName(),
                    shader.getVertexFormat());
                SHADERS.put(shader, mv);
                MV_SHADERS.add(mv);
                if (GL20.glGetProgrami(mv.getId(), GL20.GL_LINK_STATUS) == 0) {
                    throw new IllegalStateException(
                        "Couldn't link MultiView shader '" + shader.getName() + "': " +
                            GL20.glGetProgramInfoLog(mv.getId()).trim());
                }
                UNIFORM_LOCATIONS.add(new int[]{
                    mv.getId(),
                    GL20.glGetUniformLocation(mv.getId(), UNIFORM_EYE_PROJ),
                    GL20.glGetUniformLocation(mv.getId(), UNIFORM_EYE_VIEW)
                });
            }
            VRSettings.LOGGER.info("Vivecraft: compiled {} MultiView shaders", SHADERS.size());
        } catch (Exception e) {
            VRSettings.LOGGER.error("Vivecraft: failed to compile MultiView shaders, disabling MultiView", e);
            failed = true;
        } finally {
            COMPILING = false;
        }
        if (failed) {
            closeShaders();
        }
    }

    private static void closeShaders() {
        for (ShaderInstance shader : SHADERS.values()) {
            shader.close();
        }
        SHADERS.clear();
        MV_SHADERS.clear();
        UNIFORM_LOCATIONS.clear();
        uniformsUploaded = false;
    }

    public static void clearShaders() {
        closeShaders();
        shadersBuilt = false;
        failed = false;
    }

    public static void destroy() {
        clearShaders();
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
        if (readFramebuffer != -1) {
            GlStateManager._glDeleteFramebuffers(readFramebuffer);
            readFramebuffer = -1;
        }
    }

    public static MVTextureTarget getTarget() {
        RenderTarget source = DATA_HOLDER.vrRenderer.framebufferVrRender;
        if (target == null || target.width != source.width || target.height != source.height) {
            if (target != null) {
                target.destroyBuffers();
            }
            target = new MVTextureTarget("MultiView 3D Render", source.width, source.height,
                GL30.GL_DEPTH_COMPONENT32F);
            VRSettings.LOGGER.info("Vivecraft: {}", target);
            RenderHelper.checkGLError("MultiView framebuffer setup");
        }
        return target;
    }

    public static void uploadEyeUniforms() {
        Matrix4f leftProj = DATA_HOLDER.vrRenderer.eyeProj[0];
        Matrix4f rightProj = DATA_HOLDER.vrRenderer.eyeProj[1];
        Matrix4f projFix = leftProj != null && rightProj != null ?
            new Matrix4f(rightProj).mul(new Matrix4f(leftProj).invert()) : new Matrix4f();

        VRData data = DATA_HOLDER.vrPlayer.getVRDataWorld();
        VRData.VRDevicePose left = data.getEye(RenderPass.LEFT);
        VRData.VRDevicePose right = data.getEye(RenderPass.RIGHT);
        Vec3 offset = left.getPosition().subtract(right.getPosition());
        Matrix4f eyeView = right.getMatrix().invert()
            .translate((float) offset.x, (float) offset.y, (float) offset.z)
            .mul(left.getMatrix());

        if (uniformsUploaded && projFix.equals(UPLOADED_PROJ, 1.0E-5F) && eyeView.equals(UPLOADED_VIEW, 1.0E-5F)) {
            return;
        }
        UPLOADED_PROJ.set(projFix);
        UPLOADED_VIEW.set(eyeView);
        uniformsUploaded = true;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer projBuffer = projFix.get(stack.mallocFloat(16));
            FloatBuffer viewBuffer = eyeView.get(stack.mallocFloat(16));
            for (int[] locations : UNIFORM_LOCATIONS) {
                upload(locations, projBuffer, viewBuffer);
            }
            for (int[] locations : EXTERNAL_UNIFORM_LOCATIONS) {
                upload(locations, projBuffer, viewBuffer);
            }
            ProgramManager.glUseProgram(0);
        }
    }

    private static void upload(int[] locations, FloatBuffer proj, FloatBuffer view) {
        ProgramManager.glUseProgram(locations[0]);
        if (locations[1] != -1) {
            GL20.glUniformMatrix4fv(locations[1], false, proj);
        }
        if (locations[2] != -1) {
            GL20.glUniformMatrix4fv(locations[2], false, view);
        }
    }

    public static void copyLayer(RenderPass eye) {
        RenderTarget dest = DATA_HOLDER.vrRenderer.framebufferVrRender;
        int layer = eye == RenderPass.RIGHT ? 1 : 0;
        if (readFramebuffer == -1) {
            readFramebuffer = GlStateManager.glGenFramebuffers();
        }
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
        GL30.glFramebufferTextureLayer(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
            target.getColorTextureId(), 0, layer);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, dest.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, target.width, target.height, 0, 0, dest.width, dest.height,
            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}
