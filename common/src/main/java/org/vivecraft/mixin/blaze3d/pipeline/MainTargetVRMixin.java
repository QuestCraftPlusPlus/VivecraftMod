package org.vivecraft.mixin.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.MainTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MainTarget.class)
public class MainTargetVRMixin {
    // This parameter that gets set makes the texture incomplete on GLES :/
    @Redirect(method = "createFrameBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texParameter(III)V", ordinal = 4))
    private void stopDepthParameter(int i, int j, int k) {

    }
}
