package com.wardwatch.client.screen;

import com.wardwatch.protection.ProtectionManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractPasswordScreen extends Screen {
	protected final BlockPos pos;
	protected final String blockTitle;
	protected TextFieldWidget passwordField;
	protected String statusMessage = "";

	protected AbstractPasswordScreen(Text title, BlockPos pos, String blockTitle) {
		super(title);
		this.pos = pos;
		this.blockTitle = blockTitle;
	}

	@Override
	protected void init() {
		super.init();
		int panelWidth = 176;
		int panelHeight = 190;
		int left = (width - panelWidth) / 2;
		int top = (height - panelHeight) / 2;

		passwordField = new TextFieldWidget(textRenderer, left + 28, top + 28, 120, 20, Text.literal("code"));
		passwordField.setMaxLength(ProtectionManager.MAX_PASSWORD_LENGTH);
		passwordField.setDrawsBackground(true);
		passwordField.setChangedListener(this::sanitizePassword);
		addDrawableChild(passwordField);
		setInitialFocus(passwordField);

		addNumericButtons(left + 28, top + 60);
		addActionButton(left, top, panelWidth, panelHeight);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		int panelWidth = 176;
		int panelHeight = 190;
		int left = (width - panelWidth) / 2;
		int top = (height - panelHeight) / 2;

		context.fill(left, top, left + panelWidth, top + panelHeight, 0xE0C6C6C6);
		drawSimpleBorder(context, left, top, panelWidth, panelHeight, 0xFF3E3E3E);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top + 10, 0x202020);
		context.drawText(textRenderer, Text.literal("CODE"), left + 28, top + 18, 0x202020, false);

		super.render(context, mouseX, mouseY, delta);

		if (!statusMessage.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer, Text.literal(statusMessage), width / 2, top + panelHeight - 16, 0xAA2020);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (passwordField != null && passwordField.keyPressed(input)) {
			return true;
		}

		if (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_KP_ENTER) {
			submit();
			return true;
		}

		return super.keyPressed(input);
	}

	protected abstract void submit();

	protected Text getPrimaryActionText() {
		return Text.literal("Save");
	}

	protected void setStatus(String message) {
		statusMessage = message;
	}

	protected String getPassword() {
		return passwordField == null ? "" : passwordField.getText();
	}

	private void addNumericButtons(int x, int y) {
		int index = 1;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				String digit = Integer.toString(index++);
				addDrawableChild(ButtonWidget.builder(Text.literal(digit), button -> appendDigit(digit))
					.dimensions(x + col * 36, y + row * 24, 28, 20)
					.build());
			}
		}

		addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> backspace())
			.dimensions(x, y + 72, 28, 20)
			.build());
		addDrawableChild(ButtonWidget.builder(Text.literal("0"), button -> appendDigit("0"))
			.dimensions(x + 36, y + 72, 28, 20)
			.build());
		addDrawableChild(ButtonWidget.builder(Text.literal("OK"), button -> submit())
			.dimensions(x + 72, y + 72, 40, 20)
			.build());
	}

	private void addActionButton(int left, int top, int panelWidth, int panelHeight) {
		addDrawableChild(ButtonWidget.builder(getPrimaryActionText(), button -> submit())
			.dimensions(left + 38, top + panelHeight - 42, 100, 20)
			.build());
	}

	private void appendDigit(String digit) {
		String current = getPassword();
		if (current.length() >= ProtectionManager.MAX_PASSWORD_LENGTH) {
			return;
		}
		passwordField.setText(current + digit);
	}

	private void backspace() {
		String current = getPassword();
		if (!current.isEmpty()) {
			passwordField.setText(current.substring(0, current.length() - 1));
		}
	}

	private void sanitizePassword(String value) {
		StringBuilder clean = new StringBuilder();
		for (int i = 0; i < value.length() && clean.length() < ProtectionManager.MAX_PASSWORD_LENGTH; i++) {
			char character = value.charAt(i);
			if (Character.isDigit(character)) {
				clean.append(character);
			}
		}

		String sanitized = clean.toString();
		if (!sanitized.equals(value)) {
			passwordField.setText(sanitized);
		}
	}

	private void drawSimpleBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}
}
