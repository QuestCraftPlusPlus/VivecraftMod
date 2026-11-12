package org.vivecraft.mixin.client.blaze3d.opengl;

import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.GlDeviceExtension;
import org.vivecraft.client_vr.render.helpers.MultiviewHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(GlDevice.class)
public class GlDeviceMixin implements GlDeviceExtension {
    @Shadow
    @Final
    private GlDebugLabel debugLabels;

    @Shadow
    @Final
    private ShaderSource defaultShaderSource;

    @Unique
    private final Map<RenderPipeline, GlRenderPipeline> vivecraft$multiviewPipelines = new IdentityHashMap<>();

    @Unique
    private final Map<GlDevice.ShaderCompilationKey, GlShaderModule> vivecraft$multiviewShaders = new HashMap<>();

    @Shadow
    private GlRenderPipeline compilePipeline(RenderPipeline pipeline, ShaderSource shaderSource) {
        return null;
    }

    @Shadow
    private GlShaderModule compileShader(GlDevice.ShaderCompilationKey key, ShaderSource source) {
        return null;
    }

    /**
     * copy of {@link GlDevice#createTexture(String, int, TextureFormat, int, int, int, int)} but with a fixes texture id
     */
    @Override
    public GpuTexture vivecraft$createFixedIdTexture(
        @Nullable Supplier<String> labelSupplier, int usageFlags, TextureFormat textureFormat, int width,
        int height, int depthLayers, int mipmapLevels, int texId)
    {
        if (mipmapLevels < 1) {
            throw new IllegalArgumentException("mipLevels must be at least 1");
        } else if (depthLayers < 1) {
            throw new IllegalArgumentException("depthOrLayers must be at least 1");
        } else {
            if ((usageFlags & GpuTexture.USAGE_CUBEMAP_COMPATIBLE) != 0) {
                throw new UnsupportedOperationException("CubeMap textures are not supported");
            } else if (depthLayers > 1) {
                throw new UnsupportedOperationException("Array or 3D textures are not supported");
            }

            GlStateManager.clearGlErrors();
            String label = this.debugLabels.exists() && labelSupplier != null ? labelSupplier.get() : null;
            if (label == null) {
                label = String.valueOf(texId);
            }

            GlStateManager._bindTexture(texId);
            GlStateManager._texParameter(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAX_LEVEL, mipmapLevels - 1);
            GlStateManager._texParameter(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_LOD, 0);
            GlStateManager._texParameter(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAX_LOD, mipmapLevels - 1);
            if (textureFormat.hasDepthAspect()) {
                GlStateManager._texParameter(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_NONE);
            }

            for (int m = 0; m < mipmapLevels; m++) {
                GlStateManager._texImage2D(GL30C.GL_TEXTURE_2D, m, GlConst.toGlInternalId(textureFormat), width >> m,
                    height >> m, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null);
            }

            int error = GlStateManager._getError();
            if (error == GL30C.GL_OUT_OF_MEMORY) {
                throw new GpuOutOfMemoryException(
                    "Could not allocate texture of " + width + "x" + height + " for " + label);
            } else if (error != 0) {
                throw new IllegalStateException("OpenGL error " + error);
            } else {
                GlTexture glTexture = new GlTexture(usageFlags, label, textureFormat, width, height, depthLayers,
                    mipmapLevels, texId);
                this.debugLabels.applyLabel(glTexture);
                return glTexture;
            }
        }
    }

    @Override
    public GlRenderPipeline vivecraft$getOrCompileMultiviewPipeline(RenderPipeline pipeline) {
        GlRenderPipeline compiled = this.vivecraft$multiviewPipelines.get(pipeline);
        if (compiled == null) {
            MultiviewHelper.COMPILING = true;
            try {
                compiled = this.compilePipeline(pipeline, this.defaultShaderSource);
            } finally {
                MultiviewHelper.COMPILING = false;
            }
            this.vivecraft$multiviewPipelines.put(pipeline, compiled);
            if (compiled.isValid()) {
                MultiviewHelper.registerProgram(compiled.program());
            } else {
                MultiviewHelper.compileFailed(pipeline.getLocation().toString());
            }
        }
        return compiled;
    }

    @WrapOperation(method = "compileProgram", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlDevice;getOrCompileShader(Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/shaders/ShaderType;Lnet/minecraft/client/renderer/ShaderDefines;Lcom/mojang/blaze3d/shaders/ShaderSource;)Lcom/mojang/blaze3d/opengl/GlShaderModule;"))
    private GlShaderModule vivecraft$multiviewVertexShader(
        GlDevice instance, Identifier id, ShaderType type, ShaderDefines defines, ShaderSource source,
        Operation<GlShaderModule> original)
    {
        if (!MultiviewHelper.COMPILING || type != ShaderType.VERTEX) {
            return original.call(instance, id, type, defines, source);
        }
        return this.vivecraft$multiviewShaders.computeIfAbsent(new GlDevice.ShaderCompilationKey(id, type, defines),
            key -> this.compileShader(key, (shaderId, shaderType) -> {
                String src = source.get(shaderId, shaderType);
                if (src == null) {
                    return null;
                }
                try {
                    return MultiviewHelper.rewriteVertexShader(src, "ProjMat", "ModelViewMat");
                } catch (IllegalStateException e) {
                    VRSettings.LOGGER.error("Vivecraft: can't make multiview variant of shader {}: {}", shaderId,
                        e.getMessage());
                    // makes compileShader return INVALID_SHADER
                    return null;
                }
            }));
    }

    @Inject(method = "clearPipelineCache", at = @At("HEAD"))
    private void vivecraft$clearMultiviewPipelines(CallbackInfo ci) {
        for (GlRenderPipeline pipeline : this.vivecraft$multiviewPipelines.values()) {
            if (pipeline.isValid()) {
                MultiviewHelper.unregisterProgram(pipeline.program());
                pipeline.program().close();
            }
        }
        this.vivecraft$multiviewPipelines.clear();
        for (GlShaderModule shader : this.vivecraft$multiviewShaders.values()) {
            if (shader != GlShaderModule.INVALID_SHADER) {
                shader.close();
            }
        }
        this.vivecraft$multiviewShaders.clear();
        MultiviewHelper.onPipelinesCleared();
    }
}
