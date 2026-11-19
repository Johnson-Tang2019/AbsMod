package com.abyssredemption.absmod.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface ServerStorageAccessor {
    @Accessor("storageSource")
    LevelStorageSource.LevelStorageAccess absmod$getLevelStorage();
}
