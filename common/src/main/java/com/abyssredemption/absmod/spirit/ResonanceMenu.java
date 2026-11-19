package com.abyssredemption.absmod.spirit;

import com.abyssredemption.absmod.item.MeowBladeItem;
import com.abyssredemption.absmod.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import java.util.*;

/** Server-owned input escrow with read-only preview slots and one commit per opening. */
public final class ResonanceMenu extends AbstractContainerMenu {
    public static final MenuType<ResonanceMenu> TYPE = new MenuType<>(ResonanceMenu::new,
            net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS);
    public static final int CONFIRM_BUTTON = 0;
    public static final int WIDTH = 216;
    public static final int HEIGHT = 226;
    private static final int INPUT_SLOTS = 9;
    private static final int PREVIEW_SLOT = 10;
    private static final int FIRST_COST_SLOT = 12;
    private static final int MAX_COST_SLOTS = 3;

    private final DataSlot affectionTenths = DataSlot.standalone();
    private final DataSlot completed = DataSlot.standalone();
    private final SimpleContainer contents=new SimpleContainer(27);
    private final Player viewer;
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
        super(TYPE, id);
        viewer = inventory.player;
        addDataSlot(affectionTenths);
        addDataSlot(completed);
        for (int i = 0; i < contents.getContainerSize(); i++) {
            final int position = i;
            boolean material = i > 0 && i < INPUT_SLOTS;
            boolean cost = i >= FIRST_COST_SLOT && i < FIRST_COST_SLOT + MAX_COST_SLOTS;
            int x = material ? 53 + (i - 1) * 18 : cost ? 53 + (i - FIRST_COST_SLOT) * 18 : 16;
            int y = i < INPUT_SLOTS ? 48 : 89;
            addSlot(new Slot(contents, i, x, y) {
                @Override public boolean isActive() {
                    return position < INPUT_SLOTS || position == PREVIEW_SLOT || cost;
                }
                @Override public boolean mayPlace(ItemStack stack) {
                    return position < INPUT_SLOTS && (position != 0 || stack.getItem() instanceof MeowBladeItem);
                }
                @Override public boolean mayPickup(Player player) {
                    return position < INPUT_SLOTS;
                }
                @Override public int getMaxStackSize() {
                    return position == 0 ? 1 : super.getMaxStackSize();
                }
            });
        }
        addStandardInventorySlots(inventory, 28, 146);
    }

    public double currentAffection() {
        return affectionTenths.get() < 0 ? -1 : affectionTenths.get() / 10.0;
    }

    public boolean isComplete() {
        return completed.get() != 0;
    }

    public boolean hasMaterials(List<Cost> costs) {
        for (var cost : costs) {
            int total = 0;
            for (int slot = 1; slot < INPUT_SLOTS; slot++) {
                if (contents.getItem(slot).is(cost.item())) {
                    total += contents.getItem(slot).getCount();
                }
            }
            if (total < cost.count()) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack label(Item item,String key,Object... args) {
        var stack=new ItemStack(item);stack.set(DataComponents.CUSTOM_NAME,Component.translatable(key,args));return stack;
    }
    @Override public void broadcastChanges() {
        if (viewer instanceof ServerPlayer player) {
            updatePreview(player);
        }
        super.broadcastChanges();
    }

    private void updatePreview(ServerPlayer player) {
        affectionTenths.set(0);
        contents.setItem(PREVIEW_SLOT, ItemStack.EMPTY);
        for (int i = FIRST_COST_SLOT; i < FIRST_COST_SLOT + MAX_COST_SLOTS; i++) {
            contents.setItem(i, ItemStack.EMPTY);
        }
        var sword = contents.getItem(0);
        if (!(sword.getItem() instanceof MeowBladeItem blade)) {
            return;
        }
        if (sword.has(BladeSoulComponents.REFERENCE)) {
            try {
                var store = BladeSoulStore.find(player.level().getServer());
                var record = store.map(saved -> BladeSoulService.requireRecord(saved, sword));
                int value = record.map(saved -> (int) Math.floor(saved.affection().orElse(0D) * 10)).orElse(-1);
                affectionTenths.set(Math.clamp(value, -1, Short.MAX_VALUE));
            } catch (IllegalStateException error) {
                affectionTenths.set(-1);
            }
        }
        int target = blade.stage().number() + 1;
        var route = blade.stage().route();
        if (target >= 9) {
            contents.setItem(PREVIEW_SLOT, label(Items.BARRIER, route == BladeRoute.DARK
                    ? "resonance.absmod.max_stage" : "resonance.absmod.boss_locked"));
            return;
        }
        var stage = BladeStage.of(route, target);
        var next = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod", stage.itemId()));
        contents.setItem(PREVIEW_SLOT, label(next, "resonance.absmod.preview", target,
                stage.attackDamage(), stage.baseAttackSpeed(), affection(route, target)));
        int slot = FIRST_COST_SLOT;
        for (var cost : costs(route, target)) {
            contents.setItem(slot++, new ItemStack(cost.item(), cost.count()));
        }
    }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (button != CONFIRM_BUTTON || !(player instanceof ServerPlayer serverPlayer) || isComplete()) {
            return false;
        }
        upgrade(serverPlayer);
        broadcastChanges();
        broadcastFullState();
        return true;
    }

    @Override public void clicked(int slot, int button, ContainerInput input, Player player) {
        if (slot >= INPUT_SLOTS && slot < contents.getContainerSize()) {
            return;
        }
        super.clicked(slot, button, input, player);
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
            var costs = costs(blade.stage().route(), target);
            if (!hasMaterials(costs)) {
                throw new IllegalStateException("materials");
            }
            var next=(MeowBladeItem)BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("absmod",BladeStage.of(blade.stage().route(),target).itemId()));
            var result=BladeComponentTransfer.transfer(sword,next);
            store.replace(before,before.withStage(target));
            for(var cost:costs) {
                int remaining=cost.count;
                for(int i=1;i<9 && remaining>0;i++)if(contents.getItem(i).is(cost.item)) {
                    int used=Math.min(remaining,contents.getItem(i).getCount());contents.getItem(i).shrink(used);remaining-=used;
                }
            }
            contents.setItem(0, result);
            completed.set(1);
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
