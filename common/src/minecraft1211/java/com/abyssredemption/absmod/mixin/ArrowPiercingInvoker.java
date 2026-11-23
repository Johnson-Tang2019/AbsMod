package com.abyssredemption.absmod.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface ArrowPiercingInvoker {
    @Invoker("setPierceLevel")
    void absmod$setPierceLevel(byte level);
}
