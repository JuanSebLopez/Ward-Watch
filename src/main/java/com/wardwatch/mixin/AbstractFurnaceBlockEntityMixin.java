package com.wardwatch.mixin;

import com.wardwatch.protection.PasswordProtected;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements PasswordProtected {
	@Unique
	private static final String WARD_WATCH_PASSWORD_KEY = "ward_watch_password";
	@Unique
	private static final String WARD_WATCH_OWNER_UUID_KEY = "ward_watch_owner_uuid";
	@Unique
	private static final String WARD_WATCH_OWNER_NAME_KEY = "ward_watch_owner_name";

	@Unique
	private String wardWatch$password = "";
	@Unique
	private UUID wardWatch$ownerUuid;
	@Unique
	private String wardWatch$ownerName = "";

	@Inject(method = "writeData", at = @At("TAIL"))
	private void wardWatch$writeData(WriteView view, CallbackInfo ci) {
		if (!wardWatch$password.isEmpty()) {
			view.putString(WARD_WATCH_PASSWORD_KEY, wardWatch$password);
		}
		if (wardWatch$ownerUuid != null) {
			view.putString(WARD_WATCH_OWNER_UUID_KEY, wardWatch$ownerUuid.toString());
		}
		if (!wardWatch$ownerName.isEmpty()) {
			view.putString(WARD_WATCH_OWNER_NAME_KEY, wardWatch$ownerName);
		}
	}

	@Inject(method = "readData", at = @At("TAIL"))
	private void wardWatch$readData(ReadView view, CallbackInfo ci) {
		wardWatch$password = view.getString(WARD_WATCH_PASSWORD_KEY, "");
		String ownerUuid = view.getString(WARD_WATCH_OWNER_UUID_KEY, "");
		wardWatch$ownerUuid = ownerUuid.isEmpty() ? null : UUID.fromString(ownerUuid);
		wardWatch$ownerName = view.getString(WARD_WATCH_OWNER_NAME_KEY, "");
	}

	@Override
	public boolean wardWatch$isProtected() {
		return !wardWatch$password.isEmpty();
	}

	@Override
	public String wardWatch$getPassword() {
		return wardWatch$password;
	}

	@Override
	public void wardWatch$setPassword(String password) {
		wardWatch$password = password == null ? "" : password;
	}

	@Override
	public void wardWatch$clearPassword() {
		wardWatch$password = "";
		wardWatch$ownerUuid = null;
		wardWatch$ownerName = "";
	}

	@Override
	public UUID wardWatch$getOwnerUuid() {
		return wardWatch$ownerUuid;
	}

	@Override
	public String wardWatch$getOwnerName() {
		return wardWatch$ownerName;
	}

	@Override
	public void wardWatch$setOwner(UUID ownerUuid, String ownerName) {
		wardWatch$ownerUuid = ownerUuid;
		wardWatch$ownerName = ownerName == null ? "" : ownerName;
	}
}
