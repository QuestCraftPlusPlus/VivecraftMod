package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vulkan.glsl.GlslCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;

import java.util.ArrayList;
import java.util.List;

@Mixin(GlslCompiler.class)
public class GlslCompilerVRMixin {
    @WrapOperation(method = "addToBindGroup", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/BindGroupLayout;flattenUniforms(Ljava/util/List;)Ljava/util/List;"), require = 2)
    private static List<BindGroupLayout.UniformDescription> vivecraft$multiviewUniformBlock(
        List<BindGroupLayout> bindGroupLayouts, Operation<List<BindGroupLayout.UniformDescription>> original)
    {
        List<BindGroupLayout.UniformDescription> uniforms = original.call(bindGroupLayouts);
        if (MultiviewHelper.COMPILING) {
            uniforms = new ArrayList<>(uniforms);
            uniforms.add(new BindGroupLayout.UniformDescription(MultiviewHelper.UNIFORM_BLOCK,
                UniformType.UNIFORM_BUFFER));
        }
        return uniforms;
    }
}
