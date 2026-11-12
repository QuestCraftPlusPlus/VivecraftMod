package org.vivecraft.mixin.client_vr.blaze3d.opengl;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.FrameBufferAttachment;
import com.mojang.blaze3d.opengl.FrameBufferCache;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTextureView;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.render.helpers.graphics.opengl.ExternalGlTexture;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;
import org.vivecraft.client_vr.render.multiview.MVGlTextureView;

import java.nio.IntBuffer;
import java.util.List;

@Mixin(FrameBufferCache.class)
public class FrameBufferCacheVRMixin {

    @Inject(method = "createFbo", at = @At("HEAD"), cancellable = true, require = 1)
    private void vivecraft$createMultiviewFbo(
        FrameBufferCache.CacheKey key, DirectStateAccess dsa, List<@Nullable FrameBufferAttachment> colorAttachments,
        @Nullable FrameBufferAttachment depthAttachment, CallbackInfoReturnable<Integer> cir)
    {
        boolean isMultiview = vivecraft$isMultiview(depthAttachment);
        for (FrameBufferAttachment attachment : colorAttachments) {
            isMultiview |= vivecraft$isMultiview(attachment);
        }
        if (!isMultiview) return;

        int fbo = dsa.createFrameBufferObject();
        int[] colorIds = new int[colorAttachments.size()];
        for (int i = 0; i < colorIds.length; i++) {
            FrameBufferAttachment attachment = colorAttachments.get(i);
            if (attachment != null) {
                if (!vivecraft$isMultiview(attachment)) {
                    throw new IllegalStateException("Can't mix multiview and regular color attachments");
                }
                colorIds[i] = attachment.glId();
                attachment.addAssociatedFbo(key);
            }
        }

        int depthId = 0;
        if (depthAttachment != null) {
            if (!vivecraft$isMultiview(depthAttachment)) {
                throw new IllegalStateException("Can't mix multiview color with a regular depth attachment");
            }
            depthId = depthAttachment.glId();
            depthAttachment.addAssociatedFbo(key);
        }

        MultiviewHelper.attachMultiview(fbo, colorIds, depthId, MVGlTexture.VIEWS);
        cir.setReturnValue(fbo);
    }

    @Inject(method = "createFbo", at = @At("HEAD"), cancellable = true, require = 1)
    private void vivecraft$createLayerFbo(
        FrameBufferCache.CacheKey key, DirectStateAccess dsa, List<@Nullable FrameBufferAttachment> colorAttachments,
        @Nullable FrameBufferAttachment depthAttachment, CallbackInfoReturnable<Integer> cir)
    {
        if (cir.isCancelled()) return;
        boolean hasLayer = false;
        for (FrameBufferAttachment attachment : colorAttachments) {
            hasLayer |= vivecraft$getLayer(attachment) >= 0;
        }
        if (!hasLayer) return;

        int fbo = dsa.createFrameBufferObject();
        int previous = GlStateManager.getFrameBuffer(GL30.GL_DRAW_FRAMEBUFFER);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer drawBuffers = stack.mallocInt(Math.max(1, colorAttachments.size()));
            drawBuffers.put(0, GL11.GL_NONE);
            for (int i = 0; i < colorAttachments.size(); i++) {
                FrameBufferAttachment attachment = colorAttachments.get(i);
                if (attachment == null) {
                    drawBuffers.put(i, GL11.GL_NONE);
                    continue;
                }
                int layer = vivecraft$getLayer(attachment);
                if (layer >= 0) {
                    GL30.glFramebufferTextureLayer(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i,
                        attachment.glId(), attachment.fboMipLevel(), layer);
                } else {
                    GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i,
                        GL11.GL_TEXTURE_2D, attachment.glId(), attachment.fboMipLevel());
                }
                drawBuffers.put(i, GL30.GL_COLOR_ATTACHMENT0 + i);
                attachment.addAssociatedFbo(key);
            }
            GL20.glDrawBuffers(drawBuffers);
        }
        if (depthAttachment != null) {
            GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
                depthAttachment.glId(), depthAttachment.fboMipLevel());
            depthAttachment.addAssociatedFbo(key);
        }
        int status = GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previous);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Layer framebuffer incomplete: 0x" + Integer.toHexString(status));
        }
        cir.setReturnValue(fbo);
    }

    private static boolean vivecraft$isMultiview(@Nullable FrameBufferAttachment attachment) {
        return attachment instanceof MVGlTexture || attachment instanceof MVGlTextureView;
    }

    private static int vivecraft$getLayer(@Nullable FrameBufferAttachment attachment) {
        if (attachment instanceof ExternalGlTexture texture) {
            return texture.layer;
        } else if (attachment instanceof GlTextureView view && view.texture() instanceof ExternalGlTexture texture) {
            return texture.layer;
        }
        return -1;
    }
}
