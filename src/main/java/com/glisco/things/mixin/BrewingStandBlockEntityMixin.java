package com.glisco.things.mixin;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true)
    private static void checkCraft(PotionBrewing brewingRecipeRegistry, NonNullList<ItemStack> slots, CallbackInfoReturnable<Boolean> cir) {
        if (Things.recallPotionIngredient() == null) return;
        if (!slots.get(3).is(Things.recallPotionIngredient())) return;

        for (int i = 0; i < 3; i++) {
            if (!(slots.get(i).getItem() instanceof PotionItem)) continue;
            if (!slots.get(i).getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.AWKWARD)) continue;

            cir.setReturnValue(true);
            return;
        }
    }

    @Inject(method = "doBrew", at = @At("HEAD"), cancellable = true)
    private static void doCraft(Level world, BlockPos pos, NonNullList<ItemStack> slots, CallbackInfo ci) {
        if (Things.recallPotionIngredient() == null) return;

        var addition = slots.get(3);
        if (!addition.is(Things.recallPotionIngredient())) return;

        addition.shrink(1);

        for (int i = 0; i < 3; i++) {
            if (!(slots.get(i).getItem() instanceof PotionItem)) continue;
            if (!slots.get(i).getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.AWKWARD)) continue;

            slots.set(i, new ItemStack(ThingsItems.RECALL_POTION));
        }

        world.levelEvent(LevelEvent.SOUND_BREWING_STAND_BREW, pos, 0);
        ci.cancel();
    }

}
