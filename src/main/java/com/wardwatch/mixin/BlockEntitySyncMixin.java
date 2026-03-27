package com.wardwatch.mixin;

import com.wardwatch.protection.PasswordProtected;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class BlockEntitySyncMixin {
	@Inject(method = "toUpdatePacket", at = @At("HEAD"), cancellable = true)
	private void wardWatch$toUpdatePacket(CallbackInfoReturnable<Packet<ClientPlayPacketListener>> cir) {
		if ((Object) this instanceof PasswordProtected protection) {
			cir.setReturnValue(BlockEntityUpdateS2CPacket.create((BlockEntity) (Object) this));
		}
	}

	@Inject(method = "toInitialChunkDataNbt", at = @At("HEAD"), cancellable = true)
	private void wardWatch$toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries, CallbackInfoReturnable<NbtCompound> cir) {
		if ((Object) this instanceof PasswordProtected protection) {
			cir.setReturnValue(((BlockEntity) (Object) this).createNbt(registries));
		}
	}
}
