package com.wardwatch.mixin;

import com.wardwatch.protection.ProtectionManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.explosion.AdvancedExplosionBehavior.class)
public abstract class AdvancedExplosionBehaviorMixin {
	@Inject(method = "canDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void wardWatch$preventProtectedBlocksFromExploding(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power, CallbackInfoReturnable<Boolean> cir) {
		if (world instanceof World actualWorld && ProtectionManager.isProtected(actualWorld, pos)) {
			cir.setReturnValue(false);
		}
	}
}
