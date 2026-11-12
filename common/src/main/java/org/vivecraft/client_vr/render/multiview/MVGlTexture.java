package org.vivecraft.client_vr.render.multiview;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.OVRMultiview;

import java.nio.ByteBuffer;
import java.util.Collections;

import static org.vivecraft.client_vr.render.helpers.MultiviewHelper.attachMultiview;

public class MVGlTexture extends GlTexture {

    public static final int VIEWS = 2;
    private int clearDepthFbo = -1;
    private int clearColorFbo = -1;
    private int readFbo = -1;

    private MVGlTexture(int usage, String label, GpuFormat format, int width, int height, int id) {
        super(usage, label, format, width, height, 1, 1, id, ((GlDevice)RenderSystem.getDevice().backend).frameBufferCache());
    }

    public static MVGlTexture create(String label, GpuFormat format, int width, int height) {
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

        return new MVGlTexture(
            GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_COPY_SRC,
            label, format, width, height, id);
    }

    public void clearColor(float r, float g, float b, float a) {
        if (this.clearColorFbo == -1) {
            this.clearColorFbo = GlStateManager.glGenFramebuffers();
            attachMultiview(this.clearColorFbo, this.id, 0, VIEWS);
        }
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.clearColorFbo);
        GL11.glClearColor(r, g, b, a);
        GlStateManager._disableScissorTest();
        GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void clearDepth(double depth) {
        if (this.clearDepthFbo == -1) {
            this.clearDepthFbo = GlStateManager.glGenFramebuffers();
            attachMultiview(this.clearDepthFbo, 0, this.id, VIEWS);
        }
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.clearDepthFbo);
        GL11.glClearDepth(depth);
        GlStateManager._depthMask(true);
        GlStateManager._disableScissorTest();
        GlStateManager._clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void blitLayer(int layer, GpuTextureView dest, int dstWidth, int dstHeight) {
        GpuDevice device = RenderSystem.getDevice();
        if (device.backend instanceof GlDevice glDevice) {
            if (this.readFbo == -1) {
                this.readFbo = GlStateManager.glGenFramebuffers();
            }
            int drawFbo = glDevice.frameBufferCache()
                .getFbo(glDevice.directStateAccess(), Collections.singletonList((GlTextureView) dest), null);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.readFbo);
            GL30.glFramebufferTextureLayer(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, this.id, 0, layer);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
            GlStateManager._disableScissorTest();
            GlStateManager._glBlitFrameBuffer(0, 0, this.getWidth(0), this.getHeight(0), 0, 0, dstWidth, dstHeight,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        }
    }

    @Override
    public void close() {
        if (!this.isClosed()) {
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
