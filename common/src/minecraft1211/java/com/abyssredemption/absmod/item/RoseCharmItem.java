package com.abyssredemption.absmod.item;

import java.util.List;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;

public final class RoseCharmItem extends Item {
    public enum Kind {
        PAW_SIGNET(MobEffects.LUCK), DUSK_EARS(MobEffects.NIGHT_VISION),
        QUARRY_MITTENS(MobEffects.DIG_SPEED), SPRING_SLIPPERS(MobEffects.JUMP),
        TIDE_PENDANT(MobEffects.WATER_BREATHING), FISHBONE_SASH(MobEffects.MOVEMENT_SPEED),
        CLOUD_MANTLE(MobEffects.SLOW_FALLING);

        private final Holder<MobEffect> effect;
        Kind(Holder<MobEffect> effect) { this.effect = effect; }
        public String id() { return name().toLowerCase(Locale.ROOT); }
    }

    private final Kind kind;
    public RoseCharmItem(Kind kind, Properties properties) {
        super(properties.stacksTo(1));
        this.kind = kind;
    }

    public void apply(ServerPlayer player) {
        // Night vision needs at least ten seconds remaining to avoid its vanilla fade.
        int duration = kind == Kind.DUSK_EARS ? 240 : 40;
        var current = player.getEffect(kind.effect);
        if (current == null || (!current.isInfiniteDuration() && current.getAmplifier() == 0 && current.getDuration() <= duration - 10)) {
            player.addEffect(new MobEffectInstance(kind.effect, duration, 0, true, false, false));
        }
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("charm.absmod.effect", Component.translatable(kind.effect.value().getDescriptionId())));
        lines.add(Component.translatable("charm.absmod.equip"));
    }
}
