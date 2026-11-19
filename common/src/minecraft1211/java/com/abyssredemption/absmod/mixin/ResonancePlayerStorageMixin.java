package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.spirit.ResonanceEscrow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
    private void absmod$saveResonance(CompoundTag output, CallbackInfo callback) {
        var saved = absmod$resonanceEscrow.save((ServerPlayer) (Object) this);
        if (saved != null) {
            output.put(ResonanceEscrow.KEY, saved);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void absmod$loadResonance(CompoundTag input, CallbackInfo callback) {
        absmod$resonanceEscrow.load((ServerPlayer) (Object) this,
                input.get(ResonanceEscrow.KEY) instanceof CompoundTag saved ? saved : null);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void absmod$restoreResonance(ServerPlayer previous, boolean alive, CallbackInfo callback) {
        var saved = ((ResonanceEscrow.Holder) previous).absmod$resonance().save(previous);
        absmod$resonanceEscrow.load((ServerPlayer) (Object) this, saved);
    }
}
