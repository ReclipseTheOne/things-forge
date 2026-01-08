package com.glisco.things.items.generic;

import com.glisco.things.Things;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;

public class RecallPotionItem extends Item {

    public RecallPotionItem() {
        super(((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(16));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 15;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, user, hand);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        var player = user instanceof Player ? (Player) user : null;
        if (player == null) return stack;

        if (player instanceof ServerPlayer serverPlayer) {

            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);

            if (serverPlayer.getRespawnPosition() != null) {
                var respawnPos = ((ServerPlayer) player).findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
                player.changeDimension(respawnPos);

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        stack = new ItemStack(Items.GLASS_BOTTLE);
                    } else {
                        player.getInventory().placeItemBackInInventory(new ItemStack(Items.GLASS_BOTTLE));
                    }
                }
            } else {
                serverPlayer.displayClientMessage(Component.literal("No respawn point"), true);
            }
        }

        return stack;
    }

}
