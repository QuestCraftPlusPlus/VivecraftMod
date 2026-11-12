package org.vivecraft.mixin.client_vr.blaze3d.systems;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;

@Mixin(CommandEncoder.class)
public class CommandEncoderVRMixin {
    @WrapOperation(method = {
        "createRenderPass(Lcom/mojang/blaze3d/systems/RenderPassDescriptor;)Lcom/mojang/blaze3d/systems/RenderPass;",
        "verifyColorTexture", "verifyDepthTexture", "copyTextureToTexture"
    }, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTexture;getDepthOrLayers()I"), require = 6)
    private int vivecraft$allowMultiviewTextures(GpuTexture texture, Operation<Integer> original) {
        return MultiviewHelper.isMultiviewTexture(texture) ? 1 : original.call(texture);
    }
}
