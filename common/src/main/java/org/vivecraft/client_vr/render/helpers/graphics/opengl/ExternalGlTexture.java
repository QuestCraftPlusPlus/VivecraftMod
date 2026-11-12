package org.vivecraft.client_vr.render.helpers.graphics.opengl;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.FrameBufferCache;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;

public class ExternalGlTexture extends GlTexture {

    public final int layer;

    public ExternalGlTexture(
        String label, GpuFormat format, int width, int height, int glId, int layer, FrameBufferCache frameBufferCache)
    {
        super(GpuTexture.USAGE_RENDER_ATTACHMENT, label, format, width, height, 1, 1, glId, frameBufferCache);
        this.layer = layer;
    }
}
