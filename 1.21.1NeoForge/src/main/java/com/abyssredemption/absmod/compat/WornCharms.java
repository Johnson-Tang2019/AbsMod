package com.abyssredemption.absmod.compat;

import com.abyssredemption.absmod.item.RoseCharmItem;
import net.minecraft.server.level.ServerPlayer;
import top.theillusivec4.curios.api.CuriosApi;

public final class WornCharms {
    private WornCharms() {}
    public static void apply(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            var worn = inventory.getEquippedCurios();
            for (int slot = 0; slot < worn.getSlots(); slot++) {
                if (worn.getStackInSlot(slot).getItem() instanceof RoseCharmItem charm) charm.apply(player);
            }
        });
    }
}
