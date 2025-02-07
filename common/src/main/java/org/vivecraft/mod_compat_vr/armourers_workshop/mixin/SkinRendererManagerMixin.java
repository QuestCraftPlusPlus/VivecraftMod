package org.vivecraft.mod_compat_vr.armourers_workshop.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.extensions.EntityRenderDispatcherExtension;

import java.util.Collection;
import java.util.HashSet;

@Pseudo
@Mixin(targets = "moe.plushie.armourers_workshop.core.client.skinrender.SkinRendererManager$ProfileLoader")
public class SkinRendererManagerMixin {
    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"), remap = false)
    private static Collection<EntityRenderer<? extends Player>> vivecraft$alsoAddVRSkins(
        Collection<EntityRenderer<? extends Player>> collection)
    {
        Collection<EntityRenderer<? extends Player>> c = new HashSet<>(collection);
        EntityRenderDispatcherExtension dispatcher = (EntityRenderDispatcherExtension) Minecraft.getInstance()
            .getEntityRenderDispatcher();
        c.addAll(dispatcher.vivecraft$getSkinMapVRVanilla().values());
        c.addAll(dispatcher.vivecraft$getSkinMapVRArms().values());
        c.addAll(dispatcher.vivecraft$getSkinMapVRLegs().values());
        return c;
    }
}
