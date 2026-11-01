package com.abyssredemption.absmod.item;

import com.abyssredemption.absmod.cat.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.InteractionHand;
import java.util.*;

/** A vanilla book screen plus non-consuming cat observations; no custom screen dependency. */
public final class FieldGuideItem extends WrittenBookItem {
    public FieldGuideItem(Properties properties) {super(properties.stacksTo(1));}
    @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,InteractionHand hand) {
        var pages=new ArrayList<Filterable<Component>>();
        for(int i=1;i<=24;i++)pages.add(Filterable.passThrough(Component.translatable("guide.absmod.page."+i)));
        player.getItemInHand(hand).set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(Filterable.passThrough("猫咪观察手册"),"Ab's Mod",0,pages,true));
        // Synchronize generated pages before the client receives the open-book packet.
        if(player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
            // 1.21.1 ServerPlayer.openItemGui only accepts the vanilla written-book item ID.
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundOpenBookPacket(hand));
        }
        return super.use(level,player,hand);
    }
    public static void observe(ServerPlayer player,Cat cat,InteractionHand hand) {
        var genome=((CatGenetics)cat).absmod$getGenome();var stack=player.getItemInHand(hand);
        if(player.isShiftKeyDown()) {
            var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();tag.putString("selected_cat",cat.getUUID().toString());
            stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
            message(player,Component.translatable("cat.absmod.selected",cat.getDisplayName()));
        } else {
            var selected=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("selected_cat");
            if(!selected.isEmpty()) {
                try {
                    var entity=player.serverLevel().getEntity(UUID.fromString(selected));
                    if(entity instanceof Cat first && first!=cat && player.distanceToSqr(first)<=256) {
                        var firstGenome=((CatGenetics)first).absmod$getGenome();
                        message(player,Component.translatable("cat.absmod.pink_chance",firstGenome.pinkDenominator(genome)));
                        message(player,Component.translatable("cat.absmod.prediction",firstGenome.diluteChancePercent(genome)));
                        message(player,Component.translatable("cat.absmod.prediction_extended",firstGenome.chocolateChancePercent(genome),firstGenome.pointChancePercent(genome),String.format(java.util.Locale.ROOT,"%.2f",firstGenome.lilacPointChancePercent(genome))));
                    }
                    else message(player,Component.translatable("cat.absmod.select_nearby"));
                } catch(IllegalArgumentException ignored) { message(player,Component.translatable("cat.absmod.select_nearby")); }
            }
            message(player,Component.translatable("cat.absmod.observation",genome.fullNotation(),genome.generation(),Component.translatable("cat.absmod.pattern."+genome.pattern().replace(':','.'))));
            genome.coat().ifPresent(coat->message(player,Component.translatable("cat.absmod.coat",
                Component.translatable("cat.absmod.color."+CatCoat.COLORS[coat.color(genome)]),
                Component.translatable("cat.absmod.marking."+CatCoat.MARKINGS[coat.marking()]),
                Component.translatable("cat.absmod.white."+CatCoat.WHITES[coat.white()]),
                Component.translatable("cat.absmod.eyes."+CatCoat.EYES[coat.eyes()]))));
            if((genome.dilute() || genome.brown() || genome.pointed()) && !genome.supportedCoat())message(player,Component.translatable("cat.absmod.appearance_pending"));
            if(genome.firstParent().isPresent())message(player,Component.translatable("cat.absmod.parents",genome.firstParent().get().toString(),genome.secondParent().map(UUID::toString).orElse("?")));
        }
        com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_observe");
        if(genome.generation()>0) {
            if(genome.coat().isPresent()) {
                var coat=genome.coat().get();
                com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_palette");
                if(coat.marking()!=0)com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_patterns");
                if(coat.eyes()==3)com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_odd_eyes");
                if(coat.pink())com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_pink");
            }
            if(genome.supportedCoat()) {
                if(genome.brown() && genome.coat().map(c->c.family()==0 && !c.pink()).orElse(true))com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_chocolate");
                if(genome.brown() && genome.dilute() && genome.coat().map(c->c.family()==0 && !c.pink()).orElse(true))com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_lilac");
                if(genome.pointed())com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_point");
                if(genome.brown() && genome.dilute() && genome.pointed() && genome.coat().map(c->c.family()==0 && !c.pink()).orElse(true))com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_lilac_point");
            }
            com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_family");
            if(genome.dilute())com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_dilute");
        }
    }
    private static void message(ServerPlayer player,Component text){if(player.connection!=null)player.sendSystemMessage(text,false);}
}
