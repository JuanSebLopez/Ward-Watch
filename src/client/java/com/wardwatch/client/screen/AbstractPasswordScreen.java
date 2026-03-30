package com.wardwatch.client.screen;

import com.wardwatch.protection.ProtectionManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractPasswordScreen extends Screen {
	protected final BlockPos pos;
	protected final String blockTranslationKey;
	protected String password = "";
	protected String statusMessage = "";

	protected AbstractPasswordScreen(Text title, BlockPos pos, String blockTranslationKey) {
		super(title);
		this.pos = pos;
		this.blockTranslationKey = blockTranslationKey;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		String digit = digitFromKey(input.key());
		if (digit != null) {
			appendDigit(digit);
			return true;
		}

		if (input.key() == GLFW.GLFW_KEY_BACKSPACE) {
			backspace();
			return true;
		}

		if (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_KP_ENTER) {
			submit();
			return true;
		}

		return super.keyPressed(input);
	}

	protected void appendDigit(String digit) {
		if (password.length() >= ProtectionManager.MAX_PASSWORD_LENGTH) {
			return;
		}

		password += digit;
		onPasswordChanged();
	}

	protected void backspace() {
		if (password.isEmpty()) {
			return;
		}

		password = password.substring(0, password.length() - 1);
		onPasswordChanged();
	}

	protected void clearPassword() {
		password = "";
		onPasswordChanged();
	}

	protected String getPassword() {
		return password;
	}

	protected String getMaskedPassword() {
		return "*".repeat(password.length());
	}

	protected String withCursor(String text) {
		return text + (password.length() < ProtectionManager.MAX_PASSWORD_LENGTH ? "_" : "");
	}

	protected void setStatus(String message) {
		statusMessage = message == null ? "" : message;
	}

	public void handleServerResponse(boolean success, String message) {
		if (!success) {
			setStatus(message);
		}
	}

	protected void drawPanel(DrawContext context, int x, int y, int width, int height) {
		context.fill(0, 0, this.width, this.height, 0x88000000);
		context.fill(x, y, x + width, y + height, 0xFFD2D2D2);
		drawBorder(context, x, y, width, height, 0xFF444444, 0xFFFFFFFF);
	}

	protected void drawBorder(DrawContext context, int x, int y, int width, int height, int dark, int light) {
		context.fill(x, y, x + width, y + 1, light);
		context.fill(x, y, x + 1, y + height, light);
		context.fill(x, y + height - 1, x + width, y + height, dark);
		context.fill(x + width - 1, y, x + width, y + height, dark);
	}


	protected int drawWrappedCenteredTitle(DrawContext context, int left, int top, int panelWidth, int maxWidth, int color) {
		var lines = textRenderer.wrapLines(title, maxWidth);
		int y = top + 10;
		for (var line : lines) {
			int x = left + (panelWidth - textRenderer.getWidth(line)) / 2;
			context.drawText(textRenderer, line, x, y, color, false);
			y += textRenderer.fontHeight + 2;
		}
		return y;
	}

	protected void drawField(DrawContext context, int x, int y, int width, int height, String displayText) {
		context.fill(x, y, x + width, y + height, 0xFF050505);
		drawBorder(context, x, y, width, height, 0xFF9A9A9A, 0xFFEAEAEA);
		context.drawText(textRenderer, Text.literal(displayText), x + 6, y + 6, 0xFFFFFFFF, false);
	}

	protected void drawActionButton(DrawContext context, int x, int y, int width, int height, Text label, boolean hovered, boolean warning) {
		int base = warning ? 0xFF7C7C7C : 0xFF8D8D8D;
		int face = hovered ? 0xFFA1A1A1 : base;
		int light = 0xFFDADADA;
		int dark = 0xFF343434;
		context.fill(x, y, x + width, y + height, face);
		context.fill(x, y, x + width, y + 2, light);
		context.fill(x, y, x + 2, y + height, light);
		context.fill(x, y + height - 2, x + width, y + height, dark);
		context.fill(x + width - 2, y, x + width, y + height, dark);
		context.drawCenteredTextWithShadow(textRenderer, label, x + width / 2, y + 6, 0xFFF2F2F2);
	}

	protected boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private void onPasswordChanged() {
		statusMessage = "";
	}

	private String digitFromKey(int key) {
		return switch (key) {
			case GLFW.GLFW_KEY_0, GLFW.GLFW_KEY_KP_0 -> "0";
			case GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_KP_1 -> "1";
			case GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_KP_2 -> "2";
			case GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_KP_3 -> "3";
			case GLFW.GLFW_KEY_4, GLFW.GLFW_KEY_KP_4 -> "4";
			case GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_KP_5 -> "5";
			case GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_KP_6 -> "6";
			case GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_KP_7 -> "7";
			case GLFW.GLFW_KEY_8, GLFW.GLFW_KEY_KP_8 -> "8";
			case GLFW.GLFW_KEY_9, GLFW.GLFW_KEY_KP_9 -> "9";
			default -> null;
		};
	}

	protected abstract void submit();
}
