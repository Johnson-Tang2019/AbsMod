package com.abyssredemption.absmod.combat;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import java.util.*;

/** Short server-only AI interruption with a separate per-target immunity window. */
public final class BladeStun {
    private record Window(long until,long immuneUntil){}
    private static final Map<Mob,Window> WINDOWS=new WeakHashMap<>();
    private BladeStun(){}
    public static boolean apply(Entity target) {
        if(!(target instanceof Monster mob) || target.level().isClientSide()
            || target instanceof net.minecraft.world.entity.boss.wither.WitherBoss || target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
            || target.getType().builtInRegistryHolder().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("absmod","heavy_stun_immune"))))return false;
        long now=target.level().getGameTime();var old=WINDOWS.get(mob);
        if(old!=null && now<old.immuneUntil)return false;
        WINDOWS.put(mob,new Window(now+12,now+112));mob.getNavigation().stop();
        return true;
    }
    public static boolean active(Mob mob) {
        var window=WINDOWS.get(mob);if(window==null)return false;
        long now=mob.level().getGameTime();
        if(now>=window.immuneUntil || !mob.isAlive()){WINDOWS.remove(mob);return false;}
        return now<window.until;
    }
    public static void clear(){WINDOWS.clear();}
}
