package com.glisco.things.items.generic;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;

public class BaterWucketItem extends BucketItem {

    public BaterWucketItem() {
        super(Fluids.WATER, ((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public static void registerCauldronBehavior() {
        CauldronInteraction.EMPTY.map().put(ThingsItems.BATER_WUCKET, (state, world, pos, player, hand, stack) -> {
            if (world.isClientSide) return ItemInteractionResult.SUCCESS;

            world.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 1f);
            world.gameEvent(null, GameEvent.FLUID_PLACE, pos);

            return ItemInteractionResult.CONSUME;
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        var stack = user.getItemInHand(hand).copy();
        var result = super.use(world, user, hand);
        return new InteractionResultHolder<>(
                result.getResult(),
                stack
        );
    }

    
}
