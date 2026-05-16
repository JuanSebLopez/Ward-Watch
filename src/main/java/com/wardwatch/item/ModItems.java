package com.wardwatch.item;

import com.wardwatch.WardWatchMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModItems {
	private static final Identifier PASSWORD_PROTECTOR_ID = Identifier.of(WardWatchMod.MOD_ID, "password_protector");
	private static final Identifier MASTER_OVERRIDE_ID = Identifier.of(WardWatchMod.MOD_ID, "master_override");

	public static final Item PASSWORD_PROTECTOR = Registry.register(
		Registries.ITEM,
		PASSWORD_PROTECTOR_ID,
		new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, PASSWORD_PROTECTOR_ID)))
	);
	public static final Item MASTER_OVERRIDE = Registry.register(
		Registries.ITEM,
		MASTER_OVERRIDE_ID,
		new Item(
			new Item.Settings()
				.registryKey(RegistryKey.of(RegistryKeys.ITEM, MASTER_OVERRIDE_ID))
				.maxCount(1)
				.maxDamage(3)
		)
	);

	private ModItems() {
	}

	public static void register() {
	}
}
