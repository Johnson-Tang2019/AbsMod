package com.abyssredemption.absmod.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The upstream menu checks the original block by identity. Extend the same proximity test to our pot. */
@Pseudo
@Mixin(targets = "vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu", remap = false)
public abstract class RosePotMenuMixin {
    @Shadow @Final private ContainerLevelAccess canInteractWithCallable;
    @Unique private static final ResourceLocation absmod$rosePot = ResourceLocation.fromNamespaceAndPath("absmod", "rose_cooking_pot");
    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void absmod$allowRosePot(Player player, CallbackInfoReturnable<Boolean> result) {
        canInteractWithCallable.evaluate((level, pos) -> {
            if (BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).equals(absmod$rosePot)) {
                result.setReturnValue(player.level() == level && player.canInteractWithBlock(pos, 4));
            }
            return true;
        });
    }
}
