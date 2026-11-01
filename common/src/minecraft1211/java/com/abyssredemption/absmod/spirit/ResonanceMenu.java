package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import java.util.*;

/** Server-owned input escrow with read-only preview slots and one commit per opening. */
public final class ResonanceMenu extends AbstractContainerMenu {
    private final SimpleContainer contents=new SimpleContainer(27);
    private final Player viewer;
    private boolean committed;
    private static final int[] AFFECTION={0,0,10,25,45,70,100,140,190,250};
    public record Cost(Item item,int count){}
    public static List<Cost> costs(int target) {
        return switch(target) {
            case 2 -> List.of(new Cost(Items.AMETHYST_SHARD,8),new Cost(Items.COPPER_INGOT,8));
            case 3 -> List.of(new Cost(Items.DIAMOND,2),new Cost(Items.LAPIS_LAZULI,16));
            case 4 -> List.of(new Cost(Items.AMETHYST_BLOCK,4),new Cost(Items.ENDER_PEARL,4));
            case 5 -> List.of(new Cost(Items.DIAMOND,4),new Cost(Items.BLAZE_ROD,4));
            case 6 -> List.of(new Cost(Items.GHAST_TEAR,2),new Cost(Items.PHANTOM_MEMBRANE,4),new Cost(Items.AMETHYST_BLOCK,8));
            case 7 -> List.of(new Cost(Items.CHORUS_FRUIT,16),new Cost(Items.SHULKER_SHELL,2),new Cost(Items.DIAMOND,4));
            case 8 -> List.of(new Cost(Items.NETHER_STAR,1),new Cost(Items.HEART_OF_THE_SEA,1));
            default -> List.of();
        };
    }
    public static List<Cost> costs(BladeRoute route,int target) {
        if(route==BladeRoute.NORMAL)return costs(target);
        return switch(target) {
            case 2 -> List.of(new Cost(Items.OBSIDIAN,8));
            case 3 -> List.of(new Cost(Items.GOLD_INGOT,8),new Cost(Items.BLAZE_ROD,2));
            case 4 -> List.of(new Cost(Items.CRYING_OBSIDIAN,8));
            case 5 -> List.of(new Cost(Items.GHAST_TEAR,2));
            case 6 -> List.of(new Cost(Items.NETHERITE_SCRAP,2));
            case 7 -> List.of(new Cost(Items.BLAZE_ROD,8),new Cost(Items.ENDER_PEARL,8));
            case 8 -> List.of(new Cost(Items.NETHERITE_INGOT,1));
            default -> List.of();
        };
    }
    public static int affection(BladeRoute route,int target) {
        return route==BladeRoute.NORMAL?AFFECTION[target]:new int[]{0,0,5,12,22,35,50,70,100}[target];
    }
    public ResonanceMenu(int id,Inventory inventory) {
        super(MenuType.GENERIC_9x3,id);
        viewer=inventory.player;
        for(int i=0;i<27;i++) {
            final int position=i;
            addSlot(new Slot(contents,i,8+(i%9)*18,18+(i/9)*18) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return position<9 && (position!=0 || stack.getItem() instanceof MeowBladeItem);
                }
                @Override public boolean mayPickup(Player player){return position<9;}
                @Override public int getMaxStackSize(){return position==0?1:64;}
            });
        }
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlot(new Slot(inventory,column+row*9+9,8+column*18,85+row*18));
        for(int column=0;column<9;column++)addSlot(new Slot(inventory,column,8+column*18,143));
        for(int i=9;i<27;i++)contents.setItem(i,label(Items.GLASS_PANE,"resonance.absmod.inputs"));
        contents.setItem(22,label(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","resonance_heart")),"resonance.absmod.confirm"));
    }
    private static ItemStack label(Item item,String key,Object... args) {
        var stack=new ItemStack(item);stack.set(DataComponents.CUSTOM_NAME,Component.translatable(key,args));return stack;
    }
    @Override public void broadcastChanges() {
        var sword=contents.getItem(0);
        contents.setItem(18,ItemStack.EMPTY);
        contents.setItem(22,label(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","resonance_heart")),
                committed?"resonance.absmod.reopen":"resonance.absmod.confirm"));
        for(int i=10;i<18;i++)contents.setItem(i,ItemStack.EMPTY);
        if(sword.getItem() instanceof MeowBladeItem blade) {
            double affection=0;
            if(viewer instanceof ServerPlayer player && sword.has(BladeSoulComponents.REFERENCE)) {
                try {
                    var store=BladeSoulStore.find(player.level().getServer());
                    if(store.isPresent())affection=BladeSoulService.requireRecord(store.get(),sword).affection().orElse(0D);
                } catch(IllegalStateException error) { affection=-1; }
            }
            contents.setItem(18,label(Items.PAPER,"resonance.absmod.current_affection",affection));
            int target=blade.stage().number()+1;
            if(target<9) {
                var stage=BladeStage.of(blade.stage().route(),target);
                contents.setItem(10,label(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod",stage.itemId())),"resonance.absmod.preview",target,stage.attackDamage(),stage.baseAttackSpeed(),affection(blade.stage().route(),target)));
                int slot=12;for(var cost:costs(blade.stage().route(),target)){var icon=new ItemStack(cost.item,cost.count);contents.setItem(slot++,icon);}
            } else contents.setItem(10,label(Items.BARRIER,blade.stage().route()==BladeRoute.DARK?"resonance.absmod.max_stage":"resonance.absmod.boss_locked"));
        }
        super.broadcastChanges();
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player) {
        if(slot>=9 && slot<27) {
            if(slot==22 && type==ClickType.PICKUP && button==0 && player instanceof ServerPlayer serverPlayer && !committed)upgrade(serverPlayer);
            broadcastChanges();broadcastFullState();return;
        }
        super.clicked(slot,button,type,player);
    }
    private void upgrade(ServerPlayer player) {
        try {
            var sword=contents.getItem(0);
            if(sword.getCount()!=1 || !(sword.getItem() instanceof MeowBladeItem blade))throw new IllegalStateException("not_blade");
            int target=blade.stage().number()+1;
            if(target>=9)throw new IllegalStateException(blade.stage().route()==BladeRoute.DARK?"max_stage":"boss_locked");
            var store=BladeSoulStore.forInitialization(player.level().getServer());
            var before=BladeSoulService.initialize(store,sword,player.getUUID());
            if(!before.ownerId().filter(player.getUUID()::equals).isPresent())throw new IllegalStateException("not_owner");
            if(before.affection().orElse(0D)<affection(blade.stage().route(),target))throw new IllegalStateException("affection");
            for(var cost:costs(blade.stage().route(),target)) {
                int count=0;for(int i=1;i<9;i++)if(contents.getItem(i).is(cost.item))count+=contents.getItem(i).getCount();
                if(count<cost.count)throw new IllegalStateException("materials");
            }
            var next=(MeowBladeItem)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod",BladeStage.of(blade.stage().route(),target).itemId()));
            var result=BladeComponentTransfer.transfer(sword,next);
            store.replace(before,before.withStage(target));
            for(var cost:costs(blade.stage().route(),target)) {
                int remaining=cost.count;
                for(int i=1;i<9 && remaining>0;i++)if(contents.getItem(i).is(cost.item)) {
                    int used=Math.min(remaining,contents.getItem(i).getCount());contents.getItem(i).shrink(used);remaining-=used;
                }
            }
            contents.setItem(0,result);committed=true;
            BladeAdvancements.award(player,blade.stage().route().getSerializedName()+"_stage_"+target);
            if(player.connection!=null)player.sendSystemMessage(Component.translatable("resonance.absmod.success"),false);
        } catch(IllegalStateException error) {
            if(player.connection!=null)player.sendSystemMessage(Component.translatable("resonance.absmod."+error.getMessage()),false);
        }
    }
    @Override public boolean stillValid(Player player){return player.isAlive();}
    @Override public ItemStack quickMoveStack(Player player,int slot) {
        if(slot<0 || slot>=slots.size() || slot>=9 && slot<27)return ItemStack.EMPTY;
        var source=slots.get(slot);if(!source.hasItem())return ItemStack.EMPTY;
        var stack=source.getItem();var copy=stack.copy();
        boolean moved=slot<9?moveItemStackTo(stack,27,slots.size(),true):
            stack.getItem() instanceof MeowBladeItem?moveItemStackTo(stack,0,1,false):moveItemStackTo(stack,1,9,false);
        if(!moved)return ItemStack.EMPTY;
        source.setChanged();return copy;
    }
    @Override public void removed(Player player) {
        super.removed(player);
        var inputs=new SimpleContainer(9);
        for(int i=0;i<9;i++)inputs.setItem(i,contents.removeItemNoUpdate(i));
        clearContainer(player,inputs);
    }
}
