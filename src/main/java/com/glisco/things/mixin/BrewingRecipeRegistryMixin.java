package com.glisco.things.mixin;

import com.glisco.things.Things;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class BrewingRecipeRegistryMixin {

    @Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
    private void allowEnderPearl(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (Things.recallPotionIngredient() == null) return;
        if (stack.is(Things.recallPotionIngredient())) cir.setReturnValue(true);
    }

}
