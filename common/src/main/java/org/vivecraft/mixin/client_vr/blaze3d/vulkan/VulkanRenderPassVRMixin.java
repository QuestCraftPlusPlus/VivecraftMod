package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanRenderPass;
import com.mojang.blaze3d.vulkan.VulkanRenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.extensions.GlRenderPassExtension;
import org.vivecraft.client_vr.extensions.vulkan.VulkanDeviceExtension;

@Mixin(VulkanRenderPass.class)
public class VulkanRenderPassVRMixin implements GlRenderPassExtension {

    @Unique
    private boolean vivecraft$multiview;

    @Override
    @Unique
    public void vivecraft$setMultiview(boolean multiview) {
        this.vivecraft$multiview = multiview;
    }

    @Override
    @Unique
    public boolean vivecraft$isMultiview() {
        return this.vivecraft$multiview;
    }

    @WrapOperation(method = "setPipeline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanDevice;getOrCompilePipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)Lcom/mojang/blaze3d/vulkan/VulkanRenderPipeline;"), require = 1)
    private VulkanRenderPipeline vivecraft$multiviewPipeline(
        VulkanDevice device, RenderPipeline pipeline, Operation<VulkanRenderPipeline> original)
    {
        if (this.vivecraft$multiview) {
            VulkanRenderPipeline multiview = ((VulkanDeviceExtension) device).vivecraft$getOrCompileMultiviewPipeline(
                pipeline);
            if (multiview.isValid()) {
                return multiview;
            }
        }
        return original.call(device, pipeline);
    }
}
