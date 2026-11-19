package com.abyssredemption.absmod.cat;

import com.abyssredemption.absmod.cat.CatJournalMenu.Control;
import com.abyssredemption.absmod.cat.CatJournalMenu.Row;
import com.abyssredemption.absmod.cat.CatLifeStore.Observation;
import com.abyssredemption.absmod.spirit.BladeAdvancements;
import com.abyssredemption.absmod.spirit.BladeSoulRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static com.abyssredemption.absmod.cat.CatLife.label;
import static com.abyssredemption.absmod.cat.CatJournalMenu.PAGE_SIZE;

final class CatJournalViews {
    private static final Comparator<Observation> ORDER = Comparator.comparing(Observation::name)
            .thenComparingInt(row -> row.genome().generation()).thenComparing(Observation::cat);
    private static final int FAMILY_PAGE_SIZE = 2;
    private static final String[] COLORS = java.util.stream.Stream.concat(
            java.util.Arrays.stream(CatCoat.COLORS), java.util.stream.Stream.of("white", "brown", "mixed"))
            .toArray(String[]::new);

    private record Filter(int color, int pattern, int eyes, boolean owned) {
        static final Filter ALL = new Filter(-1, -1, -1, false);

        boolean matches(Observation row) {
            var coat = row.genome().coat();
            return (!owned || row.owned())
                    && (color < 0 || color == coat.map(c -> c.color(row.genome())).orElseGet(() -> classicColor(row.visibleVariant())))
                    && (pattern < 0 || pattern == coat.map(CatCoat::marking).orElse(CatCoat.MARKINGS.length))
                    && (eyes < 0 || eyes == coat.map(CatCoat::eyes).orElse(CatCoat.EYES.length));
        }
    }

    private CatJournalViews() {}

    static void collection(ServerPlayer player) {
        collection(player, Filter.ALL, 0);
    }

    private static void collection(ServerPlayer player, Filter filter, int requested) {
        var all = CatLife.store(player).observations(player.getUUID());
        var results = all.values().stream().filter(filter::matches).sorted(ORDER).toList();
        int last = lastPage(results.size(), PAGE_SIZE);
        int page = Math.min(last, Math.max(requested, 0));
        var rows = results.stream().skip((long) page * PAGE_SIZE).limit(PAGE_SIZE).map(row ->
                new Row(CatLife.name(row), appearance(row), () -> family(player, row.cat(), filter, page, 0))).toList();
        CatJournalMenu.show(player, label("collection"), label(results.isEmpty() ? "empty_collection" : "page",
                        page + 1, last + 1, results.size()), rows, List.of(
                new Control(label("colors", filterValue("color", COLORS, filter.color())), () -> filter(player,
                        new Filter(cycle(filter.color(), COLORS.length), filter.pattern(), filter.eyes(), filter.owned()))),
                new Control(label("patterns", filterValue("marking", CatCoat.MARKINGS, filter.pattern())), () -> filter(player,
                        new Filter(filter.color(), cycle(filter.pattern(), CatCoat.MARKINGS.length), filter.eyes(), filter.owned()))),
                new Control(label("eyes", filterValue("eyes", CatCoat.EYES, filter.eyes())), () -> filter(player,
                        new Filter(filter.color(), filter.pattern(), cycle(filter.eyes(), CatCoat.EYES.length), filter.owned()))),
                new Control(label(filter.owned() ? "owned" : "all_observed"), () -> filter(player,
                        new Filter(filter.color(), filter.pattern(), filter.eyes(), !filter.owned()))),
                new Control(label("previous"), page > 0 ? () -> collection(player, filter, page - 1) : null),
                new Control(label("next"), page < last ? () -> collection(player, filter, page + 1) : null)));
    }

    private static void filter(ServerPlayer player, Filter filter) {
        if (!CatLife.store(player).observations(player.getUUID()).isEmpty()) {
            BladeAdvancements.award(player, "cat_collection");
        }
        collection(player, filter, 0);
    }

