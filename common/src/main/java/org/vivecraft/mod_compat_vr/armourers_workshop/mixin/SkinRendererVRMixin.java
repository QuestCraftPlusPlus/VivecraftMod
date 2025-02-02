package org.vivecraft.mod_compat_vr.armourers_workshop.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.plushie.armourers_workshop.api.armature.IJointTransform;
import moe.plushie.armourers_workshop.core.client.bake.BakedSkinPart;
import moe.plushie.armourers_workshop.core.skin.part.head.HatPartType;
import moe.plushie.armourers_workshop.core.skin.part.head.HeadPartType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.mod_compat_vr.immersiveportals.ImmersivePortalsHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Pseudo
@Mixin(targets = "moe.plushie.armourers_workshop.core.client.skinrender.SkinRenderer")
public class SkinRendererVRMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lmoe/plushie/armourers_workshop/core/client/bake/BakedArmature;getTransform(Lmoe/plushie/armourers_workshop/core/client/bake/BakedSkinPart;)Lmoe/plushie/armourers_workshop/api/armature/IJointTransform;"), remap = false)
    private static IJointTransform vivecraft$shouldRender(
        IJointTransform original, @Local(argsOnly = true) Entity entity, @Local BakedSkinPart part)
    {
        boolean dontRender = VRState.VR_RUNNING && entity == Minecraft.getInstance().player &&
            ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf &&
            RenderPass.isFirstPerson(ClientDataHolderVR.getInstance().currentPass) &&
            !ShadersHelper.isRenderingShadows() &&
            !(ImmersivePortalsHelper.isLoaded() && ImmersivePortalsHelper.isRenderingPortal()) &&
            (part.getType() instanceof HeadPartType || part.getType() instanceof HatPartType);
        return dontRender ? null : original;
    }
}
