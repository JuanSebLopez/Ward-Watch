package com.wardwatch.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;

public class ProtectedDoorBlock extends DoorBlock {
	public static final int OPEN_TICKS = 60;

	public ProtectedDoorBlock(BlockSetType blockSetType, Settings settings) {
		super(blockSetType, settings);
	}

	public void openTemporarily(ServerWorld world, BlockState state, BlockPos pos, PlayerEntity player) {
		BlockPos lowerPos = state.get(HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
		BlockState lowerState = world.getBlockState(lowerPos);
		if (!lowerState.isOf(this)) {
			return;
		}

		if (!lowerState.get(OPEN)) {
			this.setOpen(player, world, lowerState, lowerPos, true);
			world.playSound(null, lowerPos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}

		world.scheduleBlockTick(lowerPos, this, OPEN_TICKS);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
		return ActionResult.SUCCESS;
	}

	@Override
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify) {
		// Ignore redstone updates so the door only opens through the password flow.
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		BlockPos lowerPos = state.get(HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
		BlockState lowerState = world.getBlockState(lowerPos);
		if (!lowerState.isOf(this) || !lowerState.get(OPEN)) {
			return;
		}

		this.setOpen(null, world, lowerState, lowerPos, false);
		world.playSound(null, lowerPos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, 1.0F);
	}
}
