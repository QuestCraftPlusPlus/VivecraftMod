package org.vivecraft.client_vr.provider.openxr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL31;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;

import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public class OpenXRStereoRenderer extends VRRenderer {
    private final MCOpenXR openxr;
    private XrCompositionLayerProjectionView.Buffer projectionLayerViews;

    public OpenXRStereoRenderer(MCOpenXR vr) {
        super(vr);
        this.openxr = vr;
    }

    @Override
    public void createRenderTexture(int width, int height) throws RenderConfigException{
        for (int i = 0; i < openxr.viewCount; i++) {
            this.openxr.swapchains[i].createFramebuffers();
        }
    }

    @Override
    public void setupRenderConfiguration(boolean render) throws Exception {
        super.setupRenderConfiguration(render);
        
        if (!render) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()){
            for (int i = 0; i < openxr.viewCount; i++) {
                IntBuffer intBuf2 = stack.callocInt(1);

                int error = XR10.xrAcquireSwapchainImage(
                    openxr.swapchains[i].handle,
                    XrSwapchainImageAcquireInfo.calloc(stack).type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_ACQUIRE_INFO),
                    intBuf2);
                this.openxr.logError(error, "xrAcquireSwapchainImage", "");

                error = XR10.xrWaitSwapchainImage(openxr.swapchains[i].handle,
                    XrSwapchainImageWaitInfo.calloc(stack)
                        .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_WAIT_INFO)
                        .timeout(XR10.XR_INFINITE_DURATION));
                this.openxr.logError(error, "xrWaitSwapchainImage", "");

                int swapIndex = intBuf2.get(0);
                XrSwapchainImageOpenGLESKHR xrSwapchainImageOpenGLESKHR = openxr.swapchains[i].images.get(swapIndex);
                ((RenderTargetExtension) openxr.swapchains[i]).vivecraft$setColorid(xrSwapchainImageOpenGLESKHR.image());
            }
        }
    }

    @Override
    public Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        XrFovf fov = openxr.viewBuffer.get(eyeType).fov();
        return new Matrix4f().setPerspectiveOffCenterFov(fov.angleLeft(), fov.angleRight(), fov.angleDown(), fov.angleUp(), nearClip, farClip);
    }

    @Override
    public void endFrame() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.projectionLayerViews = XrCompositionLayerProjectionView.calloc(2);
            for (int viewIndex = 0; viewIndex < this.openxr.viewCount; viewIndex++) {
                GL31.glBindFramebuffer(GL31.GL_READ_FRAMEBUFFER, getLeftEyeTarget().frameBufferId);
                GL31.glBindFramebuffer(GL31.GL_DRAW_FRAMEBUFFER, this.openxr.swapchains[viewIndex].innerFramebuffer.frameBufferId);
                GL31.glBlitFramebuffer(0, 0, getLeftEyeTarget().viewWidth, getLeftEyeTarget().viewHeight, 0, 0, this.openxr.swapchains[viewIndex].innerFramebuffer.viewWidth, this.openxr.swapchains[viewIndex].innerFramebuffer.viewHeight, GL31.GL_STENCIL_BUFFER_BIT | GL31.GL_COLOR_BUFFER_BIT, GL31.GL_NEAREST);

                var subImage = projectionLayerViews.get(viewIndex)
                    .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION_VIEW)
                    .pose(openxr.viewBuffer.get(viewIndex).pose())
                    .fov(openxr.viewBuffer.get(viewIndex).fov())
                    .subImage();
                subImage.swapchain(openxr.swapchains[viewIndex].handle);
                subImage.imageRect().offset().set(0, 0);
                subImage.imageRect().extent().set(openxr.width, openxr.height);
                subImage.imageArrayIndex(viewIndex);
            }

            PointerBuffer layers = stack.callocPointer(1);
            int error;
            if (this.openxr.shouldRender) {
                XrCompositionLayerProjection compositionLayerProjection = XrCompositionLayerProjection.calloc(stack)
                    .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION)
                    .space(openxr.xrAppSpace)
                    .views(projectionLayerViews);

                layers.put(compositionLayerProjection);
            }
            layers.flip();

            error = XR10.xrEndFrame(
                openxr.session,
                XrFrameEndInfo.calloc(stack)
                    .type(XR10.XR_TYPE_FRAME_END_INFO)
                    .displayTime(openxr.time)
                    .environmentBlendMode(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)
                    .layers(layers));
            this.openxr.logAll(error, "xrEndFrame", "");

            projectionLayerViews.close();
        }
    }

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public RenderTarget getLeftEyeTarget() {
        return openxr.swapchains[0].framebuffer;
    }

    @Override
    public RenderTarget getRightEyeTarget() {
        return openxr.swapchains[1].framebuffer;
    }

    @Override
    public Tuple<Integer, Integer> getRenderTextureSizes() {
        return new Tuple<>(openxr.width, openxr.height);
    }
}
