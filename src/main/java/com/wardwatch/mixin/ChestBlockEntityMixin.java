package com.wardwatch.mixin;

import com.wardwatch.protection.PasswordProtected;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin implements PasswordProtected {
	@Unique
	private static final String WARD_WATCH_PASSWORD_KEY = "ward_watch_password";

	@Unique
	private String wardWatch$password = "";

	@Inject(method = "writeData", at = @At("TAIL"))
	private void wardWatch$writeData(WriteView view, CallbackInfo ci) {
		if (!wardWatch$password.isEmpty()) {
			view.putString(WARD_WATCH_PASSWORD_KEY, wardWatch$password);
		}
	}

	@Inject(method = "readData", at = @At("TAIL"))
	private void wardWatch$readData(ReadView view, CallbackInfo ci) {
		wardWatch$password = view.getString(WARD_WATCH_PASSWORD_KEY, "");
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
	}
}
