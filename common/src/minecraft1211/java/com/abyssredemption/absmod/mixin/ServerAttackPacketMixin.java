package com.abyssredemption.absmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/** Raw attacks are rejected at Player.attack, after the vanilla packet thread handoff. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerAttackPacketMixin {}
