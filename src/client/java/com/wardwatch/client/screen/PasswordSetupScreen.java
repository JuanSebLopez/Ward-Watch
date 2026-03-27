package com.wardwatch.client.screen;

import com.wardwatch.network.payload.SubmitSetupPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class PasswordSetupScreen extends AbstractPasswordScreen {
	private boolean blurPassword = false;

	public PasswordSetupScreen(BlockPos pos, String blockTranslationKey) {
		super(Text.translatable("screen.ward_watch.setup_title", Text.translatable(blockTranslationKey)), pos,
				blockTranslationKey);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int panelWidth = 240;
		int panelHeight = 155;
		int left = (width - panelWidth) / 2;
		int top = (height - panelHeight) / 2;

		drawPanel(context, left, top, panelWidth, panelHeight);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top + 10, 0xFF2A2A2A);
		context.drawText(textRenderer, Text.translatable("screen.ward_watch.code_label"), left + 18, top + 40,
				0xFF1E1E1E, false);

		String visiblePassword = blurPassword ? getMaskedPassword() : getPassword();
		drawField(context, left + 58, top + 34, 112, 18, withCursor(visiblePassword));

		int blurX = left + 56;
		int blurY = top + 82;
		int blurWidth = 118;
		int blurHeight = 20;
		int actionX = left + 56;
		int actionY = top + 112;
		int actionWidth = 118;
		int actionHeight = 20;

		drawActionButton(context, blurX, blurY, blurWidth, blurHeight,
				Text.translatable(blurPassword ? "screen.ward_watch.show_password" : "screen.ward_watch.blur_password"),
				isInside(mouseX, mouseY, blurX, blurY, blurWidth, blurHeight), false);
		drawActionButton(context, actionX, actionY, actionWidth, actionHeight,
				statusMessage.isEmpty() ? Text.translatable("screen.ward_watch.setup_button")
						: Text.literal(statusMessage),
				isInside(mouseX, mouseY, actionX, actionY, actionWidth, actionHeight), !statusMessage.isEmpty());
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int panelWidth = 240;
		int panelHeight = 155;
		int left = (width - panelWidth) / 2;
		int top = (height - panelHeight) / 2;

		if (isInside(mouseX, mouseY, left + 56, top + 82, 118, 20)) {
			blurPassword = !blurPassword;
			return true;
		}

		if (isInside(mouseX, mouseY, left + 56, top + 112, 118, 20)) {
			submit();
			return true;
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	protected void submit() {
		String password = getPassword();
		if (password.isBlank()) {
			setStatus(Text.translatable("screen.ward_watch.invalid_code").getString());
			return;
		}

		ClientPlayNetworking.send(new SubmitSetupPayload(pos, password));
	}
}
