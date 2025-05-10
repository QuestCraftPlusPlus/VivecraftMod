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
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;

@Pseudo
@Mixin(targets = "moe.plushie.armourers_workshop.core.client.skinrender.SkinRenderer")
public class SkinRendererVRMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lmoe/plushie/armourers_workshop/core/client/bake/BakedArmature;getTransform(Lmoe/plushie/armourers_workshop/core/client/bake/BakedSkinPart;)Lmoe/plushie/armourers_workshop/api/armature/IJointTransform;"), remap = false)
    private static IJointTransform vivecraft$shouldRender(
        IJointTransform original, @Local(argsOnly = true) Entity entity, @Local BakedSkinPart part)
    {
        boolean dontRender = VREffectsHelper.isRenderingFirstPersonPlayer(entity) &&
            (part.getType() instanceof HeadPartType || part.getType() instanceof HatPartType);
        return dontRender ? null : original;
    }
}
