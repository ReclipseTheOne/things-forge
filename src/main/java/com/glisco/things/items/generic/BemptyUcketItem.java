package com.glisco.things.items.generic;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;

public class BemptyUcketItem extends BucketItem {

    public BemptyUcketItem() {
        super(Fluids.EMPTY, ((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public static void registerCauldronBehavior() {
        CauldronInteraction.WATER.map().put(ThingsItems.BEMPTY_UCKET, (state, world, pos, player, hand, stack) -> {
            return CauldronInteraction.fillBucket(state, world, pos, player, hand, stack, stack.copy(), blockState -> blockState.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL);
        });

        CauldronInteraction.LAVA.map().put(ThingsItems.BEMPTY_UCKET, (state, world, pos, player, hand, stack) -> {
            return CauldronInteraction.fillBucket(state, world, pos, player, hand, stack, stack.copy(), blockState -> true, SoundEvents.BUCKET_FILL_LAVA);
        });
    }
}
