package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.IOException;

public class VRShaders {
    // FSAA shader and its uniforms
    public static ShaderInstance LANCZOS_SHADER;
    public static AbstractUniform LANCZOS_TEXEL_WIDTH_OFFSET_UNIFORM;
    public static AbstractUniform LANCZOS_TEXEL_HEIGHT_OFFSET_UNIFORM;

    // mixed reality shader and its uniforms
    public static ShaderInstance MIXED_REALITY_SHADER;
    public static AbstractUniform MIXED_REALITY_HMD_VIEW_POSITION_UNIFORM;
    public static AbstractUniform MIXED_REALITY_HMD_PLANE_NORMAL_UNIFORM;
    public static AbstractUniform MIXED_REALITY_PROJECTION_MATRIX_UNIFORM;
    public static AbstractUniform MIXED_REALITY_VIEW_MATRIX_UNIFORM;
    public static AbstractUniform MIXED_REALITY_FIRST_PERSON_PASS_UNIFORM;
    public static AbstractUniform MIXED_REALITY_KEY_COLOR_UNIFORM;
    public static AbstractUniform MIXED_REALITY_ALPHA_MODE_UNIFORM;

    // vr post shader and its uniforms
    public static ShaderInstance POST_PROCESSING_SHADER;
    // same as POST_PROCESSING_SHADER, but reads a layer of a texture array
    public static ShaderInstance POST_PROCESSING_MV_SHADER;
    public static AbstractUniform POST_PROCESSING_MV_LAYER_UNIFORM;
    public static AbstractUniform POST_PROCESSING_FOV_REDUCTION_RADIUS_UNIFORM;
    public static AbstractUniform POST_PROCESSING_FOV_REDUCTION_OFFSET_UNIFORM;
    public static AbstractUniform POST_PROCESSING_FOV_REDUCTION_BORDER_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_HEALTH_ALPHA_UNiFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_FREEZE_ALPHA_UNiFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_WATER_AMPLITUDE_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_TIME_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_BLACK_ALPHA_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_EYE_UNIFORM;

    // end portal shaders
    public static ShaderInstance RENDERTYPE_END_PORTAL_VR_SHADER;
    public static ShaderInstance RENDERTYPE_END_GATEWAY_VR_SHADER;

    public static ShaderInstance getRendertypeEndPortalVrShader() {
        return RENDERTYPE_END_PORTAL_VR_SHADER;
    }

    public static ShaderInstance getRendertypeEndGatewayVrShader() {
        return RENDERTYPE_END_GATEWAY_VR_SHADER;
    }

    private VRShaders() {}

    public static void setupDepthMask() throws IOException {
        MIXED_REALITY_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "mixedreality_vr",
            DefaultVertexFormat.POSITION_TEX);

        MIXED_REALITY_HMD_VIEW_POSITION_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform("hmdViewPosition");
        MIXED_REALITY_HMD_PLANE_NORMAL_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform("hmdPlaneNormal");
        MIXED_REALITY_PROJECTION_MATRIX_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform("projectionMatrix");
        MIXED_REALITY_VIEW_MATRIX_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform("viewMatrix");
        MIXED_REALITY_FIRST_PERSON_PASS_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform("firstPersonPass");
        MIXED_REALITY_KEY_COLOR_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform("keyColor");
        MIXED_REALITY_ALPHA_MODE_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform("alphaMode");
    }

    public static void setupFSAA() throws IOException {
        LANCZOS_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "lanczos_vr",
            DefaultVertexFormat.POSITION_TEX);

        LANCZOS_TEXEL_WIDTH_OFFSET_UNIFORM = LANCZOS_SHADER.safeGetUniform("texelWidthOffset");
        LANCZOS_TEXEL_HEIGHT_OFFSET_UNIFORM = LANCZOS_SHADER.safeGetUniform("texelHeightOffset");
    }

    public static void setupFOVReduction() throws IOException {
        POST_PROCESSING_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "postprocessing_vr",
            DefaultVertexFormat.POSITION_TEX);

        try {
            POST_PROCESSING_MV_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(),
                "postprocessing_mv_vr", DefaultVertexFormat.POSITION_TEX);
            POST_PROCESSING_MV_LAYER_UNIFORM = POST_PROCESSING_MV_SHADER.safeGetUniform("layer");
        } catch (Exception e) {
            VRSettings.LOGGER.error("Vivecraft: failed to compile MultiView post processing shader", e);
            POST_PROCESSING_MV_SHADER = null;
            POST_PROCESSING_MV_LAYER_UNIFORM = null;
        }

        POST_PROCESSING_FOV_REDUCTION_RADIUS_UNIFORM = postProcessingUniform("circle_radius");
        POST_PROCESSING_FOV_REDUCTION_OFFSET_UNIFORM = postProcessingUniform("circle_offset");
        POST_PROCESSING_FOV_REDUCTION_BORDER_UNIFORM = postProcessingUniform("border");
        POST_PROCESSING_OVERLAY_HEALTH_ALPHA_UNiFORM = postProcessingUniform("redalpha");
        POST_PROCESSING_OVERLAY_FREEZE_ALPHA_UNiFORM = postProcessingUniform("bluealpha");
        POST_PROCESSING_OVERLAY_WATER_AMPLITUDE_UNIFORM = postProcessingUniform("water");
        POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE_UNIFORM = postProcessingUniform("portal");
        POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE_UNIFORM = postProcessingUniform("pumpkin");
        POST_PROCESSING_OVERLAY_EYE_UNIFORM = postProcessingUniform("eye");
        POST_PROCESSING_OVERLAY_TIME_UNIFORM = postProcessingUniform("portaltime");
        POST_PROCESSING_OVERLAY_BLACK_ALPHA_UNIFORM = postProcessingUniform("blackalpha");
    }

    private static AbstractUniform postProcessingUniform(String name) {
        if (POST_PROCESSING_MV_SHADER == null) {
            return POST_PROCESSING_SHADER.safeGetUniform(name);
        }
        return new MultiUniform(POST_PROCESSING_SHADER.safeGetUniform(name),
            POST_PROCESSING_MV_SHADER.safeGetUniform(name));
    }

    public static void setupPortalShaders() throws IOException {
        RENDERTYPE_END_PORTAL_VR_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(),
            "rendertype_end_portal_vr", DefaultVertexFormat.POSITION);
        RENDERTYPE_END_GATEWAY_VR_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(),
            "rendertype_end_gateway_vr", DefaultVertexFormat.POSITION);
    }
}
