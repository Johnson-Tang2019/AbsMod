package com.abyssredemption.absmod.spirit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.*;
import java.util.*;

/** Persistent per-soul daily budgets; relogging never resets earned rewards. */
public final class BladeGrowthStore extends SavedData {
    public record Day(UUID soul,long day,int walking,int follow,int hits,int combat,boolean talked,boolean gifted,List<UUID> targets) {
        static final Codec<Day> CODEC=RecordCodecBuilder.create(i->i.group(
            UUIDUtil.CODEC.fieldOf("soul").forGetter(Day::soul),Codec.LONG.fieldOf("day").forGetter(Day::day),
            Codec.INT.fieldOf("walking").forGetter(Day::walking),Codec.INT.fieldOf("follow").forGetter(Day::follow),
            Codec.INT.fieldOf("hits").forGetter(Day::hits),Codec.INT.fieldOf("combat").forGetter(Day::combat),
            Codec.BOOL.fieldOf("talked").forGetter(Day::talked),Codec.BOOL.fieldOf("gifted").forGetter(Day::gifted),
            UUIDUtil.CODEC.listOf().fieldOf("targets").forGetter(Day::targets)).apply(i,Day::new));
    }
    private final Map<UUID,Day> days=new HashMap<>();
    private static final Codec<BladeGrowthStore> RECORDS=Day.CODEC.listOf().fieldOf("days").codec().flatXmap(list->{
        var store=new BladeGrowthStore();
        for(var day:list) {
            if(day.walking<0 || day.walking>=6000 || day.follow<0 || day.follow>4
                    || day.hits<0 || day.hits>=10 || day.combat<0 || day.combat>4
                    || day.targets.size()>40 || new HashSet<>(day.targets).size()!=day.targets.size()
                    || store.days.putIfAbsent(day.soul,day)!=null)
                return DataResult.error(()->"Invalid or duplicate growth ledger");
        }
        return DataResult.success(store);
    },store->DataResult.success(List.copyOf(store.days.values())));
    public static final Codec<BladeGrowthStore> CODEC=new Codec<>() {
        @Override public <T> DataResult<com.mojang.datafixers.util.Pair<BladeGrowthStore,T>> decode(
                com.mojang.serialization.DynamicOps<T> ops,T input) {
            return RECORDS.decode(ops,input).result().map(DataResult::success)
                    .orElseGet(()->DataResult.error(()->"Invalid growth ledger; recovery required"));
        }
        @Override public <T> DataResult<T> encode(BladeGrowthStore input,com.mojang.serialization.DynamicOps<T> ops,T prefix) {
            return RECORDS.encode(input,ops,prefix);
        }
    };
    private static final SavedDataType<BladeGrowthStore> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("absmod","blade_growth"),BladeGrowthStore::new,CODEC,net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    public static BladeGrowthStore get(MinecraftServer server) {
        if(!server.isSameThread())throw new IllegalStateException("server_thread");
        var store=server.getDataStorage().get(TYPE);
        if(store==null) {
            if(!java.nio.file.Files.notExists(TYPE.id().withSuffix(".dat").resolveAgainst(
                    server.getWorldPath(net.minecraft.world.level.storage.LevelResource.DATA))))
                throw new IllegalStateException("growth_store_unreadable");
            store=new BladeGrowthStore();server.getDataStorage().set(TYPE,store);
        }
        return store;
    }
    public Day day(UUID soul,long now) {
        var previous=days.get(soul);
        return previous==null || now>previous.day?new Day(soul,now,0,0,0,0,false,false,List.of()):previous;
    }
    public void put(Day day){days.put(day.soul,day);setDirty();}
}
