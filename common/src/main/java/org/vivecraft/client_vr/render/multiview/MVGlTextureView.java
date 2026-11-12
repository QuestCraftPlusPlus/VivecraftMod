package org.vivecraft.client_vr.render.multiview;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.textures.GpuTexture;
import org.jspecify.annotations.Nullable;

public class MVGlTextureView extends GlTextureView {

    public MVGlTextureView(MVGlTexture texture) {
        super(texture, 0, 1);
    }

    @Override
    public int getFbo(DirectStateAccess directStateAccess, @Nullable GpuTexture depthTexture) {
        return this.texture().getFbo(directStateAccess, depthTexture);
    }
}
