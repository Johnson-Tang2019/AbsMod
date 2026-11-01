package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.client.animation.BladeMotionPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class BladeMotionReloadMixin {
    @Inject(method="onResourceManagerReload",at=@At("TAIL"))
    private void reload(ResourceManager resources,CallbackInfo ci) { BladeMotionPlayer.reload(resources); }
}
