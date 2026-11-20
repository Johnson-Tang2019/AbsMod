package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.item.BladeRoute;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/** Schema 1 remains readable; schema 2 retains the sword during manual manifestation. */
public record BladeSoulRecord(UUID soulId, Optional<UUID> ownerId, BladeRoute route, int stage,
                              Optional<Double> affection, long revision, int schemaVersion, String form, Optional<SpiritCarrier> carrier,
                              boolean mirrorCleared) {
    public BladeSoulRecord(UUID soulId, Optional<UUID> ownerId, BladeRoute route, int stage,
            Optional<Double> affection, long revision, int schemaVersion, String form, Optional<SpiritCarrier> carrier) {
        this(soulId, ownerId, route, stage, affection, revision, schemaVersion, form, carrier, false);
    }
    public BladeSoulRecord(UUID soulId, Optional<UUID> ownerId, BladeRoute route, int stage,
            Optional<Double> affection, long revision, int schemaVersion, String form) {
        this(soulId, ownerId, route, stage, affection, revision, schemaVersion, form, Optional.empty());
    }
    private static final Codec<Double> FINITE_DOUBLE = Codec.DOUBLE.validate(value -> Double.isFinite(value)
            ? DataResult.success(value) : DataResult.error(() -> "Affection must be finite"));
    public static final Codec<BladeSoulRecord> CODEC = RecordCodecBuilder.<BladeSoulRecord>create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("soul_id").forGetter(BladeSoulRecord::soulId),
            UUIDUtil.CODEC.optionalFieldOf("owner_id").forGetter(BladeSoulRecord::ownerId),
            BladeRoute.CODEC.fieldOf("route").forGetter(BladeSoulRecord::route),
            Codec.INT.fieldOf("stage").forGetter(BladeSoulRecord::stage),
            FINITE_DOUBLE.optionalFieldOf("affection").forGetter(BladeSoulRecord::affection),
            Codec.LONG.fieldOf("revision").forGetter(BladeSoulRecord::revision),
            Codec.INT.fieldOf("schema_version").forGetter(BladeSoulRecord::schemaVersion),
            Codec.STRING.fieldOf("form").forGetter(BladeSoulRecord::form),
            SpiritCarrier.CODEC.optionalFieldOf("carrier").forGetter(BladeSoulRecord::carrier),
            Codec.BOOL.optionalFieldOf("mirror_cleared", false).forGetter(BladeSoulRecord::mirrorCleared)
    ).apply(instance, BladeSoulRecord::new)).validate(record -> record.valid()
            ? DataResult.success(record) : DataResult.error(() -> "Invalid or unsupported blade soul record"));

    public boolean valid() {
        return (!mirrorCleared || route == BladeRoute.NORMAL && stage >= 8) && route.accepts(stage) && revision >= 0 && revision < Long.MAX_VALUE
                && affection.map(Double::isFinite).orElse(true)
                && ((schemaVersion == 1 && form.equals("sword") && carrier.isEmpty())
                || (schemaVersion == 2 && ownerId.isPresent() && carrier.isPresent()
                && java.util.Set.of("materializing", "spirit", "recalling", "travelling", "recovery_pending").contains(form)
                && validCarrier(carrier.orElseThrow())));
    }

    private boolean validCarrier(SpiritCarrier retained) {
        var sword = retained.sword();
        var reference = sword.get(BladeSoulComponents.REFERENCE);
        return retained.sourceSlot() >= 0 && retained.sourceSlot() < 36 && sword.getCount() == 1
                && reference != null && reference.soulId().equals(soulId)
                && sword.getItem() instanceof com.abyssredemption.absmod.item.MeowBladeItem blade
                && route == blade.stage().route() && blade.stage().number() == stage;
    }

    public BladeSoulRecord withTestAffection(double value) {
        if (!Double.isFinite(value)) { throw new IllegalArgumentException("non_finite"); }
        return new BladeSoulRecord(soulId, ownerId, route, stage, Optional.of(value), revision + 1, schemaVersion, form, carrier, mirrorCleared);
    }

    public BladeSoulRecord transition(String nextForm, SpiritCarrier nextCarrier) {
        return new BladeSoulRecord(soulId, ownerId, route, stage, affection, revision + 1,
                nextCarrier == null ? 1 : 2, nextForm, Optional.ofNullable(nextCarrier), mirrorCleared);
    }

    public BladeSoulRecord clearMirror() {
        return new BladeSoulRecord(soulId, ownerId, route, stage, affection, revision + 1, schemaVersion, form, carrier, true);
    }

    public BladeSoulRecord withStage(int targetStage) {
        return new BladeSoulRecord(soulId, ownerId, route, targetStage, affection, revision + 1, schemaVersion, form, carrier, mirrorCleared);
    }
}
