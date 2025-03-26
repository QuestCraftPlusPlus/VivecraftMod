package org.vivecraft.client_vr.render.helpers.opengl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import org.lwjgl.opengl.GL30C;

public class OpenGLHelper {
    public static void genMipmaps(GpuTexture texture) {
        if (texture instanceof GlTexture glTexture) {
            int textureUnit = GlStateManager._getInteger(GL30C.GL_ACTIVE_TEXTURE);
            int boundTexture = GlStateManager._getInteger(GL30C.GL_TEXTURE_BINDING_2D);
            GL30C.glActiveTexture(GL30C.GL_TEXTURE0);
            GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, glTexture.glId());

            GL30C.glGenerateMipmap(GL30C.GL_TEXTURE_2D);

            GL30C.glActiveTexture(textureUnit);
            GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, boundTexture);
        } else {
            throw new IllegalStateException("Vivecraft: only opengl textures are supported");
        }
    }
}
