package org.vivecraft.client_vr.render.rendertypes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;

import java.util.Optional;

/**
 * RenderType that forwards all calls to a wrapped RenderType
 */
public abstract class WrappedRenderType extends RenderType {

    private final RenderType wrapped;

    public WrappedRenderType(RenderType wrapped) {
        super(wrapped.getName(), wrapped.bufferSize(), wrapped.affectsCrumbling(), wrapped.sortOnUpload(),
            wrapped::setupRenderState, wrapped::clearRenderState);
        this.wrapped = wrapped;
        if (IrisHelper.isLoaded()) {
            IrisHelper.copyBlendingState(wrapped, this);
        }
    }

    @Override
    public void draw(MeshData meshData) {
        // need to manually call setup and clear, because wrapped.draw only calls them on the wrapped one
        this.setupRenderState();
        this.wrapped.draw(meshData);
        this.clearRenderState();
    }

    @Override
    public RenderTarget getRenderTarget() {
        return this.wrapped.getRenderTarget();
    }

    @Override
    public RenderPipeline getRenderPipeline() {
        return this.wrapped.getRenderPipeline();
    }

    @Override
    public VertexFormat format() {
        return this.wrapped.format();
    }

    @Override
    public VertexFormat.Mode mode() {
        return this.wrapped.mode();
    }

    @Override
    public int bufferSize() {
        return this.wrapped.bufferSize();
    }

    @Override
    public Optional<RenderType> outline() {
        return this.wrapped.outline();
    }

    @Override
    public boolean isOutline() {
        return this.wrapped.isOutline();
    }

    @Override
    public boolean affectsCrumbling() {
        return this.wrapped.affectsCrumbling();
    }

    @Override
    public boolean canConsolidateConsecutiveGeometry() {
        return this.wrapped.canConsolidateConsecutiveGeometry();
    }

    @Override
    public boolean sortOnUpload() {
        return this.wrapped.sortOnUpload();
    }

    @Override
    public void setupRenderState() {
        this.wrapped.setupRenderState();
    }

    @Override
    public void clearRenderState() {
        this.wrapped.clearRenderState();
    }

    @Override
    public String toString() {
        return "vivecraft_wrapped:" + this.wrapped.toString();
    }

    @Override
    public String getName() {
        return this.wrapped.getName();
    }

    @Override
    public int hashCode() {
        // make sure we don't match the original
        return this.wrapped.hashCode() + 2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WrappedRenderType that = (WrappedRenderType) o;
        return this.wrapped.equals(that.wrapped);
    }
}
