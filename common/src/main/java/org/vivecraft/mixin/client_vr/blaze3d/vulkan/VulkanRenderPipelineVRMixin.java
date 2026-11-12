package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vulkan.VulkanRenderPipeline;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfoKHR;
import org.vivecraft.client_vr.render.VRShaders;

import java.nio.IntBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;

@Mixin(VulkanRenderPipeline.class)
public class VulkanRenderPipelineVRMixin {

    @WrapOperation(method = "compile", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkPipelineRenderingCreateInfoKHR;sType$Default()Lorg/lwjgl/vulkan/VkPipelineRenderingCreateInfoKHR;"), require = 1)
    private static VkPipelineRenderingCreateInfoKHR vivecraft$multiviewViewMask(
        VkPipelineRenderingCreateInfoKHR instance, Operation<VkPipelineRenderingCreateInfoKHR> original)
    {
        VkPipelineRenderingCreateInfoKHR info = original.call(instance);
        if (MultiviewHelper.COMPILING) {
            info.viewMask((1 << MVGlTexture.VIEWS) - 1);
        }
        return info;
    }

    @WrapOperation(method = "compile", at = @At(value = "INVOKE", target = "Ljava/nio/IntBuffer;put(II)Ljava/nio/IntBuffer;"), require = 1)
    private static IntBuffer vivecraft$srgbAttachment(
        IntBuffer instance, int index, int format, Operation<IntBuffer> original,
        @Local(argsOnly = true) RenderPipeline pipeline)
    {
        return original.call(instance, index,
            pipeline == VRShaders.POST_PROCESSING_SRGB_PIPELINE ? VK10.VK_FORMAT_R8G8B8A8_SRGB : format);
    }
}
