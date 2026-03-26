package com.wardwatch.client.screen;

import com.wardwatch.network.payload.SubmitSetupPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class PasswordSetupScreen extends AbstractPasswordScreen {
	public PasswordSetupScreen(BlockPos pos, String blockTitle) {
		super(Text.literal("Password-protected " + blockTitle + " setup"), pos, blockTitle);
	}

	@Override
	protected void submit() {
		String password = getPassword();
		if (password.isBlank()) {
			setStatus("Ingresa un codigo.");
			return;
		}

		ClientPlayNetworking.send(new SubmitSetupPayload(pos, password));
	}
}
