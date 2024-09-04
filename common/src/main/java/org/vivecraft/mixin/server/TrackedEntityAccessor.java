package org.vivecraft.mixin.server;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ChunkMap.TrackedEntity.class)
public interface TrackedEntityAccessor {

    @Accessor("seenBy")
    Set<ServerPlayerConnection> getPlayersTracking();
}
