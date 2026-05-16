package com.wardwatch.protection;

import com.wardwatch.block.ModBlocks;
import com.wardwatch.block.ProtectedDoorBlock;
import com.wardwatch.item.ModItems;
import com.wardwatch.network.ModPayloads;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.DoubleBlockHalf;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProtectionManager {
	public static final int MAX_PASSWORD_LENGTH = 6;
	private static final Set<String> PENDING_PROTECTOR_DROPS = ConcurrentHashMap.newKeySet();
	private static final Set<String> PENDING_PROTECTED_DOOR_DROPS = ConcurrentHashMap.newKeySet();

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
				if (state.isOf(ModBlocks.PROTECTED_DOOR) || heldStack.isOf(ModItems.PASSWORD_PROTECTOR) || heldStack.isOf(ModItems.MASTER_OVERRIDE) || isProtected(world, pos)) {
					return ActionResult.SUCCESS;
				}
				return ActionResult.PASS;
			}

			ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
			PasswordProtected protection = getProtectionData(world, pos);
			if (protection != null && protection.wardWatch$isProtected()) {
				if (tryBypassWithOverride(serverPlayer, pos)) {
					return ActionResult.SUCCESS_SERVER;
				}
				ModPayloads.openUnlockScreen(serverPlayer, pos, state.getBlock().getTranslationKey());
				return ActionResult.SUCCESS_SERVER;
			}

			if (state.isOf(ModBlocks.PROTECTED_DOOR) || player.getMainHandStack().isOf(ModItems.PASSWORD_PROTECTOR)) {
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

			clearProtection(world, pos);

			for (BlockPos linkedPos : getLinkedPositions(world, pos)) {
				if (!linkedPos.equals(pos)) {
					clearProtection(world, linkedPos);
				}
			}

			if (state.isOf(ModBlocks.PROTECTED_DOOR)) {
				if (state.contains(ProtectedDoorBlock.HALF) && state.get(ProtectedDoorBlock.HALF) == DoubleBlockHalf.UPPER) {
					PENDING_PROTECTED_DOOR_DROPS.add(makeDropKey(world, getCanonicalPos(world, pos)));
				}
				return true;
			}

			PENDING_PROTECTOR_DROPS.add(makeDropKey(world, getCanonicalPos(world, pos)));
			return true;
		});

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			BlockPos canonicalPos = getCanonicalPos(world, pos);
			String dropKey = makeDropKey(world, canonicalPos);

			if (PENDING_PROTECTED_DOOR_DROPS.remove(dropKey)) {
				ItemEntity doorDrop = new ItemEntity(world, canonicalPos.getX() + 0.5D, canonicalPos.getY() + 0.35D, canonicalPos.getZ() + 0.5D, new ItemStack(ModBlocks.PROTECTED_DOOR.asItem()));
				world.spawnEntity(doorDrop);
			}

			if (!PENDING_PROTECTOR_DROPS.remove(dropKey)) {
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
		if (state.isOf(ModBlocks.PROTECTED_DOOR)) {
			return true;
		}

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
		BlockPos canonicalPos = getCanonicalPos(world, pos);
		BlockState state = world.getBlockState(canonicalPos);
		if (state.isOf(ModBlocks.PROTECTED_DOOR) && world instanceof ServerWorld serverWorld) {
			DoorProtectionStorage.get(serverWorld).set(canonicalPos, password, player.getUuid(), player.getName().getString());
			sync(world, canonicalPos);
			sync(world, canonicalPos.up());
			return true;
		}

		boolean applied = false;
		for (BlockPos currentPos : getLinkedPositions(world, canonicalPos)) {
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
		BlockPos canonicalPos = getCanonicalPos(world, pos);
		BlockState state = world.getBlockState(canonicalPos);
		if (state.getBlock() instanceof ProtectedDoorBlock protectedDoorBlock && world instanceof ServerWorld serverWorld) {
			protectedDoorBlock.openTemporarily(serverWorld, state, canonicalPos, player);
			return true;
		}

		NamedScreenHandlerFactory factory = world.getBlockState(canonicalPos).createScreenHandlerFactory(world, canonicalPos);
		if (factory == null) {
			return false;
		}

		player.openHandledScreen(factory);
		return true;
	}

	private static boolean tryBypassWithOverride(ServerPlayerEntity player, BlockPos pos) {
		ItemStack stack = player.getMainHandStack();
		if (!stack.isOf(ModItems.MASTER_OVERRIDE)) {
			return false;
		}

		if (!openProtectedBlock(player, pos)) {
			player.sendMessage(Text.literal("No se pudo forzar el acceso."), true);
			return true;
		}

		stack.damage(1, player, Hand.MAIN_HAND);
		if (stack.isEmpty()) {
			player.sendMessage(Text.literal("El Master Override se agoto."), true);
		}
		return true;
	}

	public static PasswordProtected getProtectionData(World world, BlockPos pos) {
		BlockPos canonicalPos = getCanonicalPos(world, pos);
		BlockState state = world.getBlockState(canonicalPos);
		if (state.isOf(ModBlocks.PROTECTED_DOOR) && world instanceof ServerWorld serverWorld) {
			return DoorProtectionStorage.get(serverWorld).get(canonicalPos)
				.map(DoorProtectionView::new)
				.orElse(null);
		}

		for (BlockPos currentPos : getLinkedPositions(world, canonicalPos)) {
			BlockEntity blockEntity = world.getBlockEntity(currentPos);
			if (blockEntity instanceof PasswordProtected protection && protection.wardWatch$isProtected()) {
				return protection;
			}
		}

		BlockEntity blockEntity = world.getBlockEntity(canonicalPos);
		return blockEntity instanceof PasswordProtected protection ? protection : null;
	}

	private static void clearProtection(World world, BlockPos pos) {
		BlockPos canonicalPos = getCanonicalPos(world, pos);
		BlockState state = world.getBlockState(canonicalPos);
		if (state.isOf(ModBlocks.PROTECTED_DOOR) && world instanceof ServerWorld serverWorld) {
			DoorProtectionStorage.get(serverWorld).remove(canonicalPos);
			sync(world, canonicalPos);
			sync(world, canonicalPos.up());
			return;
		}

		BlockEntity blockEntity = world.getBlockEntity(canonicalPos);
		if (blockEntity instanceof PasswordProtected protection) {
			protection.wardWatch$clearPassword();
			blockEntity.markDirty();
			sync(world, canonicalPos);
		}
	}

	private static Set<BlockPos> getLinkedPositions(World world, BlockPos pos) {
		Set<BlockPos> positions = new LinkedHashSet<>();
		BlockPos canonicalPos = getCanonicalPos(world, pos);
		positions.add(canonicalPos);

		BlockState state = world.getBlockState(canonicalPos);
		if (state.isOf(ModBlocks.PROTECTED_DOOR)) {
			positions.add(canonicalPos.up());
			return positions;
		}

		if (!(state.getBlock() instanceof ChestBlock) || !state.contains(ChestBlock.CHEST_TYPE) || !state.contains(ChestBlock.FACING)) {
			return positions;
		}

		BlockPos partnerPos = getDoubleChestPartnerPos(canonicalPos, state);
		if (partnerPos == null) {
			return positions;
		}

		BlockState partnerState = world.getBlockState(partnerPos);
		if (!(partnerState.getBlock() instanceof ChestBlock) || !partnerState.contains(ChestBlock.CHEST_TYPE) || !partnerState.contains(ChestBlock.FACING)) {
			return positions;
		}

		if (partnerState.get(ChestBlock.FACING) != state.get(ChestBlock.FACING)) {
			return positions;
		}

		if (partnerState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
			return positions;
		}

		positions.add(partnerPos);
		return positions;
	}

	private static BlockPos getDoubleChestPartnerPos(BlockPos pos, BlockState state) {
		if (!state.contains(ChestBlock.CHEST_TYPE) || !state.contains(ChestBlock.FACING)) {
			return null;
		}

		ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
		if (chestType == ChestType.SINGLE) {
			return null;
		}

		Direction facing = state.get(ChestBlock.FACING);
		Direction offset = chestType == ChestType.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();
		return pos.offset(offset);
	}

	private static BlockPos getCanonicalPos(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.isOf(ModBlocks.PROTECTED_DOOR) && state.contains(ProtectedDoorBlock.HALF) && state.get(ProtectedDoorBlock.HALF) == DoubleBlockHalf.UPPER) {
			return pos.down();
		}
		return pos;
	}

	private static String makeDropKey(World world, BlockPos pos) {
		return world.getRegistryKey().getValue() + ":" + pos.asLong();
	}

	private static void sync(World world, BlockPos pos) {
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.getChunkManager().markForUpdate(pos);
		}
	}

	private static final class DoorProtectionView implements PasswordProtected {
		private final String password;
		private final UUID ownerUuid;
		private final String ownerName;

		private DoorProtectionView(DoorProtectionStorage.Entry entry) {
			this.password = entry.password();
			this.ownerUuid = entry.ownerUuid();
			this.ownerName = entry.ownerName();
		}

		@Override
		public boolean wardWatch$isProtected() {
			return !this.password.isEmpty();
		}

		@Override
		public String wardWatch$getPassword() {
			return this.password;
		}

		@Override
		public void wardWatch$setPassword(String password) {
		}

		@Override
		public void wardWatch$clearPassword() {
		}

		@Override
		public UUID wardWatch$getOwnerUuid() {
			return this.ownerUuid;
		}

		@Override
		public String wardWatch$getOwnerName() {
			return this.ownerName;
		}

		@Override
		public void wardWatch$setOwner(UUID ownerUuid, String ownerName) {
		}
	}
}
