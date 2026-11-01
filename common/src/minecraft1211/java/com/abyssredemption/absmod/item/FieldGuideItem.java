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
        for(int i=1;i<=16;i++)pages.add(Filterable.passThrough(Component.translatable("guide.absmod.page."+i)));
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
                    if(entity instanceof Cat first && first!=cat && player.distanceToSqr(first)<=256)
                        message(player,Component.translatable("cat.absmod.prediction",((CatGenetics)first).absmod$getGenome().diluteChancePercent(genome)));
                    else message(player,Component.translatable("cat.absmod.select_nearby"));
                } catch(IllegalArgumentException ignored) { message(player,Component.translatable("cat.absmod.select_nearby")); }
            }
            message(player,Component.translatable("cat.absmod.observation",genome.notation(),genome.generation(),Component.translatable("cat.absmod.pattern."+genome.pattern().replace(':','.'))));
            if(genome.dilute() && !genome.pattern().equals("minecraft:all_black"))message(player,Component.translatable("cat.absmod.appearance_pending"));
            if(genome.firstParent().isPresent())message(player,Component.translatable("cat.absmod.parents",genome.firstParent().get().toString(),genome.secondParent().map(UUID::toString).orElse("?")));
        }
        com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_observe");
        if(genome.generation()>0) {
            com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_family");
            if(genome.dilute())com.abyssredemption.absmod.spirit.BladeAdvancements.award(player,"cat_dilute");
        }
    }
    private static void message(ServerPlayer player,Component text){if(player.connection!=null)player.sendSystemMessage(text,false);}
}
