package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.OVRMultiview;

import java.nio.IntBuffer;

public class MVTextureTarget extends RenderTarget {

    public static final int VIEWS = 2;

    private final String name;
    private final int depthInternalFormat;

    public MVTextureTarget(String name, int width, int height, int depthInternalFormat) {
        super(depthInternalFormat != 0);
        this.name = name;
        this.depthInternalFormat = depthInternalFormat;
        RenderSystem.assertOnRenderThreadOrInit();
        this.viewWidth = width;
        this.viewHeight = height;
        this.resize(width, height, Minecraft.ON_OSX);
        this.setClearColor(0, 0, 0, 0);
    }

    public static boolean hasStencil(int internalFormat) {
        return internalFormat == GL30.GL_DEPTH24_STENCIL8 || internalFormat == GL30.GL_DEPTH32F_STENCIL8;
    }

    @Override
    public void createBuffers(int width, int height, boolean clearError) {
        RenderSystem.assertOnRenderThreadOrInit();
        int maxSize = RenderSystem.maxSupportedTextureSize();
        if (width <= 0 || width > maxSize || height <= 0 || height > maxSize) {
            throw new IllegalArgumentException(
                "Window " + width + "x" + height + " size out of bounds (max. size: " + maxSize + ")");
        }
        this.viewWidth = width;
        this.viewHeight = height;
        this.width = width;
        this.height = height;
        this.frameBufferId = GlStateManager.glGenFramebuffers();

        this.colorTextureId = TextureUtil.generateTextureId();
        GL11.glBindTexture(GL33.GL_TEXTURE_2D_ARRAY, this.colorTextureId);
        GL11.glTexParameteri(GL33.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL33.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexImage3D(GL33.GL_TEXTURE_2D_ARRAY, 0, GL11.GL_RGBA8, width, height, VIEWS, 0, GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE, (IntBuffer) null);

        boolean stencil = hasStencil(this.depthInternalFormat);
        if (this.useDepth) {
            this.depthBufferId = TextureUtil.generateTextureId();
            GL11.glBindTexture(GL33.GL_TEXTURE_2D_ARRAY, this.depthBufferId);
            GL11.glTexParameteri(GL33.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL33.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL33.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
            GL11.glTexParameteri(GL33.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL33.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
            int format = stencil ? GL30.GL_DEPTH_STENCIL : GL11.GL_DEPTH_COMPONENT;
            int type = switch (this.depthInternalFormat) {
                case GL30.GL_DEPTH32F_STENCIL8 -> GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;
                case GL30.GL_DEPTH24_STENCIL8 -> GL30.GL_UNSIGNED_INT_24_8;
                case GL30.GL_DEPTH_COMPONENT32F -> GL11.GL_FLOAT;
                case GL14.GL_DEPTH_COMPONENT16 -> GL11.GL_UNSIGNED_SHORT;
                default -> GL11.GL_UNSIGNED_INT;
            };
            GL33.glTexImage3D(GL33.GL_TEXTURE_2D_ARRAY, 0, this.depthInternalFormat, width, height, VIEWS, 0,
                format, type, (IntBuffer) null);
        }
        GL11.glBindTexture(GL33.GL_TEXTURE_2D_ARRAY, 0);

        this.setFilterMode(GL11.GL_LINEAR);

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBufferId);
        OVRMultiview.glFramebufferTextureMultiviewOVR(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
            this.colorTextureId, 0, 0, VIEWS);
        if (this.useDepth) {
            OVRMultiview.glFramebufferTextureMultiviewOVR(GL30.GL_FRAMEBUFFER,
                stencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : GL30.GL_DEPTH_ATTACHMENT, this.depthBufferId, 0, 0,
                VIEWS);
        }

        this.checkStatus();
        this.clear(clearError);
        this.unbindRead();
    }

    @Override
    public void setFilterMode(int filterMode) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.filterMode = filterMode;
        GL11.glBindTexture(GL33.GL_TEXTURE_2D_ARRAY, this.colorTextureId);
        GL11.glTexParameteri(GL33.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, filterMode);
        GL11.glTexParameteri(GL33.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, filterMode);
        GL11.glBindTexture(GL33.GL_TEXTURE_2D_ARRAY, 0);
    }

    public boolean hasStencil() {
        return hasStencil(this.depthInternalFormat);
    }

    @Override
    public String toString() {
        return """

            Vivecraft MultiView RenderTarget: %s
            Size: %s x %s
            FB ID: %s
            Tex ID: %s"""
            .formatted(
                this.name,
                this.viewWidth, this.viewHeight,
                this.frameBufferId,
                this.colorTextureId);
    }
}