    private static void family(ServerPlayer player, UUID id, Filter filter, int collectionPage, int requested) {
        var all = CatLife.store(player).observations(player.getUUID());
        var row = all.get(id);
        if (row == null) {
            collection(player, filter, collectionPage);
            return;
        }
        var genome = row.genome();
        if (genome.firstParent().isPresent() || genome.secondParent().isPresent()) {
            BladeAdvancements.award(player, "cat_pedigree");
        }
        var children = all.values().stream()
                .filter(candidate -> candidate.genome().firstParent().filter(id::equals).isPresent()
                        || candidate.genome().secondParent().filter(id::equals).isPresent())
                .sorted(ORDER).toList();
        int last = lastPage(children.size(), FAMILY_PAGE_SIZE);
        int page = Math.min(last, Math.max(requested, 0));
        var rows = new ArrayList<Row>();
        rows.add(new Row(CatLife.name(row), label("family_traits", genome.generation(), genome.fullNotation(), appearance(row)), null));
        rows.add(relative(player, all, genome.firstParent().orElse(null), "first_parent", filter, collectionPage));
        rows.add(relative(player, all, genome.secondParent().orElse(null), "second_parent", filter, collectionPage));
        children.stream().skip((long) page * FAMILY_PAGE_SIZE).limit(FAMILY_PAGE_SIZE)
                .forEach(child -> rows.add(relative(player, all, child.cat(), "child", filter, collectionPage)));
        CatJournalMenu.show(player, label("family"), label("family_page", children.size(), page + 1, last + 1), rows, List.of(
                new Control(label("back"), () -> collection(player, filter, collectionPage)),
                new Control(label("previous"), page > 0 ? () -> family(player, id, filter, collectionPage, page - 1) : null),
                new Control(label("next"), page < last ? () -> family(player, id, filter, collectionPage, page + 1) : null)));
    }

    private static Row relative(ServerPlayer player, Map<UUID, Observation> all, UUID id, String role, Filter filter, int collectionPage) {
        var row = id == null ? null : all.get(id);
        if (row == null) {
            return new Row(label(role, label(id == null ? "no_parent" : "unrecorded")), Component.empty(), null);
        }
        return new Row(label(role, CatLife.name(row)), label("family_traits", row.genome().generation(), row.genome().fullNotation(), appearance(row)),
                () -> family(player, id, filter, collectionPage, 0));
    }

    static void partners(ServerPlayer player, int requested) {
        var store = CatLife.store(player);
        var owner = player.getUUID();
        var cats = store.observations(owner).values().stream().filter(Observation::owned).sorted(ORDER).toList();
        var spirits = CatLife.spirits(player);
        int count = cats.size() + spirits.size();
        int last = lastPage(count, PAGE_SIZE);
        int page = Math.min(last, Math.max(requested, 0));
        var rows = new ArrayList<Row>();
        for (int index = page * PAGE_SIZE; index < Math.min(count, (page + 1) * PAGE_SIZE); index++) {
            if (index < cats.size()) {
                var cat = cats.get(index);
                var name = label(cat.cat().equals(store.selected(owner)) ? "selected_cat" : "cat_kind", CatLife.name(cat));
                rows.add(new Row(name, label(CatLife.nearby(player, cat.cat()) == null ? "away" : "nearby"),
                        () -> partnerCat(player, cat, page)));
            } else {
                var spirit = spirits.get(index - cats.size());
                var name = label("spirit_kind", spirit.carrier().orElseThrow().sword().getHoverName());
                rows.add(new Row(name, label(CatLife.nearbySpirit(player, spirit) == null ? "away" : "nearby"),
                        () -> partnerSpirit(player, spirit, page)));
            }
        }
        CatJournalMenu.show(player, label("partners"), label(count == 0 ? "empty_partners" : "page", page + 1, last + 1, count),
                rows, List.of(
                        new Control(label("clear_selection"), store.selected(owner) == null ? null : () -> {
                            store.select(owner, null);
                            partners(player, page);
                        }),
                        new Control(label("previous"), page > 0 ? () -> partners(player, page - 1) : null),
                        new Control(label("next"), page < last ? () -> partners(player, page + 1) : null)));
    }

