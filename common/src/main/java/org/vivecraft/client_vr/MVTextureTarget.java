package org.vivecraft.client_vr;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;
import org.vivecraft.client_vr.render.multiview.MVGlTextureView;

public class MVTextureTarget extends RenderTarget {

    public MVTextureTarget(String name, int width, int height) {
        super(name, true);
        RenderSystem.assertOnRenderThread();
        this.createBuffers(width, height);
    }

    @Override
    public void createBuffers(int width, int height) {
        RenderSystem.assertOnRenderThread();
        int maxSize = RenderSystem.getDevice().getMaxTextureSize();
        if (width <= 0 || width > maxSize || height <= 0 || height > maxSize) {
            throw new IllegalArgumentException(
                "Multiview " + width + "x" + height + " size out of bounds (max. size: " + maxSize + ")");
        }
        this.width = width;
        this.height = height;

        MVGlTexture depth = MVGlTexture.create(this.label + " / Depth", TextureFormat.DEPTH32, width, height);
        this.depthTexture = depth;
        this.depthTextureView = new MVGlTextureView(depth);

        MVGlTexture color = MVGlTexture.create(this.label + " / Color", TextureFormat.RGBA8, width, height);
        this.colorTexture = color;
        this.colorTextureView = new MVGlTextureView(color);
    }

    public void copyLayerTo(int layer, RenderTarget dest) {
        RenderSystem.assertOnRenderThread();
        int drawFbo = ((GlTexture) dest.getColorTexture()).getFbo(
            ((com.mojang.blaze3d.opengl.GlDevice) RenderSystem.getDevice()).directStateAccess(), null);
        ((MVGlTexture) this.colorTexture).blitLayer(layer, drawFbo, dest.width, dest.height);
    }

    @Override
    public String toString() {
        return """

            Vivecraft Multiview RenderTarget: %s
            Size: %s x %s x %s
            Color: %s"""
            .formatted(
                this.label,
                this.width, this.height, MVGlTexture.VIEWS,
                this.colorTexture == null ? "none" : this.colorTexture.getLabel());
    }
}
