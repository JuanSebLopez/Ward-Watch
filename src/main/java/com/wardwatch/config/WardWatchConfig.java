package com.wardwatch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wardwatch.WardWatchMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WardWatchConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("ward-watch.json");

	private static Data data = new Data();

	private WardWatchConfig() {
	}

	public static void load() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			if (Files.notExists(CONFIG_PATH)) {
				save();
				return;
			}

			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				Data loaded = GSON.fromJson(reader, Data.class);
				if (loaded != null) {
					data = loaded;
				}
			}

			save();
		} catch (IOException exception) {
			WardWatchMod.LOGGER.error("No se pudo cargar el config de Ward Watch.", exception);
		}
	}

	public static void save() {
		try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
			GSON.toJson(data, writer);
		} catch (IOException exception) {
			WardWatchMod.LOGGER.error("No se pudo guardar el config de Ward Watch.", exception);
		}
	}

	public static boolean isMasterOverrideCraftable() {
		return data.masterOverrideCraftable;
	}

	private static final class Data {
		private boolean masterOverrideCraftable = true;
	}
}
