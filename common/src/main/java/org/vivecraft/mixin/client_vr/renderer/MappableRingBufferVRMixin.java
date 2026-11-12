package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.renderer.MappableRingBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MappableRingBuffer.class)
public class MappableRingBufferVRMixin {

    @Unique
    private static final int vivecraft$BUFFER_COUNT = 3 * 4;

    @ModifyConstant(method = {"<init>", "rotate", "close"}, constant = @Constant(intValue = 3), require = 5)
    private int vivecraft$moreBuffers(int bufferCount) {
        return vivecraft$BUFFER_COUNT;
    }
}
