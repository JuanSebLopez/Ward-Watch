package com.wardwatch.mixin;

import com.wardwatch.config.WardWatchConfig;
import com.wardwatch.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
	@Shadow
	@Final
	ItemStack result;

	@Inject(method = "matches", at = @At("RETURN"), cancellable = true)
	private void wardWatch$disableMasterOverrideRecipeWhenConfigured(CraftingRecipeInput input, World world, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			return;
		}

		if (this.result.isOf(ModItems.MASTER_OVERRIDE) && !WardWatchConfig.isMasterOverrideCraftable()) {
			cir.setReturnValue(false);
		}
	}
}
