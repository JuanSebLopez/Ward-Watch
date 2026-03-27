package com.wardwatch.client.render;

import com.wardwatch.protection.PasswordProtected;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ProtectedFurnaceBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, ProtectedBlockEntityRenderState> {
	public ProtectedFurnaceBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public ProtectedBlockEntityRenderState createRenderState() {
		return new ProtectedBlockEntityRenderState();
	}

	@Override
	public void updateRenderState(T blockEntity, ProtectedBlockEntityRenderState renderState, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
		BlockEntityRenderer.super.updateRenderState(blockEntity, renderState, tickProgress, cameraPos, crumblingOverlay);
		renderState.locked = blockEntity instanceof PasswordProtected protection && protection.wardWatch$isProtected();
	}

	@Override
	public void render(ProtectedBlockEntityRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
		if (renderState.locked) {
			LockedOverlayRenderer.submitFrontOverlay(renderState.blockState, matrices, queue, renderState.lightmapCoordinates, 0.0F, 0.34F, 0.34F);
		}
	}
}
