package com.wardwatch.protection;

import com.wardwatch.item.ModItems;
import com.wardwatch.network.ModPayloads;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ProtectionManager {
	public static final int MAX_PASSWORD_LENGTH = 6;
	private static final Set<String> PENDING_PROTECTOR_DROPS = ConcurrentHashMap.newKeySet();

	private ProtectionManager() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (hand != Hand.MAIN_HAND) {
				return ActionResult.PASS;
			}

			BlockPos pos = hitResult.getBlockPos();
			BlockState state = world.getBlockState(pos);
			if (!isSupported(state, world.getBlockEntity(pos))) {
				return ActionResult.PASS;
			}

			if (world.isClient()) {
				ItemStack heldStack = player.getMainHandStack();
				if (heldStack.isOf(ModItems.PASSWORD_PROTECTOR) || isProtected(world, pos)) {
					return ActionResult.SUCCESS;
				}
				return ActionResult.PASS;
			}

			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
			PasswordProtected protection = getProtectionData(world, pos);
			if (protection != null && protection.wardWatch$isProtected()) {
				ModPayloads.openUnlockScreen(serverPlayer, pos, state.getBlock().getTranslationKey());
				return ActionResult.SUCCESS_SERVER;
			}

			if (player.getMainHandStack().isOf(ModItems.PASSWORD_PROTECTOR)) {
				ModPayloads.openSetupScreen(serverPlayer, pos, state.getBlock().getTranslationKey());
				return ActionResult.SUCCESS_SERVER;
			}

			return ActionResult.PASS;
		});

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (!isSupported(state, blockEntity)) {
				return true;
			}

			PasswordProtected protection = getProtectionData(world, pos);
			if (protection == null || !protection.wardWatch$isProtected()) {
				return true;
			}

			if (!protection.wardWatch$isOwner(player)) {
				player.sendMessage(Text.literal("Solo el creador puede romper este bloque protegido."), true);
				return false;
			}

			for (BlockPos linkedPos : getLinkedPositions(world, pos)) {
				if (!linkedPos.equals(pos)) {
					clearProtection(world, linkedPos);
				}
			}

			PENDING_PROTECTOR_DROPS.add(makeDropKey(world, pos));
			return true;
		});

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (!PENDING_PROTECTOR_DROPS.remove(makeDropKey(world, pos))) {
				return;
			}

			ItemEntity drop = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.35D, pos.getZ() + 0.5D, new ItemStack(ModItems.PASSWORD_PROTECTOR));
			world.spawnEntity(drop);
		});
	}

	public static boolean isProtected(World world, BlockPos pos) {
		PasswordProtected protection = getProtectionData(world, pos);
		return protection != null && protection.wardWatch$isProtected();
	}

	public static boolean isSupported(BlockState state, BlockEntity blockEntity) {
		if (blockEntity == null) {
			return false;
		}

		return blockEntity instanceof ChestBlockEntity || blockEntity instanceof AbstractFurnaceBlockEntity || state.getBlock() instanceof AbstractFurnaceBlock;
	}

	public static boolean canAccess(PlayerEntity player, BlockPos pos) {
		return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
	}

	public static boolean isValidPassword(String password) {
		if (password == null || password.isBlank() || password.length() > MAX_PASSWORD_LENGTH) {
			return false;
		}

		for (int i = 0; i < password.length(); i++) {
			if (!Character.isDigit(password.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	public static boolean applyProtection(ServerPlayerEntity player, BlockPos pos, String password) {
		if (!isValidPassword(password) || !canAccess(player, pos)) {
			return false;
		}

		World world = player.getEntityWorld();
		boolean applied = false;
		for (BlockPos currentPos : getLinkedPositions(world, pos)) {
			BlockEntity blockEntity = world.getBlockEntity(currentPos);
			if (!(blockEntity instanceof PasswordProtected protection)) {
				continue;
			}

			protection.wardWatch$setProtection(password, player.getUuid(), player.getName().getString());
			blockEntity.markDirty();
			sync(world, currentPos);
			applied = true;
		}

		return applied;
	}

	public static boolean checkPassword(World world, BlockPos pos, String password) {
		PasswordProtected protection = getProtectionData(world, pos);
		return protection != null && protection.wardWatch$isProtected() && protection.wardWatch$getPassword().equals(password);
	}

	public static boolean openProtectedBlock(ServerPlayerEntity player, BlockPos pos) {
		if (!canAccess(player, pos)) {
			return false;
		}

		World world = player.getEntityWorld();
		NamedScreenHandlerFactory factory = world.getBlockState(pos).createScreenHandlerFactory(world, pos);
		if (factory == null) {
			return false;
		}

		player.openHandledScreen(factory);
		return true;
	}

	public static PasswordProtected getProtectionData(World world, BlockPos pos) {
		for (BlockPos currentPos : getLinkedPositions(world, pos)) {
			BlockEntity blockEntity = world.getBlockEntity(currentPos);
			if (blockEntity instanceof PasswordProtected protection && protection.wardWatch$isProtected()) {
				return protection;
			}
		}

		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity instanceof PasswordProtected protection ? protection : null;
	}

	private static void clearProtection(World world, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof PasswordProtected protection) {
			protection.wardWatch$clearPassword();
			blockEntity.markDirty();
			sync(world, pos);
		}
	}

	private static Set<BlockPos> getLinkedPositions(World world, BlockPos pos) {
		Set<BlockPos> positions = new LinkedHashSet<>();
		positions.add(pos);

		BlockState state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof ChestBlock) || !state.contains(ChestBlock.CHEST_TYPE) || !state.contains(ChestBlock.FACING)) {
			return positions;
		}

		if (state.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
			return positions;
		}

		for (Direction direction : Direction.Type.HORIZONTAL) {
			BlockPos neighborPos = pos.offset(direction);
			BlockState neighborState = world.getBlockState(neighborPos);
			if (!(neighborState.getBlock() instanceof ChestBlock) || !neighborState.contains(ChestBlock.CHEST_TYPE) || !neighborState.contains(ChestBlock.FACING)) {
				continue;
			}

			if (neighborState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
				continue;
			}

			if (neighborState.get(ChestBlock.FACING) == state.get(ChestBlock.FACING)) {
				positions.add(neighborPos);
			}
		}

		return positions;
	}

	private static String makeDropKey(World world, BlockPos pos) {
		return world.getRegistryKey().getValue() + ":" + pos.asLong();
	}

	private static void sync(World world, BlockPos pos) {
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.getChunkManager().markForUpdate(pos);
		}
	}
}
