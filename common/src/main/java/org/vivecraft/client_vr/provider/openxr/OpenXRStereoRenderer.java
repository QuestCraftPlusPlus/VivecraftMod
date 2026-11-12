package org.vivecraft.client_vr.provider.openxr;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.jspecify.annotations.Nullable;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.helpers.ShaderHelper;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.render.helpers.graphics.ImageFormat;
import org.vivecraft.client_vr.render.helpers.graphics.RawTexture;
import org.vivecraft.client_vr.render.helpers.graphics.opengl.ExternalGlRenderTarget;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.ExternalVulkanRenderTarget;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.VulkanHelper;
import org.vivecraft.client_vr.render.helpers.graphics.opengl.OpenGLRawTexture;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.VulkanRawTexture;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenXRStereoRenderer extends VRRenderer {
    private static final boolean VULKAN_DIRECT_SWAPCHAIN = true;

    private final MCOpenXR openxr;

    private XrSwapchain[] swapchains;
    private boolean separateEyes;
    private ImageFormat swapchainFormat;

    private RawTexture[] leftFramebuffers;
    private RawTexture[] rightFramebuffers;

    private @Nullable RenderTarget[] leftDirect;
    private @Nullable RenderTarget[] rightDirect;

    private int @Nullable [] acquiredImages;

    private boolean framePending;

    private final VRTextureTarget[] gammaCorrected = new VRTextureTarget[2];

    public OpenXRStereoRenderer(MCOpenXR vr) {
        super(vr);
        this.openxr = vr;
    }

    private boolean needsGammaCorrection() {
        // since vulkan does automatic srgb conversion we need to do gamma correction even with srb,
        // except for RGBA8 since we can copy the raw data there
        return !this.swapchainFormat.srgb || (GraphicsHelper.INSTANCE.getType() == GraphicsHelper.Type.VULKAN &&
            this.swapchainFormat != ImageFormat.R8G8B8A8_SRGB
        );
    }

    @Override
    public void createRenderTexture(int width, int height) {
        super.createRenderTexture(width, height);

        if (!this.lastError.isEmpty()) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {

            this.initializeOpenXRSwapChain(stack, width, height);

            if (needsGammaCorrection()) {
                // for non srgb formats we need to output in gamma corrected colors
                String errors = "";
                for (int i = 0; i < 2; i++) {
                    this.gammaCorrected[i] = VRTextureTarget.builder((i == 0 ? "L" : "R") + " Eye Gamma")
                        .withSize(width, height)
                        // use 16 bit, so we do not have any banding, ideally
                        .withFormat(GpuFormat.RGBA16_UNORM)
                        .build();
                    VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferEye[i]);
                    errors += GraphicsHelper.INSTANCE.checkError(
                        (i == 0 ? "Left" : "Right") + " Gamma Eye framebuffer setup");
                }
                if (!errors.isEmpty()) {
                    this.lastError = errors;
                    return;
                }
            } else {
                // no gamma correction needed, just link to the same texture
                for (int i = 0; i < 2; i++) {
                    this.gammaCorrected[i] = this.framebufferEye[i];
                }
            }

            SwapchainImages left;
            SwapchainImages right;
            if (this.separateEyes) {
                left = this.linkSwapchainImages(stack, this.swapchains[0], "L Eye", width, height, -1);
                right = this.linkSwapchainImages(stack, this.swapchains[1], "R Eye", width, height, -1);
            } else {
                left = this.linkSwapchainImages(stack, this.swapchains[0], "L Eye", width, height, 0);
                right = this.linkSwapchainImages(stack, this.swapchains[0], "R Eye", width, height, 1);
            }
            this.leftFramebuffers = left.raw();
            this.rightFramebuffers = right.raw();
            this.leftDirect = left.direct();
            this.rightDirect = right.direct();
        }
    }

    private boolean canRenderDirect() {
        return switch (GraphicsHelper.INSTANCE.getType()) {
            case OPENGL -> !needsGammaCorrection() &&
                (this.swapchainFormat == ImageFormat.R8G8B8A8_SRGB ||
                    this.swapchainFormat == ImageFormat.R8G8B8A8_UNORM
                );
            case VULKAN -> VULKAN_DIRECT_SWAPCHAIN && this.swapchainFormat == ImageFormat.R8G8B8A8_SRGB;
        };
    }

    private boolean mirrorNeedsEyeTextures() {
        VRSettings settings = ClientDataHolderVR.getInstance().vrSettings;
        return settings.renderAllPasses || switch (settings.displayMirrorMode) {
            case DUAL, SINGLE, CROPPED -> true;
            case OFF -> !this.openxr.isHMDTracking();
            default -> false;
        };
    }

    @Override
    public RenderTarget getEyeTarget(RenderPass eye) {
        if (this.leftDirect != null && this.rightDirect != null &&
            (this.acquiredImages != null || !mirrorNeedsEyeTextures()))
        {
            if (this.acquiredImages == null) {
                this.acquiredImages = this.acquireImages();
                if (GraphicsHelper.INSTANCE.getType() == GraphicsHelper.Type.VULKAN) {
                    this.transitionAcquiredImages(true);
                }
            }
            return eye == RenderPass.RIGHT ? this.rightDirect[this.acquiredImages[this.separateEyes ? 1 : 0]] :
                this.leftDirect[this.acquiredImages[0]];
        }
        return super.getEyeTarget(eye);
    }

    private void transitionAcquiredImages(boolean toRendering) {
        VulkanHelper vulkanHelper = (VulkanHelper) GraphicsHelper.INSTANCE;
        vulkanHelper.transitionExternalImage(((ExternalVulkanRenderTarget) this.leftDirect[this.acquiredImages[0]]).vkImage,
            VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, toRendering);
        vulkanHelper.transitionExternalImage(
            ((ExternalVulkanRenderTarget) this.rightDirect[this.acquiredImages[this.separateEyes ? 1 : 0]]).vkImage,
            VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, toRendering);
    }

    private int[] acquireImages() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int[] imageIndex = new int[this.swapchains.length];
            for (int i = 0; i < this.swapchains.length; i++) {
                IntBuffer indexBuffer = stack.callocInt(1);
                int error = XR10.xrAcquireSwapchainImage(
                    this.swapchains[i],
                    XrSwapchainImageAcquireInfo.calloc(stack).type$Default(),
                    indexBuffer);
                MCOpenXR.logError(error, "xrAcquireSwapchainImage", "");
                imageIndex[i] = indexBuffer.get(0);

                error = XR10.xrWaitSwapchainImage(this.swapchains[i],
                    XrSwapchainImageWaitInfo.calloc(stack)
                        .type$Default()
                        .timeout(XR10.XR_INFINITE_DURATION));
                MCOpenXR.logError(error, "xrWaitSwapchainImage", "");
            }
            return imageIndex;
        }
    }

    private void initializeOpenXRSwapChain(MemoryStack stack, int width, int height) {
        IntBuffer intBuf = stack.callocInt(1);
        // Check swapchain formats
        int error = XR10.xrEnumerateSwapchainFormats(this.openxr.session, intBuf, null);
        MCOpenXR.logError(error, "xrEnumerateSwapchainFormats", "get count");

        // Get swapchain formats
        LongBuffer swapchainFormats = stack.callocLong(intBuf.get(0));
        error = XR10.xrEnumerateSwapchainFormats(this.openxr.session, intBuf, swapchainFormats);
        MCOpenXR.logError(error, "xrEnumerateSwapchainFormats", "get formats");

        // Choose format, the runtime sorts the formats with the preferred ones being first
        long runtimePreferredFormat = 0;
        boolean supportsRGBA8srgb = false;
        swapchainFormats.rewind();
        while (swapchainFormats.hasRemaining()) {
            long format = swapchainFormats.get();
            ImageFormat imageFormat = GraphicsHelper.INSTANCE.formatFromAPI((int) format);
            // check if we support the format
            if (imageFormat != null && GraphicsHelper.INSTANCE.supportedImageFormats().contains(imageFormat)) {
                if (runtimePreferredFormat == 0) {
                    runtimePreferredFormat = format;
                }
                if (imageFormat == ImageFormat.R8G8B8A8_SRGB) {
                    supportsRGBA8srgb = true;
                }
            }
        }


        // log supported formats
        swapchainFormats.rewind();
        while (swapchainFormats.hasRemaining()) {
            long format = swapchainFormats.get();
            ImageFormat imageFormat = GraphicsHelper.INSTANCE.formatFromAPI((int) format);
            VRSettings.LOGGER.info("Vivecraft: runtime supports format: {} ({}), GPU supported: {}",
                imageFormat, format, GraphicsHelper.INSTANCE.supportedImageFormats().contains(imageFormat));
        }


        long chosenFormat = 0;
        // choose the runtime preferred format, unless it supports R8G8B8A8_SRGB, since that is what mc renders as
        if (runtimePreferredFormat != 0) {
            chosenFormat = supportsRGBA8srgb ? GraphicsHelper.INSTANCE.formatToAPI(ImageFormat.R8G8B8A8_SRGB) :
                runtimePreferredFormat;
        }

        // force a format
        // chosenFormat = GraphicsHelper.INSTANCE.formatToAPI(ImageFormat.R8G8B8A8_SRGB);

        if (chosenFormat == 0) {
            List<Long> formats = new ArrayList<>();
            swapchainFormats.rewind();
            while (swapchainFormats.hasRemaining()) {
                formats.add(swapchainFormats.get());
            }
            throw new RuntimeException(
                "No compatible swapchain / framebuffer format available, runtime only supports these: " + formats);
        }

        VRSettings.LOGGER.info("Vivecraft: runtime prefers format: {} ({})",
            GraphicsHelper.INSTANCE.formatFromAPI((int) runtimePreferredFormat), runtimePreferredFormat);
        if (supportsRGBA8srgb) {
            VRSettings.LOGGER.info("Vivecraft: runtime supports R8G8B8A8_SRGB so using that instead.");
        }

        // Make swapchain
        XrSwapchainCreateInfo swapchainCreateInfo = XrSwapchainCreateInfo.calloc(stack);
        swapchainCreateInfo.type$Default();
        swapchainCreateInfo.next(NULL);
        swapchainCreateInfo.createFlags(0);
        boolean vulkan = GraphicsHelper.INSTANCE.getType() == GraphicsHelper.Type.VULKAN;
        swapchainCreateInfo.usageFlags(
            XR10.XR_SWAPCHAIN_USAGE_COLOR_ATTACHMENT_BIT |
                XR10.XR_SWAPCHAIN_USAGE_TRANSFER_DST_BIT);
        swapchainCreateInfo.format(chosenFormat);
        swapchainCreateInfo.sampleCount(1);
        swapchainCreateInfo.width(width);
        swapchainCreateInfo.height(height);
        swapchainCreateInfo.faceCount(1);
        this.separateEyes = this.openxr.device.separateEyeSwapchains() || (vulkan && VULKAN_DIRECT_SWAPCHAIN);
        swapchainCreateInfo.arraySize(this.separateEyes ? 1 : 2);
        swapchainCreateInfo.mipCount(1);

        this.swapchains = new XrSwapchain[this.separateEyes ? 2 : 1];
        for (int i = 0; i < this.swapchains.length; i++) {
            PointerBuffer handlePointer = stack.callocPointer(1);
            error = XR10.xrCreateSwapchain(this.openxr.session, swapchainCreateInfo, handlePointer);
            MCOpenXR.logError(error, "xrCreateSwapchain", "format: " + chosenFormat);
            this.swapchains[i] = new XrSwapchain(handlePointer.get(0), this.openxr.session);
        }
        this.swapchainFormat = GraphicsHelper.INSTANCE.formatFromAPI((int) chosenFormat);
    }

    private SwapchainImages linkSwapchainImages(
        MemoryStack stack, XrSwapchain swapchain, String name, int width, int height, int layer)
    {
        IntBuffer countBuffer = stack.callocInt(1);
        int error = XR10.xrEnumerateSwapchainImages(swapchain, countBuffer, null);
        MCOpenXR.logError(error, "xrEnumerateSwapchainImages", "get count");
        int imageCount = countBuffer.get(0);
        RawTexture[] textures = new RawTexture[imageCount];
        RenderTarget[] direct = null;

        switch (GraphicsHelper.INSTANCE.getType()) {
            case OPENGL -> {
                XrSwapchainImageOpenGLKHR.Buffer images = this.openxr.device.createOpenglImageBuffers(stack,
                    imageCount);
                error = XR10.xrEnumerateSwapchainImages(swapchain, countBuffer,
                    XrSwapchainImageBaseHeader.create(images.address(), images.capacity()));
                MCOpenXR.logError(error, "xrEnumerateSwapchainImages", "get images");

                if (this.canRenderDirect()) {
                    direct = new ExternalGlRenderTarget[imageCount];
                }
                for (int i = 0; i < imageCount; i++) {
                    textures[i] = OpenGLRawTexture.link(name + " swapchain " + i, width, height,
                        this.swapchainFormat, images.get(i).image(), layer);
                    if (direct != null) {
                        direct[i] = new ExternalGlRenderTarget(name + " swapchain " + i, width, height,
                            images.get(i).image(), layer, this.swapchainFormat.srgb);
                    }
                    String glError = GraphicsHelper.INSTANCE.checkError(name + " " + i + " framebuffer setup");
                    if (this.lastError.isEmpty()) {
                        this.lastError = glError;
                    }
                }
            }
            case VULKAN -> {
                XrSwapchainImageVulkanKHR.Buffer images = this.openxr.device.createVulkanImageBuffers(stack,
                    imageCount);
                error = XR10.xrEnumerateSwapchainImages(swapchain, countBuffer,
                    XrSwapchainImageBaseHeader.create(images.address(), images.capacity()));
                MCOpenXR.logError(error, "xrEnumerateSwapchainImages", "get images");

                if (this.canRenderDirect() && layer <= 0) {
                    direct = new RenderTarget[imageCount];
                }
                for (int i = 0; i < imageCount; i++) {
                    textures[i] = VulkanRawTexture.link(name + " swapchain " + i, width, height,
                        this.swapchainFormat, images.get(i).image(), Math.max(0, layer),
                        VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
                    if (direct != null) {
                        direct[i] = new ExternalVulkanRenderTarget(name + " swapchain " + i, width, height,
                            images.get(i).image(), true);
                    }
                }
            }
        }
        return new SwapchainImages(textures, direct);
    }

    private record SwapchainImages(RawTexture[] raw, @Nullable RenderTarget[] direct) {}

    @Override
    public Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        XrFovf fov = this.openxr.viewBuffer.get(eyeType).fov();
        return new Matrix4f().setPerspectiveOffCenterFov(
            fov.angleLeft(), fov.angleRight(),
            fov.angleDown(), fov.angleUp(),
            nearClip, farClip, RenderSystem.getDevice().getDeviceInfo().isZZeroToOne());
    }

    @Override
    public void endFrame() throws RenderConfigException {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            if (this.acquiredImages == null) {
                int[] imageIndex = this.acquireImages();

                if (needsGammaCorrection()) {
                    for (int i = 0; i < 2; i++) {
                        int eye = i;
                        ShaderHelper.renderFullscreenQuad(() -> "gamma correction",
                            VRShaders.GAMMA_CORRECTION_PIPELINE,
                            pass -> pass.bindTexture(VRShaders.BLIT_VR_COLOR_SAMPLER,
                                this.framebufferEye[eye].getColorTextureView(),
                                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)),
                            this.gammaCorrected[eye].getColorTextureView());
                    }
                }

                GraphicsHelper.INSTANCE.blitTextures(this.gammaCorrected,
                    this.leftFramebuffers[imageIndex[0]],
                    this.rightFramebuffers[imageIndex[this.separateEyes ? 1 : 0]]);
            } else if (GraphicsHelper.INSTANCE.getType() == GraphicsHelper.Type.VULKAN) {
                this.transitionAcquiredImages(false);
            }
            this.acquiredImages = null;

            this.framePending = true;
        }
    }

    @Override
    public void afterSubmit() {
        if (!this.framePending) return;
        this.framePending = false;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (GraphicsHelper.INSTANCE.getType() == GraphicsHelper.Type.OPENGL) {
                GraphicsHelper.INSTANCE.flush();
            }

            for (XrSwapchain swapchain : this.swapchains) {
                int error = XR10.xrReleaseSwapchainImage(
                    swapchain,
                    XrSwapchainImageReleaseInfo.calloc(stack).type$Default());
                MCOpenXR.logError(error, "xrReleaseSwapchainImage", "");
            }

            // tell the runtime where in the image we wrote to, so that it can correctly composite it to the headset
            XrCompositionLayerProjectionView.Buffer projectionViews = XrCompositionLayerProjectionView.calloc(2,
                stack);
            for (int viewIndex = 0; viewIndex < 2; viewIndex++) {
                XrSwapchainSubImage subImage = projectionViews.get(viewIndex)
                    .type$Default()
                    .pose(this.openxr.viewBuffer.get(viewIndex).pose())
                    .fov(this.openxr.viewBuffer.get(viewIndex).fov())
                    .subImage();
                subImage.swapchain(this.swapchains[this.separateEyes ? viewIndex : 0]);
                subImage.imageRect().offset().set(0, 0);
                subImage.imageRect().extent().set(this.resolution.x(), this.resolution.y());
                subImage.imageArrayIndex(this.separateEyes ? 0 : viewIndex);
            }
            XrCompositionLayerProjection compositionLayerProjection = XrCompositionLayerProjection.calloc(stack)
                .type$Default()
                .space(this.openxr.xrAppSpace)
                .views(projectionViews);

            PointerBuffer layers = stack.callocPointer(1);
            layers.put(compositionLayerProjection);
            layers.flip();

            // tell the runtime that to composite the image to the headset
            int error = XR10.xrEndFrame(
                this.openxr.session,
                XrFrameEndInfo.calloc(stack)
                    .type$Default()
                    .displayTime(this.openxr.time)
                    .environmentBlendMode(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)
                    .layers(layers));
            MCOpenXR.logError(error, "xrEndFrame", "");
        }
    }

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public String getName() {
        return "OpenXR";
    }

    @Override
    public Vector2ic getRenderTextureSizes() {
        if (this.resolution == null) {
            XrViewConfigurationView viewConfiguration = this.openxr.viewConfigurationBuffer.get(0);
            this.resolution = new Vector2i(viewConfiguration.recommendedImageRectWidth(),
                viewConfiguration.recommendedImageRectHeight());
        }
        return this.resolution;
    }

    @Override
    public void destroy() {
        super.destroy();

        for (int i = 0; i < 2; i++) {
            if (this.gammaCorrected[i] != null) {
                this.gammaCorrected[i].destroyBuffers();
                this.gammaCorrected[i] = null;
            }
        }

        if (this.leftFramebuffers != null) {
            for (RawTexture left : this.leftFramebuffers) {
                left.destroy();
            }
            this.leftFramebuffers = null;
        }

        if (this.rightFramebuffers != null) {
            for (RawTexture right : this.rightFramebuffers) {
                right.destroy();
            }
            this.rightFramebuffers = null;
        }

        for (RenderTarget[] targets : new RenderTarget[][]{this.leftDirect, this.rightDirect}) {
            if (targets != null) {
                for (RenderTarget target : targets) {
                    target.destroyBuffers();
                }
            }
        }
        this.leftDirect = null;
        this.rightDirect = null;
        this.acquiredImages = null;

        if (this.swapchains != null) {
            for (XrSwapchain swapchain : this.swapchains) {
                int error = XR10.xrDestroySwapchain(swapchain);
                MCOpenXR.logError(error, "xrDestroySwapchain", "");
            }
            this.swapchains = null;
        }
    }
}
