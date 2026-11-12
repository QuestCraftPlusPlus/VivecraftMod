package org.vivecraft.client.extensions;

import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;

public interface GlDeviceExtension {
    GlRenderPipeline vivecraft$getOrCompileMultiviewPipeline(RenderPipeline pipeline);
}
