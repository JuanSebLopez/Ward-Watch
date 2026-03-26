package com.wardwatch.network.payload;

import com.wardwatch.WardWatchMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SubmitUnlockPayload(BlockPos pos, String password) implements CustomPayload {
	public static final Id<SubmitUnlockPayload> ID = new Id<>(Identifier.of(WardWatchMod.MOD_ID, "submit_unlock"));
	public static final PacketCodec<RegistryByteBuf, SubmitUnlockPayload> CODEC = CustomPayload.codecOf(SubmitUnlockPayload::write, SubmitUnlockPayload::new);

	private SubmitUnlockPayload(RegistryByteBuf buf) {
		this(buf.readBlockPos(), buf.readString(32));
	}

	private void write(RegistryByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeString(password, 32);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
