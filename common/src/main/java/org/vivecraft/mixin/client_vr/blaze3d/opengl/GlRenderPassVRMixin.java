package org.vivecraft.mixin.client_vr.blaze3d.opengl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.extensions.GlDeviceExtension;
import org.vivecraft.client_vr.extensions.GlRenderPassExtension;

@Mixin(GlRenderPass.class)
public class GlRenderPassVRMixin implements GlRenderPassExtension {

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

    @WrapOperation(method = "setPipeline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlDevice;getOrCompilePipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)Lcom/mojang/blaze3d/opengl/GlRenderPipeline;"))
    private GlRenderPipeline vivecraft$multiviewPipeline(
        GlDevice device, RenderPipeline pipeline, Operation<GlRenderPipeline> original)
    {
        if (this.vivecraft$multiview) {
            GlRenderPipeline multiview = ((GlDeviceExtension) device).vivecraft$getOrCompileMultiviewPipeline(
                pipeline);
            if (multiview.isValid()) {
                return multiview;
            }
        }
        return original.call(device, pipeline);
    }
}
