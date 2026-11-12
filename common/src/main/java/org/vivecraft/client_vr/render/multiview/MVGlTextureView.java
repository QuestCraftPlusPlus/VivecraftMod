package org.vivecraft.client_vr.render.multiview;

import com.mojang.blaze3d.opengl.FrameBufferCache;
import com.mojang.blaze3d.opengl.GlTextureView;

public class MVGlTextureView extends GlTextureView {

    public MVGlTextureView(MVGlTexture texture, FrameBufferCache cache) {
        super(texture, 0, 1, cache);
    }
}
