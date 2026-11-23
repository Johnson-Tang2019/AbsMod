package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.AbsMod;
import com.abyssredemption.absmod.spirit.WorldUpgrade;
import java.nio.file.Path;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
abstract class WorldUpgradeMixin {
    @Unique private boolean absmod$backupChecked;
    @Unique private boolean absmod$needsMigration;
    @Unique private String absmod$release;

    @Unique private Path absmod$worldPath() {
        var storage = (LevelStorageSource.LevelStorageAccess)(Object)this;
        return storage.getLevelPath(LevelResource.ROOT).toAbsolutePath().normalize();
    }

    @Unique private void absmod$protectOriginal() {
        if (absmod$backupChecked) return;
        absmod$release = ModList.get().getModContainerById(AbsMod.MOD_ID).orElseThrow().getModInfo().getVersion().toString();
        absmod$needsMigration = WorldUpgrade.snapshotIfNeeded(absmod$worldPath(), absmod$release);
        absmod$backupChecked = true;
    }

    // Dedicated --forceUpgrade runs after reading the mod list but before the first metadata write.
    @Inject(method = "readAdditionalLevelSaveData", at = @At("HEAD"))
    private void absmod$beforeModDataRead(boolean fallback, CallbackInfo callback) {
        absmod$protectOriginal();
    }

    @Inject(method = "restoreLevelDataFromOld", at = @At("HEAD"))
    private void absmod$beforeVanillaRecovery(CallbackInfoReturnable<Boolean> callback) {
        absmod$protectOriginal();
    }

    // Both integrated and dedicated servers have registry access before the first rotation of level.dat.
    @Inject(method = "saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void absmod$beforeFirstWrite(RegistryAccess registries, WorldData data, CompoundTag player, CallbackInfo callback) {
        absmod$protectOriginal();
        if (absmod$needsMigration) {
            WorldUpgrade.finish(absmod$worldPath(), registries, absmod$release);
            absmod$needsMigration = false;
        }
    }
}
