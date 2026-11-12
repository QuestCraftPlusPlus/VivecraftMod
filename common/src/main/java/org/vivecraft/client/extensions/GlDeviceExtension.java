package org.vivecraft.client.extensions;

import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface GlDeviceExtension {
    /**
     * additional method to create a texture with a predefined id
     */
    GpuTexture vivecraft$createFixedIdTexture(
        @Nullable Supplier<String> labelSupplier, int usageFlags, TextureFormat textureFormat, int width,
        int height, int depthLayers, int mipmapLevels, int texId);

    /**
     * returns the multiview variant of the given pipeline, compiling it if needed.
     * The vertex shader of that variant renders to both layers of a multiview framebuffer.
     */
    GlRenderPipeline vivecraft$getOrCompileMultiviewPipeline(RenderPipeline pipeline);
}
