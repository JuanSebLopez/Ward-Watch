package com.wardwatch.network.payload;

import com.wardwatch.WardWatchMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ProtectionResponsePayload(boolean success, String message) implements CustomPayload {
	public static final Id<ProtectionResponsePayload> ID = new Id<>(Identifier.of(WardWatchMod.MOD_ID, "protection_response"));
	public static final PacketCodec<RegistryByteBuf, ProtectionResponsePayload> CODEC = CustomPayload.codecOf(ProtectionResponsePayload::write, ProtectionResponsePayload::new);

	private ProtectionResponsePayload(RegistryByteBuf buf) {
		this(buf.readBoolean(), buf.readString(128));
	}

	private void write(RegistryByteBuf buf) {
		buf.writeBoolean(success);
		buf.writeString(message, 128);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
