package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.CookeryFinish;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StateDefinition.Builder.class)
abstract class CookeryDefinitionMixin {
    @Shadow @Final private Object owner;
    @Shadow @Final private Map<String,Property<?>> properties;

    // Teacups build a second definition after their size is known.
    @Inject(method = "create", at = @At("HEAD"))
    private void absmod$addFinish(CallbackInfoReturnable<?> callback) {
        if (owner instanceof Block block && CookeryFinish.supports(block)) properties.putIfAbsent(CookeryFinish.ROSE.getName(), CookeryFinish.ROSE);
    }
}
