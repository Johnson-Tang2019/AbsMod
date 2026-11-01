package com.abyssredemption.absmod.mixin;

import com.abyssredemption.absmod.combat.BladeAttackScope;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Only the primary authorized hurt call bypasses shared cooldown, without rewriting its history. */
@Mixin(LivingEntity.class)
public abstract class LivingDamageMixin {
    @Shadow protected float lastHurt;
    @Unique private boolean absmod$primaryDamage;
    @Unique private float absmod$nestedLoss;
    @Unique private int absmod$damageDepth;

    @WrapMethod(method = "hurt")
    private boolean absmod$damageScope(DamageSource source, float damage, Operation<Boolean> original) {
        boolean previous = absmod$primaryDamage;
        absmod$damageDepth++;
        float parentNestedLoss = absmod$nestedLoss;
        absmod$nestedLoss = 0;
        var entity = (LivingEntity) (Object) this;
        float before = entity.getHealth() + entity.getAbsorptionAmount();
        absmod$primaryDamage = BladeAttackScope.claimPrimary(this, source);
        try {
            boolean accepted = original.call(source, damage);
            if (absmod$primaryDamage) {
                float loss = before - entity.getHealth() - entity.getAbsorptionAmount() - absmod$nestedLoss;
                BladeAttackScope.recordPrimaryLoss(this, source, accepted, loss);
            }
            return accepted;
        } finally {
            absmod$damageDepth--;
            absmod$nestedLoss = absmod$damageDepth == 0 ? 0
                    : parentNestedLoss + Math.max(0, before - entity.getHealth() - entity.getAbsorptionAmount());
            absmod$primaryDamage = previous;
        }
    }

    @Redirect(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", opcode = Opcodes.GETFIELD))
    private int absmod$readCooldown(LivingEntity entity) { return absmod$primaryDamage ? 0 : entity.invulnerableTime; }

    @Redirect(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", opcode = Opcodes.PUTFIELD))
    private void absmod$writeCooldown(LivingEntity entity, int value) {
        if (!absmod$primaryDamage) { entity.invulnerableTime = value; }
    }

    @Redirect(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;lastHurt:F", opcode = Opcodes.PUTFIELD))
    private void absmod$writeLastHurt(LivingEntity entity, float value) {
        if (!absmod$primaryDamage) { lastHurt = value; }
    }
}
