package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.item.MeowBladeItem;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Anvils and grindstones may not sacrifice a second soul blade. */
@Mixin({AnvilMenu.class,GrindstoneMenu.class})
public abstract class BladeCombineMenuMixin {
    @Inject(method="createResult",at=@At("TAIL"))
    private void preserveSecondSoul(CallbackInfo callback) {
        var menu=(AbstractContainerMenu)(Object)this;
        if(!menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().getItem() instanceof MeowBladeItem)
            menu.getSlot(2).set(ItemStack.EMPTY);
    }
}
