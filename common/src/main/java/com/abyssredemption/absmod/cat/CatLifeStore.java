package com.abyssredemption.absmod.cat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** World-level indexes. Every observation and bell selection is scoped to its player. */
public final class CatLifeStore extends SavedData {
    public record Observation(UUID observer, UUID cat, String name, boolean owned, CatGenome genome,
                              String visibleVariant, Optional<GlobalPos> lastSeen) {
        static final Codec<Observation> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("observer").forGetter(Observation::observer),
                UUIDUtil.CODEC.fieldOf("cat").forGetter(Observation::cat),
                Codec.STRING.fieldOf("name").forGetter(Observation::name),
                Codec.BOOL.fieldOf("owned").forGetter(Observation::owned),
                CatGenome.CODEC.fieldOf("genome").forGetter(Observation::genome),
                Codec.STRING.optionalFieldOf("visible_variant", "").forGetter(Observation::visibleVariant),
                GlobalPos.CODEC.optionalFieldOf("last_seen").forGetter(Observation::lastSeen)
        ).apply(i, Observation::new));
    }

    public record Home(GlobalPos spot, UUID owner, Optional<UUID> cat, boolean resting) {
        static final Codec<Home> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("spot").forGetter(Home::spot),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Home::owner),
                UUIDUtil.CODEC.optionalFieldOf("cat").forGetter(Home::cat),
                Codec.BOOL.fieldOf("resting").forGetter(Home::resting)
        ).apply(i, Home::new));
    }

    private record Selection(UUID player, UUID cat) {
        static final Codec<Selection> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("player").forGetter(Selection::player),
                UUIDUtil.CODEC.fieldOf("cat").forGetter(Selection::cat)
        ).apply(i, Selection::new));
    }

    static final Codec<CatLifeStore> CODEC = RecordCodecBuilder.create(i -> i.group(
            Observation.CODEC.listOf().fieldOf("observations").forGetter(store -> store.observations.values().stream()
                    .flatMap(entries -> entries.values().stream()).toList()),
            Home.CODEC.listOf().fieldOf("homes").forGetter(store -> List.copyOf(store.homes.values())),
            Selection.CODEC.listOf().fieldOf("selections").forGetter(store -> store.selections.entrySet().stream()
                    .map(entry -> new Selection(entry.getKey(), entry.getValue())).toList())
    ).apply(i, CatLifeStore::new));
    private static final SavedDataType<CatLifeStore> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("absmod", "cat_life"), CatLifeStore::new, CODEC,
            net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, Map<UUID, Observation>> observations = new HashMap<>();
    private final Map<GlobalPos, Home> homes = new HashMap<>();
    private final Map<UUID, Home> byCat = new HashMap<>();
    private final Map<UUID, UUID> selections = new HashMap<>();

    private CatLifeStore() {}

    private CatLifeStore(List<Observation> records, List<Home> homes, List<Selection> selections) {
        records.forEach(row -> this.observations.computeIfAbsent(row.observer(), ignored -> new HashMap<>()).put(row.cat(), row));
        homes.forEach(home -> {
            if (this.homes.containsKey(home.spot()) || home.cat().filter(byCat::containsKey).isPresent()) {
                throw new IllegalArgumentException("Duplicate cat home");
            }
            putHome(home);
        });
        selections.forEach(selection -> this.selections.put(selection.player(), selection.cat()));
    }

    public static CatLifeStore get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public void observe(ServerPlayer player, Cat cat) {
        UUID owner = player.getUUID();
        boolean owned = cat.isOwnedBy(player);
        String name = cat.hasCustomName() ? cat.getCustomName().getString() : "";
        var location = owned ? Optional.of(GlobalPos.of(cat.level().dimension(), cat.blockPosition())) : Optional.<GlobalPos>empty();
        var genome = ((CatGenetics) cat).absmod$getGenome();
        String variant = cat.getVariant().unwrapKey().map(key -> key.identifier().toString()).orElse(genome.pattern());
        var row = new Observation(owner, cat.getUUID(), name, owned, genome, variant, location);
        var previous = observations.computeIfAbsent(owner, ignored -> new HashMap<>()).put(row.cat(), row);
        if (!row.equals(previous)) {
            setDirty();
        }
    }

    public Map<UUID, Observation> observations(UUID player) {
        return Map.copyOf(observations.getOrDefault(player, Map.of()));
    }

    public Observation observation(UUID player, UUID cat) {
        return observations.getOrDefault(player, Map.of()).get(cat);
    }

    public UUID selected(UUID player) {
        return selections.get(player);
    }

    public void select(UUID player, UUID cat) {
        if (cat == null) {
            selections.remove(player);
        } else {
            selections.put(player, cat);
        }
        setDirty();
    }

    public Home at(GlobalPos spot) {
        return homes.get(spot);
    }

    public Home home(UUID cat) {
        return byCat.get(cat);
    }

    public void place(GlobalPos spot, UUID owner) {
        putHome(new Home(spot, owner, Optional.empty(), false));
        setDirty();
    }

    public void bind(GlobalPos spot, UUID owner, UUID cat) {
        var other = at(spot);
        if (other != null && !other.owner().equals(owner)) {
            throw new IllegalArgumentException("Nest belongs to another player");
        }
        var previous = home(cat);
        if (previous != null && !previous.spot().equals(spot)) {
            place(previous.spot(), previous.owner());
        }
        putHome(new Home(spot, owner, Optional.of(cat), true));
        setDirty();
    }

    public void setResting(Home home, boolean resting) {
        if (home != null && home.cat().isPresent() && home.resting() != resting) {
            putHome(new Home(home.spot(), home.owner(), home.cat(), resting));
            setDirty();
        }
    }

    public void remove(GlobalPos spot) {
        var removed = homes.remove(spot);
        if (removed != null) {
            removed.cat().ifPresent(byCat::remove);
            setDirty();
        }
    }

    private void putHome(Home home) {
        var old = homes.put(home.spot(), home);
        if (old != null) {
            old.cat().ifPresent(byCat::remove);
        }
        home.cat().ifPresent(cat -> byCat.put(cat, home));
    }
}
