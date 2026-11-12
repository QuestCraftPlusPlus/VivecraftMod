package org.vivecraft.mixin.client.blaze3d.opengl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.GlDeviceExtension;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(GlDevice.class)
public class GlDeviceMixin implements GlDeviceExtension {
    @Shadow
    @Final
    private ShaderSource defaultShaderSource;

    @Unique
    private final Map<RenderPipeline, GlRenderPipeline> vivecraft$multiviewPipelines = new IdentityHashMap<>();

    @Unique
    private final Map<GlDevice.ShaderCompilationKey, GlShaderModule> vivecraft$multiviewShaders = new HashMap<>();

    @Shadow
    private GlRenderPipeline compilePipeline(RenderPipeline pipeline, ShaderSource shaderSource) {
        return null;
    }

    @Shadow
    private GlShaderModule compileShader(GlDevice.ShaderCompilationKey key, ShaderSource source) {
        return null;
    }

    @Override
    public GlRenderPipeline vivecraft$getOrCompileMultiviewPipeline(RenderPipeline pipeline) {
        GlRenderPipeline compiled = this.vivecraft$multiviewPipelines.get(pipeline);
        if (compiled == null) {
            MultiviewHelper.COMPILING = true;
            try {
                compiled = this.compilePipeline(pipeline, this.defaultShaderSource);
            } finally {
                MultiviewHelper.COMPILING = false;
            }
            this.vivecraft$multiviewPipelines.put(pipeline, compiled);
            if (compiled.isValid()) {
                MultiviewHelper.registerProgram(compiled.program());
            } else {
                MultiviewHelper.compileFailed(pipeline.getLocation().toString());
            }
        }
        return compiled;
    }

    @WrapOperation(method = "compileProgram", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlDevice;getOrCompileShader(Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/shaders/ShaderType;Lnet/minecraft/client/renderer/ShaderDefines;Lcom/mojang/blaze3d/shaders/ShaderSource;)Lcom/mojang/blaze3d/opengl/GlShaderModule;"))
    private GlShaderModule vivecraft$multiviewVertexShader(
        GlDevice instance, Identifier id, ShaderType type, ShaderDefines defines, ShaderSource source,
        Operation<GlShaderModule> original)
    {
        if (!MultiviewHelper.COMPILING || type != ShaderType.VERTEX) {
            return original.call(instance, id, type, defines, source);
        }
        return this.vivecraft$multiviewShaders.computeIfAbsent(new GlDevice.ShaderCompilationKey(id, type, defines),
            key -> this.compileShader(key, (shaderId, shaderType) -> {
                String src = source.get(shaderId, shaderType);
                if (src == null) {
                    return null;
                }
                try {
                    return MultiviewHelper.rewriteVertexShader(src);
                } catch (IllegalStateException e) {
                    VRSettings.LOGGER.error("Vivecraft: can't make multiview variant of shader {}: {}", shaderId,
                        e.getMessage());
                    return null;
                }
            }));
    }

    @Inject(method = "clearPipelineCache", at = @At("HEAD"))
    private void vivecraft$clearMultiviewPipelines(CallbackInfo ci) {
        for (GlRenderPipeline pipeline : this.vivecraft$multiviewPipelines.values()) {
            if (pipeline.isValid()) {
                MultiviewHelper.unregisterProgram(pipeline.program());
                pipeline.program().close();
            }
        }
        this.vivecraft$multiviewPipelines.clear();
        for (GlShaderModule shader : this.vivecraft$multiviewShaders.values()) {
            if (shader != GlShaderModule.INVALID_SHADER) {
                shader.close();
            }
        }
        this.vivecraft$multiviewShaders.clear();
        MultiviewHelper.onPipelinesCleared();
    }
}
