package com.wardwatch.block;

import com.wardwatch.WardWatchMod;
import com.wardwatch.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.TallBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class ModBlocks {
	private static final Identifier PROTECTED_DOOR_ID = Identifier.of(WardWatchMod.MOD_ID, "protected_door");
	private static final RegistryKey<Block> PROTECTED_DOOR_KEY = RegistryKey.of(RegistryKeys.BLOCK, PROTECTED_DOOR_ID);
	private static final RegistryKey<Item> PROTECTED_DOOR_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, PROTECTED_DOOR_ID);

	public static final ProtectedDoorBlock PROTECTED_DOOR = Registry.register(
		Registries.BLOCK,
		PROTECTED_DOOR_ID,
		new ProtectedDoorBlock(
			BlockSetType.IRON,
			AbstractBlock.Settings.copy(Blocks.IRON_DOOR)
				.registryKey(PROTECTED_DOOR_KEY)
				.sounds(BlockSoundGroup.IRON)
		)
	);

	private ModBlocks() {
	}

	public static void register() {
		Registry.register(
			Registries.ITEM,
			PROTECTED_DOOR_ID,
			new TallBlockItem(
				PROTECTED_DOOR,
				new Item.Settings().useBlockPrefixedTranslationKey().registryKey(PROTECTED_DOOR_ITEM_KEY)
			)
		);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.addAfter(ModItems.PASSWORD_PROTECTOR, PROTECTED_DOOR.asItem()));
	}
}
