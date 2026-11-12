package org.vivecraft.client_vr.extensions.vulkan;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vulkan.VulkanRenderPipeline;

import java.util.Set;

public interface VulkanDeviceExtension {

    /**
     * @return set of all supported device extensions
     */
    Set<String> vivecraft$getAvailableDeviceExtensions();

    VulkanRenderPipeline vivecraft$getOrCompileMultiviewPipeline(RenderPipeline pipeline);
}
