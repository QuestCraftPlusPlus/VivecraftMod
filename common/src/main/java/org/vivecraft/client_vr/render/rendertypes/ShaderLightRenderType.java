package org.vivecraft.client_vr.render.rendertypes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector3f;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;

public class ShaderLightRenderType extends WrappedRenderType {

    private final Vector3f normal;

    private Vector3f light0Old;
    private Vector3f light1Old;

    public ShaderLightRenderType(RenderType wrapped, Vector3f normal) {
        super(wrapped);
        this.normal = normal;
    }

    @Override
    public void setupRenderState() {
        super.setupRenderState();

        // store old lights
        this.light0Old = RenderSystemAccessor.getShaderLightDirections()[0];
        this.light1Old = RenderSystemAccessor.getShaderLightDirections()[1];

        // set lights to front
        RenderSystem.setShaderLights(this.normal, this.normal);
    }

    @Override
    public void clearRenderState() {
        // reset lights
        if (this.light0Old != null && this.light1Old != null) {
            RenderSystem.setShaderLights(this.light0Old, this.light1Old);
            this.light0Old = null;
            this.light1Old = null;
        }

        super.clearRenderState();
    }
}
