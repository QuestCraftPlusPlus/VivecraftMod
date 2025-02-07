package org.vivecraft.mixin.client.renderer.entity.layers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.immersiveportals.ImmersivePortalsHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Mixin(value = PlayerItemInHandLayer.class, priority = 900)
public class PlayerItemInHandLayerMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noItemsInFirstPerson(
        CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) HumanoidArm arm,
        @Local(argsOnly = true) ItemStack itemStack)
    {
        if (entity == Minecraft.getInstance().player && VRState.VR_RUNNING &&
            ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf &&
            RenderPass.isFirstPerson(ClientDataHolderVR.getInstance().currentPass) &&
            !ShadersHelper.isRenderingShadows() &&
            !(ImmersivePortalsHelper.isLoaded() && ImmersivePortalsHelper.isRenderingPortal()) &&
            // don't cancel climbing claws, unless menu hand
            (ClientDataHolderVR.getInstance().vrSettings.modelArmsMode != VRSettings.ModelArmsMode.COMPLETE ||
                ClientDataHolderVR.getInstance().isMenuHand(arm) ||
                !(ClientDataHolderVR.getInstance().climbTracker.isClimbeyClimb() || ClimbTracker.isClaws(itemStack))
            ))
        {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean vivecraft$noSpyglassInFirstPerson(
        boolean isSpyglass, @Local(argsOnly = true) LivingEntity livingEntity)
    {
        return isSpyglass && !(livingEntity == Minecraft.getInstance().player && VRState.VR_RUNNING &&
            ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf &&
            RenderPass.isFirstPerson(ClientDataHolderVR.getInstance().currentPass) &&
            !ShadersHelper.isRenderingShadows() &&
            !(ImmersivePortalsHelper.isLoaded() && ImmersivePortalsHelper.isRenderingPortal())
        );
    }
}
