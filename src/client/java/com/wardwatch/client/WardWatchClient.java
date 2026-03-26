package com.wardwatch.client;

import com.wardwatch.client.screen.AbstractPasswordScreen;
import com.wardwatch.client.screen.PasswordSetupScreen;
import com.wardwatch.client.screen.PasswordUnlockScreen;
import com.wardwatch.network.payload.OpenSetupPayload;
import com.wardwatch.network.payload.OpenUnlockPayload;
import com.wardwatch.network.payload.ProtectionResponsePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class WardWatchClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
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

			if (payload.success() && client.currentScreen instanceof AbstractPasswordScreen) {
				client.setScreen(null);
			}
		}));
	}
}
