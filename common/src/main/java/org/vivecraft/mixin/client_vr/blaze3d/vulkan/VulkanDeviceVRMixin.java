package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import com.mojang.blaze3d.vulkan.VulkanRenderPipeline;
import com.mojang.blaze3d.vulkan.glsl.GlslCompiler;
import com.mojang.blaze3d.vulkan.glsl.IntermediaryShaderModule;
import com.mojang.blaze3d.vulkan.glsl.ShaderCompileException;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.extensions.vulkan.VulkanDeviceExtension;
import org.vivecraft.client_vr.extensions.vulkan.VulkanPhysicalDeviceExtension;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@Mixin(VulkanDevice.class)
public abstract class VulkanDeviceVRMixin implements VulkanDeviceExtension {

    @Shadow
    @Final
    private ShaderSource defaultShaderSource;

    @Shadow
    @Final
    private GlslCompiler glslCompiler;

    @Shadow
    private VulkanRenderPipeline compilePipeline(RenderPipeline pipeline, ShaderSource shaderSource) {
        return null;
    }

    @Unique
    private Set<String> vivecraft$availableDeviceExtensions;

    @Unique
    private final Map<RenderPipeline, VulkanRenderPipeline> vivecraft$multiviewPipelines = new IdentityHashMap<>();

    @Unique
    private final Map<vivecraft$ShaderKey, IntermediaryShaderModule> vivecraft$multiviewShaders = new HashMap<>();

    @Unique
    private record vivecraft$ShaderKey(Identifier id, ShaderType type, ShaderDefines defines) {}

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanPhysicalDevice;close()V"))
    private void vivecraft$storeAvailableExtensions(
        CallbackInfo ci, @Local(argsOnly = true) VulkanPhysicalDevice physicalDevice)
    {
        this.vivecraft$availableDeviceExtensions = ((VulkanPhysicalDeviceExtension) physicalDevice).vivecraft$getAvailableExtensions();
    }

    @Override
    public Set<String> vivecraft$getAvailableDeviceExtensions() {
        return this.vivecraft$availableDeviceExtensions;
    }

    @Override
    public VulkanRenderPipeline vivecraft$getOrCompileMultiviewPipeline(RenderPipeline pipeline) {
        VulkanRenderPipeline compiled = this.vivecraft$multiviewPipelines.get(pipeline);
        if (compiled == null) {
            MultiviewHelper.COMPILING = true;
            try {
                compiled = this.compilePipeline(pipeline, this.defaultShaderSource);
            } finally {
                MultiviewHelper.COMPILING = false;
            }
            this.vivecraft$multiviewPipelines.put(pipeline, compiled);
            if (!compiled.isValid()) {
                MultiviewHelper.compileFailed(pipeline.getLocation().toString());
            }
        }
        return compiled;
    }

    @WrapOperation(method = "compilePipeline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanDevice;getOrCompileShader(Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/shaders/ShaderType;Lnet/minecraft/client/renderer/ShaderDefines;Lcom/mojang/blaze3d/shaders/ShaderSource;)Lcom/mojang/blaze3d/vulkan/glsl/IntermediaryShaderModule;"))
    private IntermediaryShaderModule vivecraft$multiviewVertexShader(
        VulkanDevice instance, Identifier id, ShaderType type, ShaderDefines defines, ShaderSource source,
        Operation<IntermediaryShaderModule> original)
    {
        if (!MultiviewHelper.COMPILING || type != ShaderType.VERTEX) {
            return original.call(instance, id, type, defines, source);
        }
        return this.vivecraft$multiviewShaders.computeIfAbsent(new vivecraft$ShaderKey(id, type, defines), key -> {
            String src = source.get(id, type);
            if (src == null) {
                return IntermediaryShaderModule.INVALID;
            }
            try {
                String multiview = MultiviewHelper.rewriteVertexShader(src);
                return this.glslCompiler.createIntermediary(id.toDebugFileName() + "_multiview",
                    GlslPreprocessor.injectDefines(multiview, defines), type);
            } catch (IllegalStateException | ShaderCompileException e) {
                VRSettings.LOGGER.error("Vivecraft: can't make multiview variant of shader {}: {}", id,
                    e.getMessage());
                return IntermediaryShaderModule.INVALID;
            }
        });
    }

    @Inject(method = "clearPipelineCache", at = @At("TAIL"))
    private void vivecraft$clearMultiviewPipelines(CallbackInfo ci) {
        this.vivecraft$multiviewPipelines.values().forEach(VulkanRenderPipeline::destroy);
        this.vivecraft$multiviewPipelines.clear();
        for (IntermediaryShaderModule shader : this.vivecraft$multiviewShaders.values()) {
            if (shader != IntermediaryShaderModule.INVALID) {
                shader.close();
            }
        }
        this.vivecraft$multiviewShaders.clear();
        MultiviewHelper.onPipelinesCleared();
    }
}
