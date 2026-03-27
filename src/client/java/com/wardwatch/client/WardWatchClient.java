package com.wardwatch.client;

import com.wardwatch.client.render.ProtectedChestBlockEntityRenderer;
import com.wardwatch.client.render.ProtectedFurnaceBlockEntityRenderer;
import com.wardwatch.client.screen.AbstractPasswordScreen;
import com.wardwatch.client.screen.PasswordSetupScreen;
import com.wardwatch.client.screen.PasswordUnlockScreen;
import com.wardwatch.network.payload.OpenSetupPayload;
import com.wardwatch.network.payload.OpenUnlockPayload;
import com.wardwatch.network.payload.ProtectionResponsePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.text.Text;

public class WardWatchClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(BlockEntityType.CHEST, ProtectedChestBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(BlockEntityType.TRAPPED_CHEST, context -> new ProtectedChestBlockEntityRenderer(context));
		BlockEntityRendererFactories.register(BlockEntityType.FURNACE, context -> new ProtectedFurnaceBlockEntityRenderer<>(context));
		BlockEntityRendererFactories.register(BlockEntityType.BLAST_FURNACE, context -> new ProtectedFurnaceBlockEntityRenderer<>(context));
		BlockEntityRendererFactories.register(BlockEntityType.SMOKER, context -> new ProtectedFurnaceBlockEntityRenderer<>(context));

		ClientPlayNetworking.registerGlobalReceiver(OpenSetupPayload.ID, (payload, context) ->
			context.client().execute(() -> context.client().setScreen(new PasswordSetupScreen(payload.pos(), payload.title())))
		);

		ClientPlayNetworking.registerGlobalReceiver(OpenUnlockPayload.ID, (payload, context) ->
			context.client().execute(() -> context.client().setScreen(new PasswordUnlockScreen(payload.pos(), payload.title())))
		);

		ClientPlayNetworking.registerGlobalReceiver(ProtectionResponsePayload.ID, (payload, context) -> context.client().execute(() -> {
			MinecraftClient client = context.client();
			if (client.player != null) {
				client.player.sendMessage(Text.literal(payload.message()), true);
			}

			if (client.currentScreen instanceof AbstractPasswordScreen passwordScreen) {
				if (payload.success()) {
					client.setScreen(null);
				} else {
					passwordScreen.handleServerResponse(false, payload.message());
				}
			}
		}));
	}
}
