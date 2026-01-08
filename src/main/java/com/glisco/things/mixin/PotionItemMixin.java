package com.glisco.things.mixin;

import com.glisco.things.items.ThingsItems;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {

    @Inject(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V"), cancellable = true)
    public void consume(ItemStack stack, Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        var player = (Player) user;
        var capability = AccessoriesCapability.get(player);

        if (world.random.nextDouble() > 0.75) return;
        if (capability == null || !capability.isEquipped(ThingsItems.PLACEBO)) return;

        player.awardStat(Stats.ITEM_USED.get((PotionItem) (Object) this));
        cir.setReturnValue(stack);
    }

}
