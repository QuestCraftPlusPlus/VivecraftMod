package org.vivecraft.mixin.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderTarget.class)
public class RenderTargetVRMixin {
    // This parameter that gets set makes the texture incomplete on GLES :/
    @Redirect(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texParameter(III)V", ordinal = 2))
    private void stopDepthParameter(int i, int j, int k) {

    }
}
