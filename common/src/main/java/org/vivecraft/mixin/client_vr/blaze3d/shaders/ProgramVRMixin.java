package org.vivecraft.mixin.client_vr.blaze3d.shaders;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;

import java.util.List;
import java.util.Map;

@Mixin(Program.class)
public class ProgramVRMixin {

    @ModifyArg(method = "compileShaderInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glShaderSource(ILjava/util/List;)V", remap = false), index = 1)
    private static List<String> vivecraft$multiviewSource(List<String> source) {
        if (!MultiviewHelper.COMPILING) return source;
        String src = String.join("", source);
        if (!MultiviewHelper.isVertexShader(src)) return source;
        return List.of(MultiviewHelper.rewriteVertexShader(src, "ProjMat", "ModelViewMat"));
    }

    @WrapOperation(method = "compileShader", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private static Object vivecraft$noMultiviewCache(
        Map<Object, Object> programs, Object name, Object program, Operation<Object> original)
    {
        if (MultiviewHelper.COMPILING) {
            MultiviewHelper.PROGRAMS.add((Program) program);
            return null;
        }
        return original.call(programs, name, program);
    }

    @WrapOperation(method = "close", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private Object vivecraft$noMultiviewCacheRemove(
        Map<Object, Object> programs, Object name, Operation<Object> original)
    {
        if (MultiviewHelper.PROGRAMS.remove((Program) (Object) this)) {
            return null;
        }
        return original.call(programs, name);
    }
}
