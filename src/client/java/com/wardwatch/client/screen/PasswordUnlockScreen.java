package com.wardwatch.client.screen;

import com.wardwatch.network.payload.SubmitUnlockPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class PasswordUnlockScreen extends AbstractPasswordScreen {
	public PasswordUnlockScreen(BlockPos pos, String blockTranslationKey) {
		super(Text.translatable("screen.ward_watch.unlock_title", Text.translatable(blockTranslationKey)), pos, blockTranslationKey);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int panelWidth = 210;
		int panelHeight = 218;
		int left = (width - panelWidth) / 2;
		int top = (height - panelHeight) / 2;
		int fieldX = left + 39;
		int fieldY = top + 28;
		int fieldWidth = 132;
		int buttonWidth = 30;
		int buttonHeight = 24;
		int gridStartX = left + 39;
		int gridStartY = top + 60;
		int gapX = 21;
		int gapY = 10;

		drawPanel(context, left, top, panelWidth, panelHeight);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top + 10, 0xFF2A2A2A);
		drawField(context, fieldX, fieldY, fieldWidth, 18, withCursor(getMaskedPassword()));

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				int digit = row * 3 + col + 1;
				int x = gridStartX + col * (buttonWidth + gapX);
				int y = gridStartY + row * (buttonHeight + gapY);
				drawActionButton(context, x, y, buttonWidth, buttonHeight, Text.literal(Integer.toString(digit)), isInside(mouseX, mouseY, x, y, buttonWidth, buttonHeight), false);
			}
		}

		int zeroX = left + 39;
		int zeroY = top + 162;
		int zeroWidth = 88;
		int backX = left + 141;
		int backY = zeroY;
		int backWidth = 30;
		int openX = left + 57;
		int openY = top + 190;
		int openWidth = 96;

		drawActionButton(context, zeroX, zeroY, zeroWidth, buttonHeight, Text.literal("0"), isInside(mouseX, mouseY, zeroX, zeroY, zeroWidth, buttonHeight), false);
		drawActionButton(context, backX, backY, backWidth, buttonHeight, Text.literal("<"), isInside(mouseX, mouseY, backX, backY, backWidth, buttonHeight), false);
		drawActionButton(context, openX, openY, openWidth, 22, statusMessage.isEmpty() ? Text.translatable("screen.ward_watch.open_button") : Text.literal(statusMessage), isInside(mouseX, mouseY, openX, openY, openWidth, 22), !statusMessage.isEmpty());
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int panelWidth = 210;
		int panelHeight = 218;
		int left = (width - panelWidth) / 2;
		int top = (height - panelHeight) / 2;
		int buttonWidth = 30;
		int buttonHeight = 24;
		int gridStartX = left + 39;
		int gridStartY = top + 60;
		int gapX = 21;
		int gapY = 10;

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				int digit = row * 3 + col + 1;
				int x = gridStartX + col * (buttonWidth + gapX);
				int y = gridStartY + row * (buttonHeight + gapY);
				if (isInside(mouseX, mouseY, x, y, buttonWidth, buttonHeight)) {
					appendDigit(Integer.toString(digit));
					return true;
				}
			}
		}

		if (isInside(mouseX, mouseY, left + 39, top + 162, 88, buttonHeight)) {
			appendDigit("0");
			return true;
		}

		if (isInside(mouseX, mouseY, left + 141, top + 162, 30, buttonHeight)) {
			backspace();
			return true;
		}

		if (isInside(mouseX, mouseY, left + 57, top + 190, 96, 22)) {
			submit();
			return true;
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public void handleServerResponse(boolean success, String message) {
		super.handleServerResponse(success, message);
		if (!success) {
			clearPassword();
		}
	}

	@Override
	protected void submit() {
		String password = getPassword();
		if (password.isBlank()) {
			setStatus(Text.translatable("screen.ward_watch.invalid_code").getString());
			return;
		}

		ClientPlayNetworking.send(new SubmitUnlockPayload(pos, password));
	}
}
