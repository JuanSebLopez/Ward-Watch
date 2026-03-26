package com.wardwatch.item;

import com.wardwatch.WardWatchMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
	public static final Item PASSWORD_PROTECTOR = Registry.register(
		Registries.ITEM,
		Identifier.of(WardWatchMod.MOD_ID, "password_protector"),
		new Item(new Item.Settings())
	);

	private ModItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(PASSWORD_PROTECTOR));
	}
}
