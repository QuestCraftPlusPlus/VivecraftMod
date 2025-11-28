package org.vivecraft.client_vr.provider.openxr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.util.Tuple;
import net.minecraft.util.profiling.Profiler;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.RenderHelper;

import java.io.IOException;
import java.nio.IntBuffer;

public class OpenXRStereoRenderer extends VRRenderer {
    private final MCOpenXR openxr;
    private final int[] swapIndex = new int[] {0, 0}; // Needs to be initialized otherwise stuff splodes
    private VRTextureTarget[] leftFramebuffers;
    private VRTextureTarget[] rightFramebuffers;
    private XrCompositionLayerProjectionView.Buffer projectionLayerViews;
    private boolean recalculateProjectionMatrix = true;


    public OpenXRStereoRenderer(MCOpenXR vr) {
        super(vr);
        this.openxr = vr;
    }

    @Override
    public void createRenderTexture(int width, int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for(int i = 0; i < 2; i++) {
                // Get amount of views in the swapchain
                IntBuffer intBuffer = stack.ints(0); //Set value to 0
                int error = XR10.xrEnumerateSwapchainImages(this.openxr.swapchain[i], intBuffer, null);
                this.openxr.logError(error, "xrEnumerateSwapchainImages", "get count");

                // Now we know the amount, create the image buffer
                int imageCount = intBuffer.get(0);
                XrSwapchainImageOpenGLKHR.Buffer swapchainImageBuffer = MCOpenXR.device.createImageBuffers(imageCount,
                        stack);

                error = XR10.xrEnumerateSwapchainImages(this.openxr.swapchain[i], intBuffer,
                        XrSwapchainImageBaseHeader.create(swapchainImageBuffer.address(), swapchainImageBuffer.capacity()));
                this.openxr.logError(error, "xrEnumerateSwapchainImages", "get images");

                if(i == 0) {
                    this.leftFramebuffers = new VRTextureTarget[imageCount];
                } else {
                    this.rightFramebuffers = new VRTextureTarget[imageCount];
                }

                String leftError = "";
                String rightError = "";
                for (int i1 = 0; i1 < imageCount; i1++) {
                    XrSwapchainImageOpenGLKHR openxrImage = swapchainImageBuffer.get(i1);
                    if(i == 0) {
                        this.leftFramebuffers[i1] = VRTextureTarget.builder("L Eye").withSize(width, height).withTexId(openxrImage.image()).withLinearFilter().build();
                        leftError = RenderHelper.checkGLError("Left Eye " + i1 + " framebuffer setup");
                    } else {
                        this.rightFramebuffers[i1] = VRTextureTarget.builder("R Eye").withSize(width, height).withTexId(openxrImage.image()).withLinearFilter().build();
                        rightError = RenderHelper.checkGLError("Right Eye " + i1 + " framebuffer setup");
                    }
                }
                if (this.lastError.isEmpty()) {
                    this.lastError = !leftError.isEmpty() ? leftError : rightError;
                }
            }
        }
    }

    @Override
    public void setupRenderConfiguration(boolean render) throws IOException, RenderConfigException {
        super.setupRenderConfiguration(render);

        if(!render) return;

        this.projectionLayerViews = XrCompositionLayerProjectionView.calloc(2);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for(int i = 0; i < 2; i++) {
                IntBuffer intBuf2 = stack.callocInt(1);

                int error = XR10.xrAcquireSwapchainImage(
                        this.openxr.swapchain[i],
                        XrSwapchainImageAcquireInfo.calloc(stack).type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_ACQUIRE_INFO),
                        intBuf2);
                this.openxr.logError(error, "xrAcquireSwapchainImage", "");

                error = XR10.xrWaitSwapchainImage(this.openxr.swapchain[i],
                        XrSwapchainImageWaitInfo.calloc(stack)
                                .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_WAIT_INFO)
                                .timeout(XR10.XR_INFINITE_DURATION));
                this.openxr.logError(error, "xrWaitSwapchainImage", "");

                this.swapIndex[i] = intBuf2.get(0);

                // Render view to the appropriate part of the swapchain image.
                XrSwapchainSubImage subImage = this.projectionLayerViews.get(i)
                        .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION_VIEW)
                        .pose(this.openxr.viewBuffer.get(i).pose())
                        .fov(this.openxr.viewBuffer.get(i).fov())
                        .subImage();
                subImage.swapchain(this.openxr.swapchain[i]);
                subImage.imageRect().offset().set(0, 0);
                subImage.imageRect().extent().set(this.openxr.width, this.openxr.height);
            }
            this.recalculateProjectionMatrix = true;
        }
    }

    /**
     * no caching for openxr
     * the projection matrix may change every frame, so recalculate it once per frame for up to date info
     */
    @Override
    public Matrix4f getCachedProjectionMatrix(int eyeType, float nearClip, float farClip) {
        if (this.recalculateProjectionMatrix) {
            this.eyeProj[0] = this.getProjectionMatrix(0, nearClip, farClip);
            this.eyeProj[1] = this.getProjectionMatrix(1, nearClip, farClip);
            this.recalculateProjectionMatrix = false;
        }
        return this.eyeProj[eyeType];
    }

    @Override
    public Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        XrFovf fov = this.openxr.viewBuffer.get(eyeType).fov();
        return new Matrix4f().setPerspectiveOffCenterFov(fov.angleLeft(), fov.angleRight(), fov.angleDown(),
            fov.angleUp(), nearClip, farClip);
    }

    @Override
    public void endFrame() throws RenderConfigException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer layers = stack.callocPointer(1);
            int error;
            for(int i = 0; i < 2; i++) {
                error = XR10.xrReleaseSwapchainImage(
                        this.openxr.swapchain[i],
                        XrSwapchainImageReleaseInfo.calloc(stack)
                                .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_RELEASE_INFO));
                this.openxr.logError(error, "xrReleaseSwapchainImage", "");
            }

            XrCompositionLayerProjection compositionLayerProjection = XrCompositionLayerProjection.calloc(stack)
                .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION)
                .space(this.openxr.xrAppSpace)
                .views(this.projectionLayerViews);

            layers.put(compositionLayerProjection);

            layers.flip();

            error = XR10.xrEndFrame(
                this.openxr.session,
                XrFrameEndInfo.calloc(stack)
                    .type(XR10.XR_TYPE_FRAME_END_INFO)
                    .displayTime(this.openxr.time)
                    .environmentBlendMode(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)
                    .layers(layers));
            this.openxr.logError(error, "xrEndFrame", "");

            this.projectionLayerViews.close();
        }
    }

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public RenderTarget getLeftEyeTarget() {
        if(this.leftFramebuffers == null) {
            return null;
        }
        return this.leftFramebuffers[this.swapIndex[0]];
    }

    @Override
    public RenderTarget getRightEyeTarget() {
        if(this.leftFramebuffers == null) {
            return null;
        }
        return this.rightFramebuffers[this.swapIndex[1]];
    }

    @Override
    public String getName() {
        return "OpenXR";
    }

    @Override
    public Tuple<Integer, Integer> getRenderTextureSizes() {
        return new Tuple<>(this.openxr.width, this.openxr.height);
    }

    @Override
    public void destroy() {
        super.destroy();

        if (this.leftFramebuffers != null) {
            for (VRTextureTarget leftFramebuffer : this.leftFramebuffers) {
                if(leftFramebuffer == null) {
                    continue;
                }
                leftFramebuffer.destroyBuffers();
            }
            this.leftFramebuffers = null;
        }

        if (this.rightFramebuffers != null) {
            for (VRTextureTarget rightFramebuffer : this.rightFramebuffers) {
                if(rightFramebuffer == null) {
                    continue;
                }
                rightFramebuffer.destroyBuffers();
            }
            this.rightFramebuffers = null;
        }
    }
}
