package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;
import com.mojang.blaze3d.vulkan.VulkanGpuTextureView;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.MVTextureTarget;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.ExternalVulkanRenderTarget;

@Mixin(VulkanGpuTextureView.class)
public class VulkanGpuTextureViewVRMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkImageViewCreateInfo;viewType(I)Lorg/lwjgl/vulkan/VkImageViewCreateInfo;"), require = 1)
    private VkImageViewCreateInfo vivecraft$multiviewViewType(
        VkImageViewCreateInfo instance, int viewType, Operation<VkImageViewCreateInfo> original,
        @Local(argsOnly = true) VulkanGpuTexture texture)
    {
        if (MVTextureTarget.PENDING_VIEW_LAYER >= 0) {
            return original.call(instance, VK10.VK_IMAGE_VIEW_TYPE_2D);
        }
        return original.call(instance,
            MultiviewHelper.isMultiviewTexture(texture) ? VK10.VK_IMAGE_VIEW_TYPE_2D_ARRAY : viewType);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkImageSubresourceRange;layerCount(I)Lorg/lwjgl/vulkan/VkImageSubresourceRange;"), require = 1)
    private VkImageSubresourceRange vivecraft$multiviewLayers(
        VkImageSubresourceRange instance, int layerCount, Operation<VkImageSubresourceRange> original,
        @Local(argsOnly = true) VulkanGpuTexture texture)
    {
        if (MVTextureTarget.PENDING_VIEW_LAYER >= 0) {
            return original.call(instance, 1);
        }
        return original.call(instance,
            MultiviewHelper.isMultiviewTexture(texture) ? texture.getDepthOrLayers() : layerCount);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkImageViewCreateInfo;format(I)Lorg/lwjgl/vulkan/VkImageViewCreateInfo;"), require = 1)
    private VkImageViewCreateInfo vivecraft$srgbView(
        VkImageViewCreateInfo instance, int format, Operation<VkImageViewCreateInfo> original)
    {
        return original.call(instance,
            ExternalVulkanRenderTarget.PENDING_SRGB_VIEW ? VK10.VK_FORMAT_R8G8B8A8_SRGB : format);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkImageSubresourceRange;baseArrayLayer(I)Lorg/lwjgl/vulkan/VkImageSubresourceRange;"), require = 1)
    private VkImageSubresourceRange vivecraft$singleLayer(
        VkImageSubresourceRange instance, int baseArrayLayer, Operation<VkImageSubresourceRange> original)
    {
        return original.call(instance,
            MVTextureTarget.PENDING_VIEW_LAYER >= 0 ? MVTextureTarget.PENDING_VIEW_LAYER : baseArrayLayer);
    }
}
