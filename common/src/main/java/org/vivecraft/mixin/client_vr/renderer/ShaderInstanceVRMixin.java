package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;

import java.util.Map;

@Mixin(ShaderInstance.class)
public class ShaderInstanceVRMixin {

    @WrapOperation(method = "getOrCreate", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private static Object vivecraft$noMultiviewCache(Map<Object, Object> programs, Object name,
        Operation<Object> original)
    {
        return MultiviewHelper.COMPILING ? null : original.call(programs, name);
    }
}
