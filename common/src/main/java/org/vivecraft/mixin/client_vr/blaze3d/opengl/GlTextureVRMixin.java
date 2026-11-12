package org.vivecraft.mixin.client_vr.blaze3d.opengl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.helpers.graphics.opengl.ExternalGlTexture;

@Mixin(GlTexture.class)
public class GlTextureVRMixin {

    @WrapOperation(method = "destroyImmediately", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_deleteTexture(I)V"), require = 1)
    private void vivecraft$keepExternalTexture(int id, Operation<Void> original) {
        if (!((Object) this instanceof ExternalGlTexture)) {
            original.call(id);
        }
    }
}
