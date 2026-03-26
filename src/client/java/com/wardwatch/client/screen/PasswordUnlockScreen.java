package com.wardwatch.client.screen;

import com.wardwatch.network.payload.SubmitUnlockPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class PasswordUnlockScreen extends AbstractPasswordScreen {
	public PasswordUnlockScreen(BlockPos pos, String blockTitle) {
		super(Text.literal("Password-protected " + blockTitle), pos, blockTitle);
	}

	@Override
	protected Text getPrimaryActionText() {
		return Text.literal("Open");
	}

	@Override
	protected void submit() {
		String password = getPassword();
		if (password.isBlank()) {
			setStatus("Ingresa un codigo.");
			return;
		}

		ClientPlayNetworking.send(new SubmitUnlockPayload(pos, password));
	}
}
