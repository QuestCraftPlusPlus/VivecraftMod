package org.vivecraft.client_vr.provider.openxr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;

import java.io.IOException;
import java.nio.IntBuffer;

public class OpenXRStereoRenderer extends VRRenderer {
    private final MCOpenXR openxr;
    private int swapIndexLeft;
    private int swapIndexRight;
    private VRTextureTarget[] leftFramebuffers;
    private VRTextureTarget[] rightFramebuffers;
    private XrCompositionLayerProjectionView.Buffer projectionLayerViews;

    public OpenXRStereoRenderer(MCOpenXR vr) {
        super(vr);
        this.openxr = vr;
    }

    @Override
    public void createRenderTexture(int width, int height) {
        for(int i = 0; i < 2; i++) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                //Get amount of views in the swapchain
                IntBuffer intBuffer = stack.ints(0); //Set value to 0
                int error = XR10.xrEnumerateSwapchainImages(openxr.swapchain[i], intBuffer, null);
                this.openxr.logError(error, "xrEnumerateSwapchainImages", "get count");

                //Now we know the amount, create the image buffer
                int imageCount = intBuffer.get(0);
                XrSwapchainImageOpenGLKHR.Buffer swapchainImageBuffer = this.openxr.device.createImageBuffers(
                    imageCount, stack);

                error = XR10.xrEnumerateSwapchainImages(openxr.swapchain[i], intBuffer,
                    XrSwapchainImageBaseHeader.create(swapchainImageBuffer.address(), swapchainImageBuffer.capacity()));
                this.openxr.logError(error, "xrEnumerateSwapchainImages", "get images");

                if(i == 0) {
                    this.leftFramebuffers = new VRTextureTarget[imageCount];

                    for (int i2 = 0; i2 < imageCount; i2++) {
                        XrSwapchainImageOpenGLKHR openxrImage = swapchainImageBuffer.get(i2);
                        leftFramebuffers[i2] = VRTextureTarget.builder("L Eye " + i2).withSize(width, height)
                            .withTexId(openxrImage.image()).build();
                        eyeTextureId[i] = openxrImage.image();
                    }
                } else {
                    this.rightFramebuffers = new VRTextureTarget[imageCount];

                    for (int i2 = 0; i2 < imageCount; i2++) {
                        XrSwapchainImageOpenGLKHR openxrImage = swapchainImageBuffer.get(i2);
                        rightFramebuffers[i2] = VRTextureTarget.builder("R Eye " + i2).withSize(width, height)
                            .withTexId(openxrImage.image()).build();
                        eyeTextureId[i] = openxrImage.image();
                    }
                }
            }
        }
    }

    @Override
    public void setupRenderConfiguration(boolean render) throws IOException, RenderConfigException {
        super.setupRenderConfiguration(render);
        
        if (!render) {
            return;
        }
        if (this.projectionLayerViews == null) {
            // reused every frame, freed in destroy()
            this.projectionLayerViews = XrCompositionLayerProjectionView.calloc(2);
        }
        for(int i = 0; i < 2; i++) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer intBuf2 = stack.callocInt(1);

                int error = XR10.xrAcquireSwapchainImage(
                    openxr.swapchain[i],
                    XrSwapchainImageAcquireInfo.calloc(stack).type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_ACQUIRE_INFO),
                    intBuf2);
                this.openxr.logError(error, "xrAcquireSwapchainImage", "");

                error = XR10.xrWaitSwapchainImage(openxr.swapchain[i],
                    XrSwapchainImageWaitInfo.calloc(stack)
                        .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_WAIT_INFO)
                        .timeout(XR10.XR_INFINITE_DURATION));
                this.openxr.logError(error, "xrWaitSwapchainImage", "");

                if(i == 0) {
                    swapIndexLeft = intBuf2.get(0);
                } else {
                    swapIndexRight = intBuf2.get(0);
                }

                // Render view to the appropriate part of the swapchain image.
                var subImage = projectionLayerViews.get(i)
                    .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION_VIEW)
                    .pose(openxr.viewBuffer.get(i).pose())
                    .fov(openxr.viewBuffer.get(i).fov())
                    .subImage();
                subImage.swapchain(openxr.swapchain[i]);
                subImage.imageRect().offset().set(0, 0);
                subImage.imageRect().extent().set(openxr.width, openxr.height);
                subImage.imageArrayIndex(0);
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
            int error;
            for (int i = 0; i < 2; i++) {
                error = XR10.xrReleaseSwapchainImage(
                    openxr.swapchain[i],
                    XrSwapchainImageReleaseInfo.calloc(stack)
                        .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_RELEASE_INFO));
                this.openxr.logError(error, "xrReleaseSwapchainImage", "");
            }

            PointerBuffer layers = stack.callocPointer(1);

            XrCompositionLayerProjection compositionLayerProjection = XrCompositionLayerProjection.calloc(stack)
                .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION)
                .space(openxr.xrAppSpace)
                .views(projectionLayerViews);

            layers.put(compositionLayerProjection);

            layers.flip();

            error = XR10.xrEndFrame(
                openxr.session,
                XrFrameEndInfo.calloc(stack)
                    .type(XR10.XR_TYPE_FRAME_END_INFO)
                    .displayTime(openxr.time)
                    .environmentBlendMode(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)
                    .layers(layers));
            this.openxr.logError(error, "xrEndFrame", "");
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (this.projectionLayerViews != null) {
            this.projectionLayerViews.free();
            this.projectionLayerViews = null;
        }
    }

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public VRTextureTarget getLeftEyeTarget() {
        return leftFramebuffers[swapIndexLeft];
    }

    @Override
    public VRTextureTarget getRightEyeTarget() {
        return rightFramebuffers[swapIndexRight];
    }

    @Override
    public String getName() {
        return "OpenXR";
    }

    @Override
    public Vector2ic getRenderTextureSizes() {
        return new Vector2i(openxr.width, openxr.height);
    }
}
