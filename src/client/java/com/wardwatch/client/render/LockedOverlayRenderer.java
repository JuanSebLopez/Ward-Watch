package com.wardwatch.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public final class LockedOverlayRenderer {
	private static final Identifier OVERLAY_TEXTURE_ID = Identifier.of("ward_watch", "entity/chest/protected_overlay");
	private static final SpriteIdentifier OVERLAY_SPRITE = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, OVERLAY_TEXTURE_ID);
	private static final RenderLayer OVERLAY_LAYER = TexturedRenderLayers.getChest();

	private LockedOverlayRenderer() {
	}

	public static void submitFrontOverlay(BlockState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, float centerY, float width, float height) {
		Direction facing = state.contains(Properties.HORIZONTAL_FACING) ? state.get(Properties.HORIZONTAL_FACING) : Direction.NORTH;
		Sprite sprite = MinecraftClient.getInstance().getAtlasManager().getSprite(OVERLAY_SPRITE);

		matrices.push();
		matrices.translate(0.5F, 0.5F, 0.5F);
		switch (facing) {
			case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
			case WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
			case EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
			default -> {
			}
		}

		float left = -width / 2.0F;
		float right = width / 2.0F;
		float top = centerY + height / 2.0F;
		float bottom = centerY - height / 2.0F;
		float z = -0.501F;
		float minU = sprite.getMinU();
		float maxU = sprite.getMaxU();
		float minV = sprite.getMinV();
		float maxV = sprite.getMaxV();

		queue.submitCustom(matrices, OVERLAY_LAYER, (entry, vertexConsumer) -> {
			vertexConsumer.vertex(entry, left, bottom, z).color(255, 255, 255, 255).texture(minU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
			vertexConsumer.vertex(entry, right, bottom, z).color(255, 255, 255, 255).texture(maxU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
			vertexConsumer.vertex(entry, right, top, z).color(255, 255, 255, 255).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
			vertexConsumer.vertex(entry, left, top, z).color(255, 255, 255, 255).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
		});
		matrices.pop();
	}
}
