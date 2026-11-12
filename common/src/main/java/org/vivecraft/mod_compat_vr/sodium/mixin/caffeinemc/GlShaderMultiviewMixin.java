package org.vivecraft.mod_compat_vr.sodium.mixin.caffeinemc;

import net.caffeinemc.mods.sodium.client.gl.shader.GlShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;

@Mixin(value = GlShader.class, remap = false)
public class GlShaderMultiviewMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gl/shader/ShaderWorkarounds;safeShaderSource(ILjava/lang/CharSequence;)V"), index = 1)
    private CharSequence vivecraft$multiviewSource(CharSequence source) {
        if (!MultiviewHelper.COMPILING) return source;
        String src = source.toString();
        if (!MultiviewHelper.isVertexShader(src)) return source;
        return MultiviewHelper.rewriteVertexShader(src, "u_ProjectionMatrix", "u_ModelViewMatrix");
    }
}
