package com.wardwatch;

import com.wardwatch.block.ModBlocks;
import com.wardwatch.config.WardWatchConfig;
import com.wardwatch.item.ModItemGroups;
import com.wardwatch.item.ModItems;
import com.wardwatch.network.ModPayloads;
import com.wardwatch.protection.ProtectionManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WardWatchMod implements ModInitializer {
	public static final String MOD_ID = "ward_watch";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WardWatchConfig.load();
		ModBlocks.register();
		ModItems.register();
		ModItemGroups.register();
		ModPayloads.register();
		ProtectionManager.register();
		LOGGER.info("Ward & Watch listo. Sistema de contrasenas inicializado.");
	}
}
