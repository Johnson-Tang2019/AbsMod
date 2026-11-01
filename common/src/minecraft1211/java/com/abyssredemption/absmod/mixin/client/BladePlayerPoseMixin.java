package com.abyssredemption.absmod.mixin.client;
import com.abyssredemption.absmod.client.animation.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerModel.class)
public abstract class BladePlayerPoseMixin {
    @Inject(method="setupAnim",at=@At("TAIL"))
    private void applyBladePose(LivingEntity entity,float walk,float speed,float age,float yaw,float pitch,CallbackInfo ci) {
        if(entity instanceof Player player) {
            var pose=BladeMotionPlayer.pose(player,age-player.tickCount);
            if(pose!=null) {
                var model=(PlayerModel<?>)(Object)this;
                BladeThirdPerson.apply(model,pose,player.getMainArm()==HumanoidArm.LEFT);
                model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);model.jacket.copyFrom(model.body);
            }
        }
    }
}
