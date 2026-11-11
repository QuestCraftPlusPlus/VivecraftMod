package org.vivecraft.client_vr.provider.nullvr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.opengl.GL11;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.settings.VRSettings;

public class NullVRStereoRenderer extends VRRenderer {
    protected int LeftEyeTextureId = -1;
    protected int RightEyeTextureId = -1;
    public VRTextureTarget framebufferEye0;
    public VRTextureTarget framebufferEye1;
    private float lastFov = -1;
    private float lastAngle = -1;

    public NullVRStereoRenderer(MCVR vr) {
        super(vr);
    }

    @Override
    public Vector2ic getRenderTextureSizes() {
        if (this.resolution == null) {
            this.resolution = new Vector2i(2048, 2048);
            VRSettings.LOGGER.info("Vivecraft: NullVR Render Res {}x{}", this.resolution.x(), this.resolution.y());
            this.ss = -1.0F;
            VRSettings.LOGGER.info("Vivecraft: NullVR Supersampling: {}", this.ss);
        }
        return this.resolution;
    }

    @Override
    public Matrix4f getCachedProjectionMatrix(int eyeType, float nearClip, float farClip) {
        if (this.lastFov != ClientDataHolderVR.getInstance().vrSettings.nullvrFOV ||
            this.lastAngle != ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle)
        {
            // reset far clip plane to force a projection fetch
            this.lastFarClip = 0F;
            this.lastFov = ClientDataHolderVR.getInstance().vrSettings.nullvrFOV;
            this.lastAngle = ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle;
        }
        return super.getCachedProjectionMatrix(eyeType, nearClip, farClip);
    }

    @Override
    protected Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        return new Matrix4f().setPerspectiveOffCenter(
            Mth.DEG_TO_RAD * ClientDataHolderVR.getInstance().vrSettings.nullvrFOV,
            Mth.DEG_TO_RAD * ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle * (eyeType == 0 ? -1F : 1F), 0F,
            1.0F, nearClip, farClip);
    }

    @Override
    public void createRenderTexture(int lwidth, int lheight) throws RenderConfigException {
        // generate eye textures
        for (int i = 0; i < 2; i++) {
            int prevTexture = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
            this.eyeTextureId[i] = GlStateManager._genTexture();
            GlStateManager._bindTexture(this.eyeTextureId[i]);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA,
                GL11.GL_INT,
                null);

            GlStateManager._bindTexture(prevTexture);
            GraphicsHelper.INSTANCE.checkError((i == 0 ? "Left" : "Right") + " Eye framebuffer setup");
        }

        this.lastError = GraphicsHelper.INSTANCE.checkError("create VR textures");
    }

    @Override
    public void endFrame() {
        if (!((NullVR) this.vr).polled) {
            VRSettings.LOGGER.warn("Vivecraft: frame ended without polling new data first!");
        }

        ((NullVR) this.vr).polled = false;
    }

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public VRTextureTarget getLeftEyeTarget() {
        return framebufferEye0;
    }

    @Override
    public VRTextureTarget getRightEyeTarget() {
        return framebufferEye1;
    }

    @Override
    public String getName() {
        return "NullVR";
    }

    @Override
    protected void destroyBuffers() {
        super.destroyBuffers();

        if (this.framebufferEye0 != null) {
            this.framebufferEye0.destroyBuffers();
            this.framebufferEye0 = null;
        }

        if (this.framebufferEye1 != null) {
            this.framebufferEye1.destroyBuffers();
            this.framebufferEye1 = null;
        }
    }
}
