package org.vivecraft.mixin.client_vr.blaze3d.opengl;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.vivecraft.client_vr.VRState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.extensions.GlCommandEncoderExtension;
import org.vivecraft.client_vr.extensions.GlRenderPassExtension;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;
import org.vivecraft.client_vr.render.multiview.MVGlTextureView;

import java.util.Collection;
import java.util.Optional;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderVRMixin implements GlCommandEncoderExtension {
    @Shadow
    private @Nullable GlProgram lastProgram;

    @Override
    @Unique
    public void vivecraft$resetLastProgram() {
        this.lastProgram = null;
    }

    @Inject(method = "createRenderPass", at = @At("RETURN"))
    private void vivecraft$markMultiviewPass(
        RenderPassDescriptor descriptor, CallbackInfoReturnable<RenderPassBackend> cir)
    {
        for (RenderPassDescriptor.Attachment<Optional<Vector4fc>> attachment : descriptor.colorAttachments()) {
            if (attachment != null && attachment.textureView() instanceof MVGlTextureView) {
                ((GlRenderPassExtension) cir.getReturnValue()).vivecraft$setMultiview(true);
                break;
            }
        }
    }

    @WrapWithCondition(method = {"submitRenderPass", "clearColorTexture", "clearColorAndDepthTextures", "clearDepthTexture"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_glBindFramebuffer(II)V"), require = 5)
    private boolean vivecraft$keepFramebufferBound(int target, int framebuffer) {
        return framebuffer != 0 || !VRState.VR_RUNNING;
    }

    @Inject(method = "trySetup", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_glUseProgram(I)V", shift = At.Shift.AFTER))
    private void vivecraft$uploadMultiviewUniforms(
        GlRenderPass renderPass, Collection<String> uniforms, CallbackInfoReturnable<Boolean> cir)
    {
        if (((GlRenderPassExtension) renderPass).vivecraft$isMultiview()) {
            MultiviewHelper.uploadEyeUniforms(renderPass.pipeline.program());
        }
    }

    @Inject(method = "clearColorTexture", at = @At("HEAD"), cancellable = true)
    private void vivecraft$clearMultiviewColor(GpuTexture colorTexture, Vector4fc clearColor, CallbackInfo ci) {
        if (colorTexture instanceof MVGlTexture mvTexture) {
            mvTexture.clearColor(clearColor.get(0), clearColor.get(1), clearColor.get(2), clearColor.get(3));
            ci.cancel();
        }
    }

    @Inject(method = "clearDepthTexture", at = @At("HEAD"), cancellable = true)
    private void vivecraft$clearMultiviewDepth(GpuTexture texture, double depth, CallbackInfo ci) {
        if (texture instanceof MVGlTexture mvTexture) {
            mvTexture.clearDepth(depth);
            ci.cancel();
        }
    }
}
