package com.wardwatch.client.render;

import com.wardwatch.protection.PasswordProtected;
import com.wardwatch.protection.ProtectionManager;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.render.block.entity.model.ChestBlockModel;
import net.minecraft.client.render.block.entity.state.ChestBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ProtectedChestBlockEntityRenderer implements BlockEntityRenderer<ChestBlockEntity, ProtectedChestRenderState> {
	private static final SpriteIdentifier PROTECTED_SINGLE = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, Identifier.of("ward_watch", "entity/chest/protected"));
	private static final SpriteIdentifier PROTECTED_LEFT = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, Identifier.of("ward_watch", "entity/chest/protected_left"));
	private static final SpriteIdentifier PROTECTED_RIGHT = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, Identifier.of("ward_watch", "entity/chest/protected_right"));

	private final SpriteHolder spriteHolder;
	private final ChestBlockModel singleChest;
	private final ChestBlockModel doubleChestLeft;
	private final ChestBlockModel doubleChestRight;
	private final boolean christmas;

	public ProtectedChestBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		this.spriteHolder = context.spriteHolder();
		this.christmas = ChestBlockEntityRenderer.isAroundChristmas();
		this.singleChest = new ChestBlockModel(context.getLayerModelPart(EntityModelLayers.CHEST));
		this.doubleChestLeft = new ChestBlockModel(context.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_LEFT));
		this.doubleChestRight = new ChestBlockModel(context.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_RIGHT));
	}

	@Override
	public ProtectedChestRenderState createRenderState() {
		return new ProtectedChestRenderState();
	}

	@Override
	public void updateRenderState(ChestBlockEntity blockEntity, ProtectedChestRenderState renderState, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
		BlockEntityRenderer.super.updateRenderState(blockEntity, renderState, tickProgress, cameraPos, crumblingOverlay);
		World world = blockEntity.getWorld();
		BlockState state = world != null
			? blockEntity.getCachedState()
			: Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);

		renderState.chestType = state.contains(ChestBlock.CHEST_TYPE) ? state.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
		renderState.yaw = state.get(ChestBlock.FACING).getPositiveHorizontalDegrees();
		renderState.variant = getVariant(blockEntity, this.christmas);
		renderState.locked = world != null && ProtectionManager.isProtected(world, blockEntity.getPos());

		if (world != null && state.getBlock() instanceof ChestBlock chestBlock) {
			DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> source = chestBlock.getBlockEntitySource(state, world, blockEntity.getPos(), true);
			Float2FloatFunction animationProgress = source.apply(ChestBlock.getAnimationProgressRetriever((LidOpenable) blockEntity));
			renderState.lidAnimationProgress = animationProgress.get(tickProgress);
			if (renderState.chestType != ChestType.SINGLE) {
				Int2IntFunction lightmap = (Int2IntFunction) source.apply(new LightmapCoordinatesRetriever());
				renderState.lightmapCoordinates = lightmap.applyAsInt(renderState.lightmapCoordinates);
			}
		} else {
			renderState.lidAnimationProgress = 0.0F;
		}
	}

	@Override
	public void render(ProtectedChestRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
		matrices.push();
		matrices.translate(0.5F, 0.5F, 0.5F);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-renderState.yaw));
		matrices.translate(-0.5F, -0.5F, -0.5F);

		float animation = 1.0F - renderState.lidAnimationProgress;
		animation = 1.0F - animation * animation * animation;
		SpriteIdentifier spriteId = renderState.locked ? getProtectedTexture(renderState.chestType) : TexturedRenderLayers.getChestTextureId(renderState.variant, renderState.chestType);
		RenderLayer layer = spriteId.getRenderLayer(id -> TexturedRenderLayers.getChest());
		Sprite sprite = this.spriteHolder.getSprite(spriteId);

		if (renderState.chestType != ChestType.SINGLE) {
			if (renderState.chestType == ChestType.LEFT) {
				queue.submitModel(this.doubleChestLeft, animation, matrices, layer, renderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, sprite, 0, renderState.crumblingOverlay);
			} else {
				queue.submitModel(this.doubleChestRight, animation, matrices, layer, renderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, sprite, 0, renderState.crumblingOverlay);
			}
		} else {
			queue.submitModel(this.singleChest, animation, matrices, layer, renderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, -1, sprite, 0, renderState.crumblingOverlay);
		}

		matrices.pop();
	}

	private static SpriteIdentifier getProtectedTexture(ChestType chestType) {
		return switch (chestType) {
			case LEFT -> PROTECTED_LEFT;
			case RIGHT -> PROTECTED_RIGHT;
			default -> PROTECTED_SINGLE;
		};
	}

	private static ChestBlockEntityRenderState.Variant getVariant(BlockEntity blockEntity, boolean christmas) {
		if (blockEntity instanceof EnderChestBlockEntity) {
			return ChestBlockEntityRenderState.Variant.ENDER_CHEST;
		}
		if (christmas) {
			return ChestBlockEntityRenderState.Variant.CHRISTMAS;
		}
		if (blockEntity instanceof TrappedChestBlockEntity) {
			return ChestBlockEntityRenderState.Variant.TRAPPED;
		}
		return ChestBlockEntityRenderState.Variant.REGULAR;
	}
}

