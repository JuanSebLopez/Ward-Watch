package com.wardwatch.protection;

import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public interface PasswordProtected {
	boolean wardWatch$isProtected();

	String wardWatch$getPassword();

	void wardWatch$setPassword(String password);

	void wardWatch$clearPassword();

	UUID wardWatch$getOwnerUuid();

	String wardWatch$getOwnerName();

	void wardWatch$setOwner(UUID ownerUuid, String ownerName);

	default void wardWatch$setProtection(String password, UUID ownerUuid, String ownerName) {
		wardWatch$setPassword(password);
		wardWatch$setOwner(ownerUuid, ownerName);
	}

	default boolean wardWatch$isOwner(PlayerEntity player) {
		UUID ownerUuid = wardWatch$getOwnerUuid();
		return ownerUuid == null || ownerUuid.equals(player.getUuid());
	}
}
