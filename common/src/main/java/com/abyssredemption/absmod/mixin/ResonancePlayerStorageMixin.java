package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.spirit.ResonanceEscrow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ResonancePlayerStorageMixin implements ResonanceEscrow.Holder {
    @Unique private final ResonanceEscrow absmod$resonanceEscrow = new ResonanceEscrow();

    @Override public ResonanceEscrow absmod$resonance() {
        return absmod$resonanceEscrow;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void absmod$saveResonance(ValueOutput output, CallbackInfo callback) {
        var saved = absmod$resonanceEscrow.save((ServerPlayer) (Object) this);
        if (saved != null) {
            output.store(ResonanceEscrow.KEY, CompoundTag.CODEC, saved);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void absmod$loadResonance(ValueInput input, CallbackInfo callback) {
        absmod$resonanceEscrow.load((ServerPlayer) (Object) this,
                input.read(ResonanceEscrow.KEY, CompoundTag.CODEC).orElse(null));
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void absmod$restoreResonance(ServerPlayer previous, boolean alive, CallbackInfo callback) {
        var saved = ((ResonanceEscrow.Holder) previous).absmod$resonance().save(previous);
        absmod$resonanceEscrow.load((ServerPlayer) (Object) this, saved);
    }
}
