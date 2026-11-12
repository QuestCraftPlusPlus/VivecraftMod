package org.vivecraft.client_vr.render.helpers.graphics.opengl;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

public class ExternalGlRenderTarget extends RenderTarget {

    public final boolean srgb;

    public ExternalGlRenderTarget(String label, int width, int height, int glId, int layer, boolean srgb) {
        super(label, false, GpuFormat.RGBA8_UNORM);
        RenderSystem.assertOnRenderThread();
        this.srgb = srgb;
        GlDevice device = (GlDevice) RenderSystem.getDevice().backend;
        this.width = width;
        this.height = height;
        this.colorTexture = new ExternalGlTexture(label, GpuFormat.RGBA8_UNORM, width, height, glId, layer,
            device.frameBufferCache());
        this.colorTextureView = device.createTextureView(this.colorTexture);
    }

    @Override
    public void createBuffers(int width, int height) {
        throw new UnsupportedOperationException("External render targets can't be resized");
    }
}
