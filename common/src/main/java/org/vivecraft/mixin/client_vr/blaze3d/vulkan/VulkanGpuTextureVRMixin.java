package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.ExternalVulkanRenderTarget;

import java.nio.LongBuffer;

@Mixin(VulkanGpuTexture.class)
public class VulkanGpuTextureVRMixin {

    @Unique
    private boolean vivecraft$external;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaCreateImage(JLorg/lwjgl/vulkan/VkImageCreateInfo;Lorg/lwjgl/util/vma/VmaAllocationCreateInfo;Ljava/nio/LongBuffer;Lorg/lwjgl/PointerBuffer;Lorg/lwjgl/util/vma/VmaAllocationInfo;)I"), require = 1)
    private int vivecraft$useExternalImage(
        long allocator, VkImageCreateInfo createInfo, VmaAllocationCreateInfo allocationInfo, LongBuffer image,
        PointerBuffer allocation, VmaAllocationInfo info, Operation<Integer> original)
    {
        long external = ExternalVulkanRenderTarget.PENDING_IMAGE;
        if (external != 0L) {
            this.vivecraft$external = true;
            image.put(0, external);
            allocation.put(0, 0L);
            return 0;
        }
        return original.call(allocator, createInfo, allocationInfo, image, allocation, info);
    }

    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdPipelineBarrier(Lorg/lwjgl/vulkan/VkCommandBuffer;IIILorg/lwjgl/vulkan/VkMemoryBarrier$Buffer;Lorg/lwjgl/vulkan/VkBufferMemoryBarrier$Buffer;Lorg/lwjgl/vulkan/VkImageMemoryBarrier$Buffer;)V"), require = 1)
    private boolean vivecraft$noInitialTransition(
        VkCommandBuffer commandBuffer, int srcStageMask, int dstStageMask, int dependencyFlags,
        VkMemoryBarrier.Buffer memoryBarriers, VkBufferMemoryBarrier.Buffer bufferBarriers,
        VkImageMemoryBarrier.Buffer imageBarriers)
    {
        return !this.vivecraft$external;
    }

    @WrapWithCondition(method = "destroy", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaDestroyImage(JJJ)V"), require = 1)
    private boolean vivecraft$keepExternalImage(long allocator, long image, long allocation) {
        return !this.vivecraft$external;
    }
}
