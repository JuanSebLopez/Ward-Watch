package com.wardwatch.mixin;

import com.wardwatch.protection.ProtectionManager;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
	@Inject(method = "getInventoryAt", at = @At("HEAD"), cancellable = true)
	private static void wardWatch$blockHopperAccessToProtectedBlocks(World world, BlockPos pos, CallbackInfoReturnable<Inventory> cir) {
		if (ProtectionManager.isProtected(world, pos)) {
			cir.setReturnValue(null);
		}
	}
}
