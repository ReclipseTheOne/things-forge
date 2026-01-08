package com.glisco.things.mixin;

import com.glisco.things.items.ThingsItems;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "eatFood", at = @At("TAIL"))
    public void onConsume(Level world, ItemStack stack, FoodProperties foodComponent, CallbackInfoReturnable<ItemStack> cir) {

        if (!stack.getItem().equals(Items.POISONOUS_POTATO)) return;

        var player = (Player) (Object) this;
        var capability = AccessoriesCapability.get(player);

        if (capability == null || !capability.isEquipped(ThingsItems.LUCK_OF_THE_IRISH)) return;

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 400, 0));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 2, 0));
        player.removeEffect(MobEffects.POISON);
    }
}
