package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.shaders.AbstractUniform;

public class MultiUniform extends AbstractUniform {

    private final AbstractUniform[] uniforms;

    public MultiUniform(AbstractUniform... uniforms) {
        this.uniforms = uniforms;
    }

    @Override
    public void set(float value) {
        for (AbstractUniform uniform : this.uniforms) {
            uniform.set(value);
        }
    }

    @Override
    public void set(int value) {
        for (AbstractUniform uniform : this.uniforms) {
            uniform.set(value);
        }
    }
}
