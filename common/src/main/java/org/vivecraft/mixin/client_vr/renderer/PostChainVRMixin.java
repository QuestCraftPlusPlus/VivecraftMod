package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.MVTextureTarget;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.render.multiview.MVGlTexture;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(PostChain.class)
public abstract class PostChainVRMixin {
    // TODO figure out how to use stencils when applicable, if that is even in here

    @Shadow
    @Final
    private Set<Identifier> externalTargets;

    @Shadow
    public abstract void addToFrame(
        FrameGraphBuilder frame, int screenWidth, int screenHeight, PostChain.TargetBundle providedTargets);

    @Unique
    private boolean vivecraft$addingLayer;

    @Inject(method = "addToFrame", at = @At("HEAD"), cancellable = true)
    private void vivecraft$addPerLayer(
        FrameGraphBuilder frame, int screenWidth, int screenHeight, PostChain.TargetBundle providedTargets,
        CallbackInfo ci)
    {
        if (this.vivecraft$addingLayer || !MultiviewHelper.isLayeredPostActive()) {
            return;
        }

        Map<Identifier, ResourceHandle<RenderTarget>> handles = new HashMap<>();
        Map<Identifier, MVTextureTarget> layered = new HashMap<>();
        for (Identifier id : this.externalTargets) {
            ResourceHandle<RenderTarget> handle = providedTargets.getOrThrow(id);
            handles.put(id, handle);
            if (vivecraft$resolve(handle) instanceof MVTextureTarget target && target.getLayerTarget(0) != null) {
                layered.put(id, target);
            }
        }
        if (layered.isEmpty() || layered.size() != this.externalTargets.size()) {
            return;
        }
        ci.cancel();

        FramePass split = frame.addPass("vivecraft multiview split");
        LayerBundle[] bundles = new LayerBundle[MVGlTexture.VIEWS];
        for (int layer = 0; layer < bundles.length; layer++) {
            bundles[layer] = new LayerBundle();
        }
        for (Map.Entry<Identifier, MVTextureTarget> entry : layered.entrySet()) {
            split.reads(handles.get(entry.getKey()));
            for (int layer = 0; layer < bundles.length; layer++) {
                ResourceHandle<RenderTarget> layerHandle = frame.importExternal(
                    entry.getKey() + " layer " + layer, entry.getValue().getLayerTarget(layer));
                bundles[layer].replace(entry.getKey(), split.readsAndWrites(layerHandle));
            }
        }
        split.executes(() -> {});

        this.vivecraft$addingLayer = true;
        try {
            for (LayerBundle bundle : bundles) {
                this.addToFrame(frame, screenWidth, screenHeight, bundle);
            }
        } finally {
            this.vivecraft$addingLayer = false;
        }

        FramePass join = frame.addPass("vivecraft multiview join");
        for (LayerBundle bundle : bundles) {
            for (Identifier id : layered.keySet()) {
                join.reads(bundle.getOrThrow(id));
            }
        }
        for (Identifier id : layered.keySet()) {
            providedTargets.replace(id, join.readsAndWrites(handles.get(id)));
        }
        join.executes(() -> {});
    }

    @Unique
    private static @Nullable RenderTarget vivecraft$resolve(ResourceHandle<RenderTarget> handle) {
        try {
            return handle.get();
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Unique
    private static class LayerBundle implements PostChain.TargetBundle {
        private final Map<Identifier, ResourceHandle<RenderTarget>> targets = new HashMap<>();

        @Override
        public void replace(Identifier id, ResourceHandle<RenderTarget> handle) {
            this.targets.put(id, handle);
        }

        @Override
        public @Nullable ResourceHandle<RenderTarget> get(Identifier id) {
            return this.targets.get(id);
        }
    }
}
