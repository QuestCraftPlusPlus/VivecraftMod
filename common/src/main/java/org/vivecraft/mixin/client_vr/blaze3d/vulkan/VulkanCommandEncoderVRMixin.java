package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanCommandEncoder;
import com.mojang.blaze3d.vulkan.VulkanGpuTextureView;
import com.mojang.blaze3d.vulkan.VulkanRenderPass;
import org.jspecify.annotations.Nullable;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.extensions.GlRenderPassExtension;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;

import java.util.Arrays;
import java.util.Optional;

@Mixin(VulkanCommandEncoder.class)
public class VulkanCommandEncoderVRMixin {

    @Shadow
    private @Nullable VkCommandBuffer currentCommandBuffer;

    @Unique
    private boolean vivecraft$renderingOpen;

    @Unique
    private long @Nullable [] vivecraft$openKey;

    @Unique
    private boolean vivecraft$continueRendering;

    @Unique
    private boolean vivecraft$inPassSetup;

    @Unique
    private void vivecraft$endOpenRendering() {
        if (this.vivecraft$renderingOpen && this.currentCommandBuffer != null) {
            this.vivecraft$renderingOpen = false;
            this.vivecraft$openKey = null;
            KHRDynamicRendering.vkCmdEndRenderingKHR(this.currentCommandBuffer);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VulkanCommandEncoder.memoryBarrier(this.currentCommandBuffer, stack);
            }
        }
        this.vivecraft$renderingOpen = false;
        this.vivecraft$openKey = null;
    }

    @Inject(method = "commandBuffer", at = @At("HEAD"), require = 1)
    private void vivecraft$endRenderingBeforeCommands(CallbackInfoReturnable<VkCommandBuffer> cir) {
        if (!this.vivecraft$inPassSetup) {
            this.vivecraft$endOpenRendering();
        }
    }

    @Inject(method = "endCommandBuffer", at = @At("HEAD"), require = 1)
    private void vivecraft$endRenderingBeforeEnd(CallbackInfo ci) {
        this.vivecraft$endOpenRendering();
    }

    @Inject(method = "createRenderPass", at = @At("HEAD"))
    private void vivecraft$checkContinueRendering(
        RenderPassDescriptor descriptor, CallbackInfoReturnable<RenderPassBackend> cir)
    {
        long[] key = vivecraft$passKey(descriptor);
        this.vivecraft$continueRendering = this.vivecraft$renderingOpen && !vivecraft$hasClear(descriptor) &&
            Arrays.equals(key, this.vivecraft$openKey);
        if (!this.vivecraft$continueRendering) {
            this.vivecraft$endOpenRendering();
        }
        this.vivecraft$openKey = key;
        this.vivecraft$inPassSetup = true;
    }

    @WrapOperation(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRDynamicRendering;vkCmdBeginRenderingKHR(Lorg/lwjgl/vulkan/VkCommandBuffer;Lorg/lwjgl/vulkan/VkRenderingInfo;)V"), require = 1)
    private void vivecraft$continueRendering(
        VkCommandBuffer commandBuffer, VkRenderingInfo renderingInfo, Operation<Void> original)
    {
        if (this.vivecraft$continueRendering) {
            this.vivecraft$renderingOpen = false;
        } else {
            original.call(commandBuffer, renderingInfo);
        }
    }

    @Inject(method = "createRenderPass", at = @At("RETURN"))
    private void vivecraft$endPassSetup(CallbackInfoReturnable<RenderPassBackend> cir) {
        this.vivecraft$inPassSetup = false;
        this.vivecraft$continueRendering = false;
    }

    @Inject(method = "submitRenderPass", at = @At("HEAD"))
    private void vivecraft$startPassSubmit(CallbackInfo ci) {
        this.vivecraft$inPassSetup = true;
    }

    @WrapOperation(method = "submitRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRDynamicRendering;vkCmdEndRenderingKHR(Lorg/lwjgl/vulkan/VkCommandBuffer;)V"), require = 1)
    private void vivecraft$keepRenderingOpen(VkCommandBuffer commandBuffer, Operation<Void> original) {
        this.vivecraft$renderingOpen = true;
    }

    @WrapOperation(method = "submitRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanCommandEncoder;memoryBarrier(Lorg/lwjgl/system/MemoryStack;)V"), require = 1)
    private void vivecraft$deferBarrier(VulkanCommandEncoder instance, MemoryStack stack, Operation<Void> original) {
        if (!this.vivecraft$renderingOpen) {
            original.call(instance, stack);
        }
    }

    @Inject(method = "submitRenderPass", at = @At("RETURN"))
    private void vivecraft$endPassSubmit(CallbackInfo ci) {
        this.vivecraft$inPassSetup = false;
    }

    @Unique
    private static long[] vivecraft$passKey(RenderPassDescriptor descriptor) {
        int colors = descriptor.colorAttachments().size();
        long[] key = new long[colors + 6];
        for (int i = 0; i < colors; i++) {
            RenderPassDescriptor.Attachment<Optional<Vector4fc>> attachment = descriptor.colorAttachments().get(i);
            key[i] = attachment == null ? 0L : ((VulkanGpuTextureView) attachment.textureView()).vkImageView();
        }
        key[colors] = descriptor.depthAttachment() == null ? 0L :
            ((VulkanGpuTextureView) descriptor.depthAttachment().textureView()).vkImageView();
        if (descriptor.renderArea != null) {
            key[colors + 1] = descriptor.renderArea.x();
            key[colors + 2] = descriptor.renderArea.y();
            key[colors + 3] = descriptor.renderArea.width();
            key[colors + 4] = descriptor.renderArea.height();
        }
        key[colors + 5] = vivecraft$isMultiview(descriptor) ? vivecraft$viewMask() : 0;
        return key;
    }

    @Unique
    private static boolean vivecraft$hasClear(RenderPassDescriptor descriptor) {
        for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> attachment : descriptor.colorAttachments()) {
            if (attachment != null && attachment.clearValue().isPresent()) {
                return true;
            }
        }
        return descriptor.depthAttachment() != null && descriptor.depthAttachment().clearValue().isPresent();
    }

    @WrapOperation(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkRenderingInfo;viewMask(I)Lorg/lwjgl/vulkan/VkRenderingInfo;"), require = 1)
    private VkRenderingInfo vivecraft$multiviewViewMask(
        VkRenderingInfo instance, int viewMask, Operation<VkRenderingInfo> original,
        @Local(argsOnly = true) RenderPassDescriptor descriptor)
    {
        return original.call(instance, vivecraft$isMultiview(descriptor) ? vivecraft$viewMask() : viewMask);
    }

    @Inject(method = "createRenderPass", at = @At("RETURN"))
    private void vivecraft$markMultiviewPass(
        RenderPassDescriptor descriptor, CallbackInfoReturnable<RenderPassBackend> cir)
    {
        if (vivecraft$isMultiview(descriptor)) {
            VulkanRenderPass renderPass = (VulkanRenderPass) cir.getReturnValue();
            ((GlRenderPassExtension) renderPass).vivecraft$setMultiview(true);
            renderPass.setUniform(MultiviewHelper.UNIFORM_BLOCK, MultiviewHelper.getEyeUniformBuffer().slice());
        }
    }

    @WrapOperation(method = {"clearColorTextureUnsynced", "clearDepthTextureUnsynced"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkImageSubresourceRange;layerCount(I)Lorg/lwjgl/vulkan/VkImageSubresourceRange;"), require = 2)
    private VkImageSubresourceRange vivecraft$clearAllLayers(
        VkImageSubresourceRange instance, int layerCount, Operation<VkImageSubresourceRange> original,
        @Local(argsOnly = true) GpuTexture texture)
    {
        return original.call(instance,
            MultiviewHelper.isMultiviewTexture(texture) ? texture.getDepthOrLayers() : layerCount);
    }

    @WrapOperation(method = "copyTextureToTexture", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkImageSubresourceLayers;layerCount(I)Lorg/lwjgl/vulkan/VkImageSubresourceLayers;"), require = 1)
    private org.lwjgl.vulkan.VkImageSubresourceLayers vivecraft$copyAllLayers(
        org.lwjgl.vulkan.VkImageSubresourceLayers instance, int layerCount,
        Operation<org.lwjgl.vulkan.VkImageSubresourceLayers> original,
        @Local(argsOnly = true, ordinal = 0) GpuTexture source, @Local(argsOnly = true, ordinal = 1) GpuTexture destination)
    {
        if (MultiviewHelper.isMultiviewTexture(source) && MultiviewHelper.isMultiviewTexture(destination)) {
            return original.call(instance, Math.min(source.getDepthOrLayers(), destination.getDepthOrLayers()));
        }
        return original.call(instance, layerCount);
    }

    @Unique
    private static boolean vivecraft$isMultiview(RenderPassDescriptor descriptor) {
        for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> attachment : descriptor.colorAttachments()) {
            if (attachment != null && MultiviewHelper.isMultiviewView(attachment.textureView())) {
                return true;
            }
        }
        return descriptor.depthAttachment() != null &&
            MultiviewHelper.isMultiviewView(descriptor.depthAttachment().textureView());
    }

    @Unique
    private static int vivecraft$viewMask() {
        return (1 << MVGlTexture.VIEWS) - 1;
    }
}
