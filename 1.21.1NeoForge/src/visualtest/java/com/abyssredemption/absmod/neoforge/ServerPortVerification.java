package com.abyssredemption.absmod.neoforge;
import com.abyssredemption.absmod.cat.*;
import com.abyssredemption.absmod.item.*;
import com.abyssredemption.absmod.spirit.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/** Dedicated-server assertions; this source set is never packaged in release builds. */
@Mod(value="absmod",dist=Dist.DEDICATED_SERVER)
public final class ServerPortVerification {
    private int tick;
    public ServerPortVerification(){NeoForge.EVENT_BUS.addListener(this::tick);}
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private void tick(ServerTickEvent.Post event) {
        if(++tick!=60)return;
        var server=event.getServer();var level=server.overworld();
        try {
            var a=EntityType.CAT.create(level);var b=EntityType.CAT.create(level);
            var empty=java.util.Optional.<java.util.UUID>empty();
            ((CatGenetics)a).absmod$setGenome(new CatGenome(3,"minecraft:all_black",empty,empty,0,3,3));
            ((CatGenetics)b).absmod$setGenome(new CatGenome(3,"minecraft:all_black",empty,empty,0,3,3));
            var child=a.getBreedOffspring(level,b);var genome=((CatGenetics)child).absmod$getGenome();
            check(genome.dilute() && genome.brown() && genome.pointed() && child.getVariant().unwrapKey().orElseThrow().location().toString().equals(genome.appearance()) && genome.generation()==1 && genome.firstParent().get().equals(a.getUUID()),"Cat inheritance failed");
            var saved=new CompoundTag();child.saveWithoutId(saved);var restored=EntityType.CAT.create(level);restored.load(saved);
            check(((CatGenetics)restored).absmod$getGenome().equals(genome) && restored.getVariant().equals(child.getVariant()),"Cat NBT lineage failed");
            for(var stage:MeowBladeStage.values()) {
                var stack=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod",stage.itemId())));
                check(stack.getMaxDamage()==stage.durability(),"Stage durability mismatch: "+stage.itemId());
            }
            var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);
            player.setPos(0,100,0);level.getChunk(0,0);
            var blade=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("absmod","meow_blade")));
            var store=BladeSoulStore.forInitialization(server);BladeSoulService.initialize(store,blade,player.getUUID());
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,blade);
            blade.hurtAndBreak(blade.getMaxDamage()+10,level,player,item->{});
            check(!blade.isEmpty() && blade.getDamageValue()==blade.getMaxDamage(),"Broken blade was lost");
            blade.setDamageValue(0);
            var spirit=SpiritTransformationService.manifest(player);
            check(player.getMainHandItem().getItem() instanceof SpectralBladeItem,"Manifest projection missing");
            check(!spirit.hurt(level.damageSources().genericKill(),1000),"Spirit invulnerability failed");
            check(SpiritTransformationService.recallForAttack(player),"Attack recall failed");
            check(player.getMainHandItem().get(BladeSoulComponents.REFERENCE).equals(blade.get(BladeSoulComponents.REFERENCE)),"Recall identity changed");
            var grid=net.minecraft.world.item.crafting.CraftingInput.of(3,3,java.util.List.of(new ItemStack(Items.DIAMOND_SWORD),new ItemStack(Items.AMETHYST_SHARD),new ItemStack(Items.AMETHYST_SHARD),new ItemStack(Items.AMETHYST_SHARD),new ItemStack(Items.AMETHYST_SHARD),new ItemStack(Items.COD),new ItemStack(Items.PINK_DYE),ItemStack.EMPTY,ItemStack.EMPTY));
            var recipe=server.getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,grid,level).orElseThrow();
            check(recipe.value().assemble(grid,server.registryAccess()).getItem() instanceof MeowBladeItem,"Loaded crafting recipe failed");
            var file=dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;
            var chapter=file.getChapter(Long.parseUnsignedLong("1C4E810187B7DDC2",16));
            check(chapter!=null && chapter.getQuests().size()==33,"FTB chapter failed on dedicated server");
            long completed=file.getAllTeamData().stream().mapToLong(data->chapter.getQuests().stream().filter(data::isCompleted).count()).sum();
            check(completed>=13,"FTB saved progress not restored: "+completed);
            com.mojang.logging.LogUtils.getLogger().info("PORT_SERVER_PASS genetics, NBT, durability, transformation, invincibility, attack recall, loaded recipe; FTB persisted completed={}",completed);
        } catch(Throwable error) {
            com.mojang.logging.LogUtils.getLogger().error("PORT_SERVER_FAILED",error);
            throw error;
        } finally {server.halt(false);}
    }
}
