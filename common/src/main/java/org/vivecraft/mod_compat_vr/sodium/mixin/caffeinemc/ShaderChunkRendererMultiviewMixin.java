package org.vivecraft.mod_compat_vr.sodium.mixin.caffeinemc;

import com.mojang.blaze3d.textures.GpuSampler;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = ShaderChunkRenderer.class, remap = false)
public abstract class ShaderChunkRendererMultiviewMixin {

    @Unique
    private final Map<ChunkShaderOptions, GlProgram<ChunkShaderInterface>> vivecraft$multiviewPrograms = new HashMap<>();

    @Unique
    private final IntSet vivecraft$multiviewProgramIds = new IntOpenHashSet();

    @Shadow
    protected GlProgram<ChunkShaderInterface> activeProgram;

    @Shadow
    private GlProgram<ChunkShaderInterface> createShader(String path, ChunkShaderOptions options) {
        return null;
    }

    /**
     * use a separate set of programs with multiview vertex shaders while multiview is active
     */
    @Inject(method = "compileProgram", at = @At("HEAD"), cancellable = true)
    private void vivecraft$multiviewProgram(
        ChunkShaderOptions options, CallbackInfoReturnable<GlProgram<ChunkShaderInterface>> cir)
    {
        if (!MultiviewHelper.ACTIVE) return;
        GlProgram<ChunkShaderInterface> program = this.vivecraft$multiviewPrograms.get(options);
        if (program == null) {
            MultiviewHelper.COMPILING = true;
            try {
                program = this.createShader("blocks/block_layer_opaque", options);
            } finally {
                MultiviewHelper.COMPILING = false;
            }
            this.vivecraft$multiviewPrograms.put(options, program);
            this.vivecraft$multiviewProgramIds.add(program.handle());
            MultiviewHelper.registerProgram(program.handle());
        }
        cir.setReturnValue(program);
    }

    /**
     * the program is bound at this point, upload the eye matrices
     */
    @Inject(method = "begin", at = @At("TAIL"))
    private void vivecraft$uploadMultiviewUniforms(
        TerrainRenderPass pass, FogParameters parameters, GpuSampler terrainSampler, CallbackInfo ci)
    {
        if (!MultiviewHelper.ACTIVE) return;
        // activeProgram is bound at this point
        MultiviewHelper.uploadEyeUniforms(this.activeProgram.handle());
    }

    @Inject(method = "delete", at = @At("HEAD"))
    private void vivecraft$deleteMultiviewPrograms(CommandList commandList, CallbackInfo ci) {
        for (int program : this.vivecraft$multiviewProgramIds) {
            MultiviewHelper.unregisterProgram(program);
        }
        this.vivecraft$multiviewProgramIds.clear();
        this.vivecraft$multiviewPrograms.values().forEach(GlProgram::delete);
        this.vivecraft$multiviewPrograms.clear();
    }
}
