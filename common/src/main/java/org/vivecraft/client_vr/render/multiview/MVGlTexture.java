package org.vivecraft.client_vr.render.multiview;

import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.OVRMultiview;

import java.nio.ByteBuffer;

public class MVGlTexture extends GlTexture {

    public static final int VIEWS = 2;

    private final Int2IntMap mvFbos = new Int2IntArrayMap();
    private int clearDepthFbo = -1;
    private int clearColorFbo = -1;
    private int readFbo = -1;

    private MVGlTexture(int usage, String label, TextureFormat format, int width, int height, int id) {
        super(usage, label, format, width, height, 1, 1, id);
    }

    public static MVGlTexture create(String label, TextureFormat format, int width, int height) {
        GlStateManager.clearGlErrors();
        int id = GlStateManager._genTexture();

        int previous = GL11.glGetInteger(GL30.GL_TEXTURE_BINDING_2D_ARRAY);
        GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, id);
        GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL12.GL_TEXTURE_MIN_LOD, 0);
        GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL12.GL_TEXTURE_MAX_LOD, 0);
        GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        if (format.hasDepthAspect()) {
            GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
        }
        GL12.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GlConst.toGlInternalId(format), width, height, VIEWS, 0,
            GlConst.toGlExternalId(format), GlConst.toGlType(format), (ByteBuffer) null);
        GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, previous);

        int error = GlStateManager._getError();
        if (error == GL11.GL_OUT_OF_MEMORY) {
            GlStateManager._deleteTexture(id);
            throw new GpuOutOfMemoryException(
                "Could not allocate multiview texture of " + width + "x" + height + " for " + label);
        } else if (error != GL11.GL_NO_ERROR) {
            GlStateManager._deleteTexture(id);
            throw new IllegalStateException("OpenGL error " + error + " creating multiview texture " + label);
        }

        return new MVGlTexture(GpuTexture.USAGE_RENDER_ATTACHMENT, label, format, width, height, id);
    }

    private static void attachMultiview(int fbo, int colorId, int depthId) {
        int previous = GlStateManager.getFrameBuffer(GL30.GL_DRAW_FRAMEBUFFER);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo);
        if (colorId != 0) {
            OVRMultiview.glFramebufferTextureMultiviewOVR(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                colorId, 0, 0, VIEWS);
        } else {
            GL11.glDrawBuffer(GL11.GL_NONE);
        }
        if (depthId != 0) {
            OVRMultiview.glFramebufferTextureMultiviewOVR(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                depthId, 0, 0, VIEWS);
        }
        int status = GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previous);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Multiview framebuffer incomplete: 0x" + Integer.toHexString(status));
        }
    }

    @Override
    public int getFbo(DirectStateAccess directStateAccess, @Nullable GpuTexture depthTexture) {
        if (depthTexture != null && !(depthTexture instanceof MVGlTexture)) {
            throw new IllegalArgumentException("Multiview color texture can only be combined with a multiview depth");
        }
        int depthId = depthTexture == null ? 0 : ((MVGlTexture) depthTexture).glId();
        return this.mvFbos.computeIfAbsent(depthId, d -> {
            int fbo = GlStateManager.glGenFramebuffers();
            attachMultiview(fbo, this.id, d);
            return fbo;
        });
    }

    public void clearColor(float r, float g, float b, float a) {
        if (this.clearColorFbo == -1) {
            this.clearColorFbo = GlStateManager.glGenFramebuffers();
            attachMultiview(this.clearColorFbo, this.id, 0);
        }
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.clearColorFbo);
        GL11.glClearColor(r, g, b, a);
        GlStateManager._disableScissorTest();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void clearDepth(double depth) {
        if (this.clearDepthFbo == -1) {
            this.clearDepthFbo = GlStateManager.glGenFramebuffers();
            attachMultiview(this.clearDepthFbo, 0, this.id);
        }
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.clearDepthFbo);
        GL11.glClearDepth(depth);
        GlStateManager._depthMask(true);
        GlStateManager._disableScissorTest();
        GlStateManager._clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void blitLayer(int layer, int drawFbo, int dstWidth, int dstHeight) {
        if (this.readFbo == -1) {
            this.readFbo = GlStateManager.glGenFramebuffers();
        }
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.readFbo);
        GL30.glFramebufferTextureLayer(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, this.id, 0, layer);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
        GlStateManager._disableScissorTest();
        GlStateManager._glBlitFrameBuffer(0, 0, this.getWidth(0), this.getHeight(0), 0, 0, dstWidth, dstHeight,
            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void close() {
        if (!this.isClosed()) {
            for (int fbo : this.mvFbos.values()) {
                GlStateManager._glDeleteFramebuffers(fbo);
            }
            this.mvFbos.clear();
            for (int fbo : new int[]{this.clearColorFbo, this.clearDepthFbo, this.readFbo}) {
                if (fbo != -1) {
                    GlStateManager._glDeleteFramebuffers(fbo);
                }
            }
            this.clearColorFbo = this.clearDepthFbo = this.readFbo = -1;
        }
        super.close();
    }
}
