package com.glisco.things.mixin;

import com.glisco.things.items.ThingsItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    @Inject(method = "getEmptiedStack", at = @At("HEAD"), cancellable = true)
    private static void preserverBaterWucket(ItemStack stack, Player player, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.is(ThingsItems.BATER_WUCKET)) cir.setReturnValue(stack);
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemUsage;exchangeStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack preserveBemptyUcket(ItemStack inputStack, Player player, ItemStack outputStack, Operation<ItemStack> original) {
        return original.call(inputStack, player, inputStack.is(ThingsItems.BEMPTY_UCKET) ? inputStack.copy() : outputStack);
    }
}
