package org.vivecraft.mod_compat_vr.sodium.mixin.jellysquid;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
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

    @Shadow
    private GlProgram<ChunkShaderInterface> createShader(String path, ChunkShaderOptions options) {
        return null;
    }

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
            MultiviewHelper.registerProgram(program.handle());
        }
        cir.setReturnValue(program);
    }

    @Inject(method = "delete", at = @At("HEAD"))
    private void vivecraft$deleteMultiviewPrograms(CommandList commandList, CallbackInfo ci) {
        for (GlProgram<ChunkShaderInterface> program : this.vivecraft$multiviewPrograms.values()) {
            MultiviewHelper.unregisterProgram(program.handle());
            program.delete();
        }
        this.vivecraft$multiviewPrograms.clear();
    }
}
