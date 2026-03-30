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
		int panelWidth = 236;
		int panelHeight = 244;
		int left = (width - panelWidth) / 2;
		int top = (height - panelHeight) / 2;
		int buttonWidth = 34;
		int buttonHeight = 24;
		int gapX = 18;
		int gapY = 10;

		drawPanel(context, left, top, panelWidth, panelHeight);
		int titleBottom = drawWrappedCenteredTitle(context, left, top, panelWidth, panelWidth - 24, 0xFF2A2A2A);
		int fieldX = left + 44;
		int fieldY = titleBottom + 10;
		int fieldWidth = 148;
		int gridStartX = left + 44;
		int gridStartY = fieldY + 32;
		drawField(context, fieldX, fieldY, fieldWidth, 18, withCursor(getMaskedPassword()));

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				int digit = row * 3 + col + 1;
				int x = gridStartX + col * (buttonWidth + gapX);
				int y = gridStartY + row * (buttonHeight + gapY);
				drawActionButton(context, x, y, buttonWidth, buttonHeight, Text.literal(Integer.toString(digit)), isInside(mouseX, mouseY, x, y, buttonWidth, buttonHeight), false);
			}
		}

		int zeroX = left + 44;
		int zeroY = gridStartY + 102;
		int zeroWidth = 100;
		int backX = left + 158;
		int backY = zeroY;
		int backWidth = 34;
		int openX = left + 58;
		int openY = zeroY + 30;
		int openWidth = 120;

		drawActionButton(context, zeroX, zeroY, zeroWidth, buttonHeight, Text.literal("0"), isInside(mouseX, mouseY, zeroX, zeroY, zeroWidth, buttonHeight), false);
		drawActionButton(context, backX, backY, backWidth, buttonHeight, Text.literal("<"), isInside(mouseX, mouseY, backX, backY, backWidth, buttonHeight), false);
		drawActionButton(context, openX, openY, openWidth, 22, statusMessage.isEmpty() ? Text.translatable("screen.ward_watch.open_button") : Text.literal(statusMessage), isInside(mouseX, mouseY, openX, openY, openWidth, 22), !statusMessage.isEmpty());
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int panelWidth = 236;
		int panelHeight = 244;
		int left = (width - panelWidth) / 2;
		int top = (height - panelHeight) / 2;
		int buttonWidth = 34;
		int buttonHeight = 24;
		int gapX = 18;
		int gapY = 10;
		int titleBottom = top + 10 + textRenderer.wrapLines(title, panelWidth - 24).size() * (textRenderer.fontHeight + 2);
		int fieldY = titleBottom + 10;
		int gridStartX = left + 44;
		int gridStartY = fieldY + 32;

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

		if (isInside(mouseX, mouseY, left + 44, gridStartY + 102, 100, buttonHeight)) {
			appendDigit("0");
			return true;
		}

		if (isInside(mouseX, mouseY, left + 158, gridStartY + 102, 34, buttonHeight)) {
			backspace();
			return true;
		}

		if (isInside(mouseX, mouseY, left + 58, gridStartY + 132, 120, 22)) {
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