    private static void partnerCat(ServerPlayer player, Observation observed, int page) {
        var store = CatLife.store(player);
        var cat = CatLife.nearby(player, observed.cat());
        if (cat != null) {
            store.observe(player, cat);
        }
        var current = store.observation(player.getUUID(), observed.cat());
        var home = CatLife.findHome(player, observed.cat());
        var rows = List.of(
                new Row(label("cat_kind", CatLife.name(current)), label(cat == null ? "away" : "nearby"), null),
                new Row(label("nest"), home == null || !home.owner().equals(player.getUUID()) ? label("no_home") : CatLife.position(home.spot()), null),
                new Row(label("last_seen"), current.lastSeen().map(CatLife::position).orElse(label("unrecorded")), null));
        CatJournalMenu.show(player, label("partners"), label("partner_details"), rows, List.of(
                new Control(label("back"), () -> partners(player, page)),
                new Control(label("choose"), cat == null ? null : () -> {
                    var nearby = CatLife.nearby(player, observed.cat());
                    if (nearby != null) {
                        CatLife.select(player, nearby);
                    }
                    partners(player, page);
                }),
                new Control(label("call"), cat == null ? null : () -> {
                    CatLife.call(player, observed.cat());
                    partnerCat(player, observed, page);
                })));
    }

    private static void partnerSpirit(ServerPlayer player, BladeSoulRecord spirit, int page) {
        var carrier = spirit.carrier().orElseThrow();
        var nearby = CatLife.nearbySpirit(player, spirit);
        var rows = List.of(
                new Row(label("spirit_kind", carrier.sword().getHoverName()), label(nearby == null ? "away" : "nearby"), null),
                new Row(label("last_seen"), carrier.lastLocation().map(CatLife::position).orElse(label("unrecorded")), null));
        CatJournalMenu.show(player, label("partners"), label("partner_details"), rows, List.of(
                new Control(label("back"), () -> partners(player, page)),
                new Control(label("recall"), nearby == null ? null : () -> {
                    CatLife.recall(player, spirit);
                    partners(player, page);
                })));
    }

    private static Component appearance(Observation row) {
        var genome = row.genome();
        return genome.coat().<Component>map(coat -> label("appearance",
                trait("color", CatCoat.COLORS[coat.color(genome)]), trait("marking", CatCoat.MARKINGS[coat.marking()]),
                trait("eyes", CatCoat.EYES[coat.eyes()])))
                .orElseGet(() -> Component.translatable("cat.absmod.pattern." + (row.visibleVariant().isEmpty() ? genome.pattern() : row.visibleVariant()).replace(':', '.')));
    }

    private static int classicColor(String variant) {
        return switch (variant) {
            case "minecraft:all_black" -> 0;
            case "minecraft:british_shorthair", "absmod:blue_point" -> 1;
            case "absmod:chocolate", "absmod:chocolate_point" -> 2;
            case "absmod:lilac", "absmod:lilac_point" -> 3;
            case "minecraft:red" -> 6;
            case "minecraft:persian", "minecraft:siamese", "absmod:seal_point" -> 7;
            case "minecraft:white" -> 9;
            case "minecraft:tabby" -> 10;
            case "minecraft:black", "minecraft:calico", "minecraft:jellie", "minecraft:ragdoll" -> 11;
            default -> COLORS.length;
        };
    }

    private static Component filterValue(String group, String[] values, int index) {
        return index < 0 ? label("any") : index == values.length ? label("unclassified") : trait(group, values[index]);
    }

    private static Component trait(String group, String value) {
        return Component.translatable("cat.absmod." + group + "." + value);
    }

    private static int cycle(int value, int count) { return value == count ? -1 : value + 1; }
    private static int lastPage(int count, int size) { return Math.max(0, (count - 1) / size); }
}
