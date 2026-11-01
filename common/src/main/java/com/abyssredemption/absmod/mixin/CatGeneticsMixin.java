package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.cat.*;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Cat.class)
public abstract class CatGeneticsMixin implements CatGenetics {
    @Shadow private void setVariant(net.minecraft.core.Holder<net.minecraft.world.entity.animal.feline.CatVariant> variant){throw new AssertionError();}
    @Unique private CatGenome absmod$genome;
    @Override public CatGenome absmod$getGenome() {
        if(absmod$genome==null) {
            var cat=(Cat)(Object)this;
            String pattern=cat.getVariant().unwrapKey().orElseThrow().identifier().toString();
            absmod$genome=CatGenome.founder(pattern,cat.getUUID());
        }
        return absmod$genome;
    }
    @Override public void absmod$setGenome(CatGenome genome) {
        absmod$genome=genome;
        var cat=(Cat)(Object)this;
        String appearance=genome.appearance();
        cat.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.CAT_VARIANT)
            .get(net.minecraft.resources.Identifier.parse(appearance)).ifPresent(this::setVariant);
    }
    @Inject(method="addAdditionalSaveData",at=@At("TAIL"))
    private void saveGenome(ValueOutput output,CallbackInfo callback) {
        if(absmod$genome!=null)output.store("absmod_genome",CatGenome.CODEC,absmod$genome);
    }
    @Inject(method="readAdditionalSaveData",at=@At("TAIL"))
    private void loadGenome(ValueInput input,CallbackInfo callback) {
        absmod$genome=input.read("absmod_genome",CatGenome.CODEC).orElse(null);
        // Keep the saved visual variant; old cats are never recolored merely by loading.
    }
    @Inject(method="getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/animal/feline/Cat;",at=@At("RETURN"))
    private void inheritGenome(ServerLevel level,AgeableMob other,CallbackInfoReturnable<Cat> callback) {
        var child=callback.getReturnValue();var self=(Cat)(Object)this;
        if(child!=null && other instanceof Cat partner)
            ((CatGenetics)child).absmod$setGenome(absmod$getGenome().breed(((CatGenetics)partner).absmod$getGenome(),self.getUUID(),partner.getUUID(),self.getRandom()));
    }
    @Inject(method="mobInteract",at=@At("HEAD"),cancellable=true)
    private void observe(net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,CallbackInfoReturnable<net.minecraft.world.InteractionResult> callback) {
        if(player.getItemInHand(hand).getItem() instanceof com.abyssredemption.absmod.item.FieldGuideItem) {
            if(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                com.abyssredemption.absmod.item.FieldGuideItem.observe(serverPlayer,(Cat)(Object)this,hand);
            callback.setReturnValue(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }
}
