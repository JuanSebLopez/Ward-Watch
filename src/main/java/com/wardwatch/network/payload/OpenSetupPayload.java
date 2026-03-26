package com.wardwatch.network.payload;

import com.wardwatch.WardWatchMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record OpenSetupPayload(BlockPos pos, String title) implements CustomPayload {
	public static final Id<OpenSetupPayload> ID = new Id<>(Identifier.of(WardWatchMod.MOD_ID, "open_setup"));
	public static final PacketCodec<RegistryByteBuf, OpenSetupPayload> CODEC = CustomPayload.codecOf(OpenSetupPayload::write, OpenSetupPayload::new);

	private OpenSetupPayload(RegistryByteBuf buf) {
		this(buf.readBlockPos(), buf.readString(64));
	}

	private void write(RegistryByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeString(title, 64);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
