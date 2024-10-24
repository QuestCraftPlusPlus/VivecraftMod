package org.vivecraft.client_vr.provider.openxr;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRTextureTarget;

import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryStack.stackInts;

public class OpenXRSwapchain implements AutoCloseable {
    public final String name;
    public final XrSwapchain handle;
    public final MCOpenXR openxr;
    public int width;
    public int height;
    public XrSwapchainImageOpenGLESKHR.Buffer images;
    public VRTextureTarget innerFramebuffer;
    public VRTextureTarget framebuffer;

    public OpenXRSwapchain(XrSwapchain handle, MCOpenXR instance, String name) {
        this.name = name;
        this.handle = handle;
        this.openxr = instance;
    }

    public void createFramebuffers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer intBuf = stackInts(0);

            int error  = XR10.xrEnumerateSwapchainImages(this.handle, intBuf, null);
            this.openxr.logError(error, "xrEnumerateSwapchainImages", "get images");

            int imageCount = intBuf.get(0);
            XrSwapchainImageOpenGLESKHR.Buffer swapchainImageBuffer = XrSwapchainImageOpenGLESKHR.calloc(imageCount, stack);
            for (XrSwapchainImageOpenGLESKHR image : swapchainImageBuffer) {
                image.type(KHROpenGLESEnable.XR_TYPE_SWAPCHAIN_IMAGE_OPENGL_ES_KHR);
            }

            error = XR10.xrEnumerateSwapchainImages(handle, intBuf, XrSwapchainImageBaseHeader.create(swapchainImageBuffer.address(), swapchainImageBuffer.capacity()));
            this.openxr.logError(error, "xrEnumerateSwapchainImages", "get images");

            this.images = swapchainImageBuffer;
            this.innerFramebuffer = new VRTextureTarget(name, width, height, true, false, -1, true, true, ClientDataHolderVR.getInstance().vrSettings.vrUseStencil);

            this.framebuffer = new VRTextureTarget(name + " Mirror", width, height, true, false, -1, true, true, ClientDataHolderVR.getInstance().vrSettings.vrUseStencil);
        }
    }

    @Override
    public void close() {
        XR10.xrDestroySwapchain(handle);
        if (this.images != null) {
            this.images.close();
        }
        if (this.framebuffer != null) {
            RenderSystem.recordRenderCall(() -> {
                this.innerFramebuffer.destroyBuffers();
                this.framebuffer.destroyBuffers();
            });
        }
    }
}
