package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;
import org.vivecraft.client_vr.MVTextureTarget;
import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client.extensions.LevelRenderStateExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MultiPassTextureTarget;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.extensions.LevelTargetBundleExtension;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.render.helpers.VRPassHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.render.renderstates.VRRenderState;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import javax.annotation.Nullable;
import java.util.Set;

// priority 1010 to inject after iris, for the VrStuffFinal rendering
@Mixin(value = LevelRenderer.class, priority = 1010)
public abstract class LevelRendererVRMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {

    @Unique
    private static final Identifier vivecraft$VR_TRANSPARENCY_POST_CHAIN_ID = Identifier.fromNamespaceAndPath(
        "vivecraft", "vrtransparency");

    @Shadow
    protected abstract void submitHitOutline(
        PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType,
        BlockOutlineRenderState state, int color, float width, boolean afterTerrain);

    @Shadow
    @Final
    private LevelTargetBundle targets;

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @Shadow
    @Final
    private FeatureRenderDispatcher featureRenderDispatcher;

    @Shadow
    protected abstract void finalizeGizmoCollection();

    @Shadow
    private LevelRenderer.FinalizedGizmos finalizedGizmos;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Shadow
    @Final
    private GameRenderer gameRenderer;

    @Inject(method = "resize", at = @At("TAIL"))
    private void vivecraft$reinitVR(CallbackInfo ci) {
        if (VRState.VR_INITIALIZED) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffersMaybe("Resource Reload");
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;createInternal(Ljava/lang/String;Lcom/mojang/blaze3d/resource/ResourceDescriptor;)Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 0))
    private void vivecraft$addVRTargets(
        CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder,
        @Local RenderTargetDescriptor renderTargetDescriptor)
    {
        if (VRState.VR_RUNNING) {
            this.targets.replace(LevelTargetBundleExtension.OCCLUDED_TARGET_ID,
                vivecraft$createTarget(frameGraphBuilder, "vroccluded", renderTargetDescriptor));
            this.targets.replace(LevelTargetBundleExtension.UNOCCLUDED_TARGET_ID,
                vivecraft$createTarget(frameGraphBuilder, "vrunoccluded", renderTargetDescriptor));
            this.targets.replace(LevelTargetBundleExtension.HANDS_TARGET_ID,
                vivecraft$createTarget(frameGraphBuilder, "vrhands", renderTargetDescriptor));
        }
    }

    @Unique
    private static ResourceHandle<RenderTarget> vivecraft$createTarget(
        FrameGraphBuilder frame, String name, ResourceDescriptor<RenderTarget> descriptor)
    {
        if (MultiviewHelper.isLayeredPostActive()) {
            MVTextureTarget target = MultiviewHelper.getExtraTarget(name);
            descriptor.prepare(target);
            return frame.importExternal(name, target);
        }
        return frame.createInternal(name, descriptor);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;createInternal(Ljava/lang/String;Lcom/mojang/blaze3d/resource/ResourceDescriptor;)Lcom/mojang/blaze3d/resource/ResourceHandle;"))
    private <T> ResourceHandle<T> vivecraft$multiviewLevelTargets(
        FrameGraphBuilder instance, String name, ResourceDescriptor<T> descriptor,
        Operation<ResourceHandle<T>> original)
    {
        if (MultiviewHelper.isLayeredPostActive() && descriptor instanceof RenderTargetDescriptor) {
            return (ResourceHandle<T>) vivecraft$createTarget(instance, name, (ResourceDescriptor<RenderTarget>) descriptor);
        }
        return original.call(instance, name, descriptor);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;importExternal(Ljava/lang/String;Ljava/lang/Object;)Lcom/mojang/blaze3d/resource/ResourceHandle;"))
    private <T> ResourceHandle<T> vivecraft$multiviewOutlineTarget(
        FrameGraphBuilder instance, String name, T resource, Operation<ResourceHandle<T>> original)
    {
        if (MultiviewHelper.isLayeredPostActive() && "entity_outline".equals(name)) {
            return original.call(instance, name, (T) MultiviewHelper.getExtraTarget("entity_outline"));
        }
        return original.call(instance, name, resource);
    }

    @Inject(method = "entityOutlineTarget", at = @At("HEAD"), cancellable = true)
    private void vivecraft$multiviewOutlineTargetGetter(CallbackInfoReturnable<RenderTarget> cir) {
        if (MultiviewHelper.isLayeredPostActive()) {
            cir.setReturnValue(MultiviewHelper.getExtraTarget("entity_outline"));
        }
    }

    @Inject(method = "doEntityOutline", at = @At("HEAD"), cancellable = true)
    private void vivecraft$multiviewOutlineComposite(CallbackInfo ci) {
        if (MultiviewHelper.isLayeredPostActive()) {
            ci.cancel();
            if (this.levelRenderState.shouldShowEntityOutlines) {
                MVTextureTarget outline = MultiviewHelper.getExtraTarget("entity_outline");
                MVTextureTarget main = MultiviewHelper.getTarget();
                for (int layer = 0; layer < MVGlTexture.VIEWS; layer++) {
                    RenderTarget mainLayer = main.getLayerTarget(layer);
                    outline.getLayerTarget(layer).blitAndBlendToTexture(mainLayer.getColorTextureView(),
                        mainLayer.getDepthTextureView());
                }
            }
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareChunkRenders(Lorg/joml/Matrix4fc;)Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;"))
    private void vivecraft$addStencilPass(CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder) {
        if (!RenderPassType.isVanilla()) {
            if (ClientDataHolderVR.getInstance().vrSettings.vrUseStencil) {
                FramePass framePass = frameGraphBuilder.addPass("vr_stencil");
                this.targets.main = framePass.readsAndWrites(this.targets.main);
                framePass.executes(() -> {
                    Profiler.get().popPush("stencil");
                    VREffectsHelper.drawEyeStencil();
                });
            }
        }
    }

    @Inject(method = "addMainPass*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;readsAndWrites(Lcom/mojang/blaze3d/resource/ResourceHandle;)Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 0))
    public void vivecraft$markVRTargetsForWrite(CallbackInfo ci, @Local FramePass framePass) {
        if (VRState.VR_RUNNING && this.targets instanceof LevelTargetBundleExtension ext) {
            if (ext.vivecraft$getOccluded() != null) {
                this.targets.replace(LevelTargetBundleExtension.OCCLUDED_TARGET_ID,
                    framePass.readsAndWrites(ext.vivecraft$getOccluded()));
            }
            if (ext.vivecraft$getUnoccluded() != null) {
                this.targets.replace(LevelTargetBundleExtension.UNOCCLUDED_TARGET_ID,
                    framePass.readsAndWrites(ext.vivecraft$getUnoccluded()));
            }
            if (ext.vivecraft$getHands() != null) {
                this.targets.replace(LevelTargetBundleExtension.HANDS_TARGET_ID,
                    framePass.readsAndWrites(ext.vivecraft$getHands()));
            }
        }
    }

    @Inject(method = "submitFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
    private void vivecraft$interactOutlineSolid(
        CallbackInfo ci, @Local(argsOnly = true) LevelRenderState levelRenderState,
        @Local(argsOnly = true) SubmitNodeCollector output, @Local PoseStack poseStack)
    {
        if (RenderPassType.isVanilla()) return;

        Profiler.get().popPush("interact outline");

        BlockOutlineRenderState[] outlines = ((LevelRenderStateExtension) levelRenderState).vivecraft$getInteractOutlineStates();

        for (int c = 0; c < 2; c++) {
            if (outlines[c] != null) {
                this.submitHitOutline(poseStack,
                    output,
                    RenderTypes.lines(),
                    outlines[c],
                    0x66FFFFFF,
                    this.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth,
                    outlines[c].isTranslucent());
            }
        }
    }

    @Inject(method = "lambda$addMainPass$0*", at = @At(value = "CONSTANT", args = "stringValue=renderSolidFeatures"))
    private void vivecraft$clearVrFabulous(CallbackInfo ci) {
        if (RenderPassType.isVanilla() || this.targets.translucent == null) return;
        if (this.targets instanceof LevelTargetBundleExtension ext) {
            if (ext.vivecraft$getOccluded() != null) {
                (ext.vivecraft$getOccluded().get()).copyDepthFrom(this.targets.main.get());
            }
            if (ext.vivecraft$getHands() != null) {
                (ext.vivecraft$getHands().get()).copyDepthFrom(this.targets.main.get());
            }
            // no depth copy for unoccluded
        }
    }

    @Inject(method = "submitFeatures*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
    private void vivecraft$renderVrStuffPart1(
        CallbackInfo ci, @Local(argsOnly = true) LevelRenderState levelRenderState, @Local PoseStack poseStack)
    {
        if (RenderPassType.isVanilla()) return;

        if (this.gameRenderer.gameRenderState().useShaderTransparency()) {
            VREffectsHelper.renderVRFabulous(this.submitNodeStorage, levelRenderState, poseStack);
        } else {
            VREffectsHelper.renderVrFast(this.submitNodeStorage, levelRenderState, poseStack, false);
        }
    }

    // if the gui didn't render yet, render it now.
    // or if shaders are on, and option AFTER_SHADER is selected
    @Inject(method = "render", at = @At("RETURN"))
    private void vivecraft$renderVrStuffFinal(CallbackInfo ci) {
        if (RenderPassType.isVanilla()) return;
        VRRenderState vrState = ((LevelRenderStateExtension) this.levelRenderState).vivecraft$getVRRenderState();

        if (vrState.uiAfterWorld) {
            // re set up modelView, since this is after everything got cleared
            RenderSystem.getModelViewStack().pushMatrix().identity();
            RenderHelper.applyVRModelView(vrState.currentPass, RenderSystem.getModelViewStack());

            VREffectsHelper.renderVrFast(this.submitNodeStorage, this.levelRenderState, new PoseStack(), true);
            // actuallyrender the stuff
            this.featureRenderDispatcher.renderAllFeatures(this.submitNodeStorage);

            RenderSystem.getModelViewStack().popMatrix();
        }
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "com/mojang/blaze3d/pipeline/TextureTarget"))
    private TextureTarget vivecraft$multiPassOutlineTarget(
        String label, int width, int height, boolean useDepth, GpuFormat format, Operation<TextureTarget> original)
    {
        return new MultiPassTextureTarget(label, width, height, useDepth, format);
    }

    @WrapOperation(method = "getTransparencyChain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain vivecraft$vrTransparency(
        ShaderManager instance, Identifier id, Set<Identifier> externalTargets,
        Operation<PostChain> original)
    {
        if (VRState.VR_RUNNING) {
            return original.call(instance, vivecraft$VR_TRANSPARENCY_POST_CHAIN_ID,
                LevelTargetBundleExtension.VR_TARGETS);
        } else {
            return original.call(instance, id, externalTargets);
        }
    }

    @Inject(method = "cloudsTarget", at = @At("HEAD"), cancellable = true)
    private void vivecraft$getCloudsTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null &&
            ClientDataHolderVR.getInstance().menuWorldRenderer.isRendering())
        {
            cir.setReturnValue(null);
        }
    }

    @Unique
    @Override
    public void vivecraft$renderGizmos(
        CameraRenderState cameraState, SubmitNodeStorage output, FeatureRenderDispatcher dispatcher)
    {
        RenderSystem.outputColorTextureOverride = this.gameRenderer.mainRenderTarget.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = this.gameRenderer.mainRenderTarget.getDepthTextureView();

        this.finalizeGizmoCollection();
        // submit
        this.finalizedGizmos.standardPrimitives().submit(output, cameraState, false);
        this.finalizedGizmos.alwaysOnTopPrimitives().submit(output, cameraState, true);

        // render
        try (FeatureRenderDispatcher.PreparedFrame featureFrame = dispatcher.prepareFrame(output)) {
            featureFrame.executeSolid();
            featureFrame.executeTranslucent();
            featureFrame.executeTranslucentAfterTerrain();

            // always on top gizmos
            if (featureFrame.hasAnyAlwaysOnTop()) {
                RenderSystem.getDevice().createCommandEncoder()
                    .clearDepthTexture(this.gameRenderer.mainRenderTarget.getDepthTexture(),
                        0.0);
                featureFrame.executeAlwaysOnTop();
            }
        }
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
    }


    @Unique
    @Override
    @Nullable
    public RenderTarget vivecraft$getHandsTarget() {
        return this.targets instanceof LevelTargetBundleExtension ext && ext.vivecraft$getHands() != null ?
            ext.vivecraft$getHands().get() : null;
    }

    @Unique
    @Override
    @Nullable
    public RenderTarget vivecraft$getVrOccludedTarget() {
        return this.targets instanceof LevelTargetBundleExtension ext && ext.vivecraft$getOccluded() != null ?
            ext.vivecraft$getOccluded().get() : null;
    }

    @Unique
    @Override
    @Nullable
    public RenderTarget vivecraft$getVrUnoccludedTarget() {
        return this.targets instanceof LevelTargetBundleExtension ext && ext.vivecraft$getUnoccluded() != null ?
            ext.vivecraft$getUnoccluded().get() : null;
    }

    @WrapWithCondition(method = "submitFeatures", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"), require = 3)
    private boolean vivecraft$keepStatesForNextEye(java.util.List<?> instance) {
        return !VRPassHelper.EXTRACT_FOR_BOTH_EYES;
    }
}
