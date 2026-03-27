package com.wardwatch.item;

import com.wardwatch.WardWatchMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModItems {
	private static final Identifier PASSWORD_PROTECTOR_ID = Identifier.of(WardWatchMod.MOD_ID, "password_protector");

	public static final Item PASSWORD_PROTECTOR = Registry.register(
		Registries.ITEM,
		PASSWORD_PROTECTOR_ID,
		new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, PASSWORD_PROTECTOR_ID)))
	);

	private ModItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(PASSWORD_PROTECTOR));
	}
}
