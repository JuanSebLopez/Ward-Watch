package com.wardwatch.protection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DoorProtectionStorage extends PersistentState {
	private static final Codec<Map<String, Entry>> ENTRIES_CODEC = Codec.unboundedMap(Codec.STRING, Entry.CODEC);
	private static final Codec<DoorProtectionStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ENTRIES_CODEC.optionalFieldOf("entries", Map.of()).forGetter(storage -> storage.entries)
	).apply(instance, DoorProtectionStorage::new));
	private static final PersistentStateType<DoorProtectionStorage> TYPE = new PersistentStateType<>(
		"ward_watch_door_protection",
		DoorProtectionStorage::new,
		CODEC,
		DataFixTypes.SAVED_DATA_COMMAND_STORAGE
	);

	private final Map<String, Entry> entries;

	public DoorProtectionStorage() {
		this(new HashMap<>());
	}

	private DoorProtectionStorage(Map<String, Entry> entries) {
		this.entries = new HashMap<>(entries);
	}

	public static DoorProtectionStorage get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE);
	}

	public Optional<Entry> get(BlockPos pos) {
		return Optional.ofNullable(this.entries.get(key(pos)));
	}

	public boolean isProtected(BlockPos pos) {
		return this.entries.containsKey(key(pos));
	}

	public void set(BlockPos pos, String password, UUID ownerUuid, String ownerName) {
		this.entries.put(key(pos), new Entry(password, ownerUuid, ownerName == null ? "" : ownerName));
		this.markDirty();
	}

	public void remove(BlockPos pos) {
		if (this.entries.remove(key(pos)) != null) {
			this.markDirty();
		}
	}

	private static String key(BlockPos pos) {
		return Long.toString(pos.asLong());
	}

	public record Entry(String password, UUID ownerUuid, String ownerName) {
		private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("password").forGetter(Entry::password),
			Uuids.CODEC.fieldOf("owner_uuid").forGetter(Entry::ownerUuid),
			Codec.STRING.optionalFieldOf("owner_name", "").forGetter(Entry::ownerName)
		).apply(instance, Entry::new));
	}
}
