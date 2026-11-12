package org.vivecraft.client_vr.render.helpers.graphics.vulkan;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;

public class ExternalVulkanRenderTarget extends RenderTarget {

    public static long PENDING_IMAGE = 0L;

    public static boolean PENDING_SRGB_VIEW = false;

    public final long vkImage;

    public final boolean srgb;

    public ExternalVulkanRenderTarget(String label, int width, int height, long vkImage, boolean srgb) {
        super(label, false, GpuFormat.RGBA8_UNORM);
        RenderSystem.assertOnRenderThread();
        VulkanDevice device = (VulkanDevice) RenderSystem.getDevice().backend;
        this.vkImage = vkImage;
        this.srgb = srgb;
        this.width = width;
        this.height = height;

        PENDING_IMAGE = vkImage;
        try {
            this.colorTexture = new VulkanGpuTexture(device, GpuTexture.USAGE_RENDER_ATTACHMENT, label,
                GpuFormat.RGBA8_UNORM, width, height, 1, 1);
        } finally {
            PENDING_IMAGE = 0L;
        }
        PENDING_SRGB_VIEW = srgb;
        try {
            this.colorTextureView = device.createTextureView(this.colorTexture);
        } finally {
            PENDING_SRGB_VIEW = false;
        }
    }

    @Override
    public void createBuffers(int width, int height) {
        throw new UnsupportedOperationException("External render targets can't be resized");
    }
}
