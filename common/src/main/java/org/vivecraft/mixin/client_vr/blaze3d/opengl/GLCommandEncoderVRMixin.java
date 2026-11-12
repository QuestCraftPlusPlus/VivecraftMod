package org.vivecraft.mixin.client_vr.blaze3d.opengl;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GlCommandEncoderExtension;
import org.vivecraft.client_vr.extensions.GlRenderPassExtension;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;
import org.vivecraft.client_vr.render.multiview.MVGlTextureView;

import java.util.Collection;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;

@Mixin(GlCommandEncoder.class)
public class GLCommandEncoderVRMixin implements GlCommandEncoderExtension {

    @Shadow
    private @Nullable GlProgram lastProgram;

    @ModifyExpressionValue(method = "applyPipelineState", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline;getDepthTestFunction()Lcom/mojang/blaze3d/platform/DepthTestFunction;", ordinal = 0, remap = false), remap = true)
    private DepthTestFunction vivecraft$depthAlways(
        DepthTestFunction depthTest, @Local(argsOnly = true) RenderPipeline renderPipeline)
    {
        // This is just here because there is no specific ALWAYS_DEPTH_TEST
        return (VRState.VR_RUNNING && VRShaders.DEPTH_ALWAYS_PIPELINES.contains(renderPipeline)) ? null : depthTest;
    }

    @Override
    @Unique
    public void vivecraft$resetLastProgram() {
        this.lastProgram = null;
    }

    @Inject(method = "createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalInt;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPass;", at = @At("RETURN"))
    private void vivecraft$markMultiviewPass(
        Supplier<String> label, GpuTextureView colorTexture, OptionalInt clearColor,
        @Nullable GpuTextureView depthTexture, OptionalDouble clearDepth, CallbackInfoReturnable<RenderPass> cir)
    {
        if (colorTexture instanceof MVGlTextureView) {
            ((GlRenderPassExtension) cir.getReturnValue()).vivecraft$setMultiview(true);
        }
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
    private void vivecraft$clearMultiviewColor(GpuTexture texture, int color, CallbackInfo ci) {
        if (texture instanceof MVGlTexture mvTexture) {
            mvTexture.clearColor(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color),
                ARGB.alphaFloat(color));
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
