package com.wardwatch.item;

import com.wardwatch.WardWatchMod;
import com.wardwatch.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
	private static final Identifier WARD_WATCH_GROUP_ID = Identifier.of(WardWatchMod.MOD_ID, "ward_watch");
	private static final RegistryKey<ItemGroup> WARD_WATCH_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, WARD_WATCH_GROUP_ID);

	public static final ItemGroup WARD_WATCH_GROUP = Registry.register(
		Registries.ITEM_GROUP,
		WARD_WATCH_GROUP_ID,
		FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModItems.MASTER_OVERRIDE))
			.displayName(Text.translatable("itemGroup.ward_watch.ward_watch"))
			.entries((context, entries) -> {
				entries.add(ModItems.PASSWORD_PROTECTOR);
				entries.add(ModItems.MASTER_OVERRIDE);
				entries.add(ModBlocks.PROTECTED_DOOR.asItem());
			})
			.build()
	);

	private ModItemGroups() {
	}

	public static void register() {
	}
}
