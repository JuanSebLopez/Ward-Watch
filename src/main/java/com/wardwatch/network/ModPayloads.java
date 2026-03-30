package com.wardwatch.network;

import com.wardwatch.block.ModBlocks;
import com.wardwatch.item.ModItems;
import com.wardwatch.network.payload.OpenSetupPayload;
import com.wardwatch.network.payload.OpenUnlockPayload;
import com.wardwatch.network.payload.ProtectionResponsePayload;
import com.wardwatch.network.payload.SubmitSetupPayload;
import com.wardwatch.network.payload.SubmitUnlockPayload;
import com.wardwatch.protection.ProtectionManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ModPayloads {
	private ModPayloads() {
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(OpenSetupPayload.ID, OpenSetupPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenUnlockPayload.ID, OpenUnlockPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ProtectionResponsePayload.ID, ProtectionResponsePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SubmitSetupPayload.ID, SubmitSetupPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SubmitUnlockPayload.ID, SubmitUnlockPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SubmitSetupPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			boolean consumesProtector = !player.getEntityWorld().getBlockState(payload.pos()).isOf(ModBlocks.PROTECTED_DOOR);
			if (consumesProtector && !player.getMainHandStack().isOf(ModItems.PASSWORD_PROTECTOR)) {
				sendResult(player, false, "Necesitas el Password Protector en la mano principal.");
				return;
			}

			if (!ProtectionManager.isValidPassword(payload.password())) {
				sendResult(player, false, "La clave debe ser numerica y de hasta 6 digitos.");
				return;
			}

			if (!ProtectionManager.applyProtection(player, payload.pos(), payload.password())) {
				sendResult(player, false, "No se pudo proteger este bloque.");
				return;
			}

			if (consumesProtector) {
				player.getMainHandStack().decrement(1);
			}
			sendResult(player, true, "Bloque protegido correctamente.");
		});

		ServerPlayNetworking.registerGlobalReceiver(SubmitUnlockPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			if (!ProtectionManager.checkPassword(player.getEntityWorld(), payload.pos(), payload.password())) {
				sendResult(player, false, "Contrasena incorrecta.");
				return;
			}

			if (!ProtectionManager.openProtectedBlock(player, payload.pos())) {
				sendResult(player, false, "No se pudo abrir el bloque.");
				return;
			}

			sendResult(player, true, "Acceso concedido.");
		});
	}

	public static void openSetupScreen(ServerPlayerEntity player, BlockPos pos, String title) {
		ServerPlayNetworking.send(player, new OpenSetupPayload(pos, title));
	}

	public static void openUnlockScreen(ServerPlayerEntity player, BlockPos pos, String title) {
		ServerPlayNetworking.send(player, new OpenUnlockPayload(pos, title));
	}

	private static void sendResult(ServerPlayerEntity player, boolean success, String message) {
		ServerPlayNetworking.send(player, new ProtectionResponsePayload(success, message));
		player.sendMessage(Text.literal(message), true);
	}
}
