package com.wardwatch.network.payload;

import com.wardwatch.WardWatchMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SubmitSetupPayload(BlockPos pos, String password) implements CustomPayload {
	public static final Id<SubmitSetupPayload> ID = new Id<>(Identifier.of(WardWatchMod.MOD_ID, "submit_setup"));
	public static final PacketCodec<RegistryByteBuf, SubmitSetupPayload> CODEC = CustomPayload.codecOf(SubmitSetupPayload::write, SubmitSetupPayload::new);

	private SubmitSetupPayload(RegistryByteBuf buf) {
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
