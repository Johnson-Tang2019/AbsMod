package com.abyssredemption.absmod.mixin.client;

import com.abyssredemption.absmod.client.animation.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class BladeFirstPersonMixin {
    @Shadow public abstract void renderItem(LivingEntity entity,ItemStack item,ItemDisplayContext context,boolean left,PoseStack pose,MultiBufferSource collector,int light);
    @Inject(method="renderArmWithItem",at=@At("HEAD"),cancellable=true)
    private void bladeMotion(AbstractClientPlayer player,float partial,float pitch,InteractionHand hand,float attack,
            ItemStack item,float equip,PoseStack stack,MultiBufferSource collector,int light,CallbackInfo ci) {
        if(!BladeMotionPlayer.isReady() || hand!=InteractionHand.MAIN_HAND || !(item.getItem() instanceof com.abyssredemption.absmod.item.MeowBladeItem)) { return; }
        var pose=BladeMotionPlayer.pose(player,partial);
        var client=net.minecraft.client.Minecraft.getInstance();
        if(player.isUsingItem() || client.screen!=null || client.gameMode==null || client.gameMode.isDestroying()) { return; }
        if(pose==null && !com.abyssredemption.absmod.client.BladeCombatClient.isAvailable()) { return; }
        stack.pushPose();
        // Preserve the equip transition instead of popping the blade into its resting grip.
        stack.translate(0,-.6F*equip,0);
        BladeGrip.firstPerson(stack,pose,player.getMainArm());
        renderItem(player,item,ItemDisplayContext.NONE,false,stack,collector,light);
        stack.popPose();
        ci.cancel();
    }
}
