package org.vivecraft.client_vr;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.VulkanHelper;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;
import org.vivecraft.client_vr.render.multiview.MVGlTextureView;

public class MVTextureTarget extends RenderTarget {

    public static int PENDING_VIEW_LAYER = -1;

    private @Nullable LayerTarget[] layerTargets;

    public static final Set<GpuTextureView> LAYER_VIEWS = Collections.newSetFromMap(new IdentityHashMap<>());

    public MVTextureTarget(String name, int width, int height, final GpuFormat format) {
        super(name, true, format);
        RenderSystem.assertOnRenderThread();
        this.createBuffers(width, height);
    }

    @Override
    public void createBuffers(int width, int height) {
        RenderSystem.assertOnRenderThread();
        this.width = width;
        this.height = height;

        GpuDevice device = RenderSystem.getDevice();

        if(device.backend instanceof GlDevice) {
            MVGlTexture depth = MVGlTexture.create(this.label + " / Depth", GpuFormat.D32_FLOAT, width, height);
            this.depthTexture = depth;
            this.depthTextureView = new MVGlTextureView(depth, ((GlDevice) device.backend).frameBufferCache());

            MVGlTexture color = MVGlTexture.create(this.label + " / Color", GpuFormat.RGBA8_UNORM, width, height);
            this.colorTexture = color;
            this.colorTextureView = new MVGlTextureView(color, ((GlDevice) device.backend).frameBufferCache());
        } else if (device.backend instanceof VulkanDevice vulkanDevice) {
            int usage = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_COPY_SRC |
                GpuTexture.USAGE_TEXTURE_BINDING;
            this.depthTexture = vulkanDevice.createTexture(() -> this.label + " / Depth", usage,
                GpuFormat.D32_FLOAT, width, height, MVGlTexture.VIEWS, 1);
            this.depthTextureView = vulkanDevice.createTextureView(this.depthTexture);

            this.colorTexture = vulkanDevice.createTexture(() -> this.label + " / Color", usage,
                GpuFormat.RGBA8_UNORM, width, height, MVGlTexture.VIEWS, 1);
            this.colorTextureView = vulkanDevice.createTextureView(this.colorTexture);

            this.layerTargets = new LayerTarget[MVGlTexture.VIEWS];
            for (int layer = 0; layer < MVGlTexture.VIEWS; layer++) {
                PENDING_VIEW_LAYER = layer;
                try {
                    GpuTextureView colorView = vulkanDevice.createTextureView(this.colorTexture);
                    GpuTextureView depthView = vulkanDevice.createTextureView(this.depthTexture);
                    LAYER_VIEWS.add(colorView);
                    LAYER_VIEWS.add(depthView);
                    this.layerTargets[layer] = new LayerTarget(this.label + " / Layer " + layer, this.colorTexture,
                        colorView, this.depthTexture, depthView, width, height);
                } finally {
                    PENDING_VIEW_LAYER = -1;
                }
            }
        } else {
            throw new IllegalStateException("Multiview is not supported on " + device.getDeviceInfo().backendName());
        }
    }

    @Override
    public void destroyBuffers() {
        if (this.layerTargets != null) {
            for (LayerTarget layerTarget : this.layerTargets) {
                layerTarget.destroyBuffers();
            }
            this.layerTargets = null;
        }
        super.destroyBuffers();
    }

    public @Nullable RenderTarget getLayerTarget(int layer) {
        return this.layerTargets == null ? null : this.layerTargets[layer];
    }

    public void copyLayerTo(int layer, RenderTarget dest) {
        RenderSystem.assertOnRenderThread();
        if (this.colorTexture instanceof MVGlTexture mvTexture) {
            mvTexture.blitLayer(layer, dest.getColorTextureView(), dest.width, dest.height);
        } else {
            ((VulkanHelper) GraphicsHelper.INSTANCE).copyArrayLayer(this.colorTexture, layer, dest.getColorTexture());
        }
    }

    private static class LayerTarget extends RenderTarget {
        LayerTarget(
            String label, GpuTexture color, GpuTextureView colorView, GpuTexture depth, GpuTextureView depthView,
            int width, int height)
        {
            super(label, true, color.getFormat());
            this.width = width;
            this.height = height;
            this.colorTexture = color;
            this.colorTextureView = colorView;
            this.depthTexture = depth;
            this.depthTextureView = depthView;
        }

        @Override
        public void createBuffers(int width, int height) {
            throw new UnsupportedOperationException("Multiview layer targets can't be resized");
        }

        @Override
        public void destroyBuffers() {
            for (GpuTextureView view : new GpuTextureView[]{this.colorTextureView, this.depthTextureView}) {
                if (view != null) {
                    LAYER_VIEWS.remove(view);
                    view.close();
                }
            }
            this.colorTextureView = null;
            this.depthTextureView = null;
            this.colorTexture = null;
            this.depthTexture = null;
        }
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
