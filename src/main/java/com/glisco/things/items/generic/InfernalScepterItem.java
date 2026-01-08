package com.glisco.things.items.generic;

import com.glisco.things.Things;
import com.glisco.things.items.ItemWithExtendableTooltip;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import java.util.Collections;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class InfernalScepterItem extends ItemWithExtendableTooltip {

    public InfernalScepterItem() {
        super(((OwoItemSettingsExtension) new Item.Properties().stacksTo(1).fireResistant()).group(() -> Things.THINGS_GROUP).durability(Things.CONFIG.infernalScepterDurability()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (!user.getInventory().hasAnyOf(Collections.singleton(Items.FIRE_CHARGE)))
            return InteractionResultHolder.fail(user.getItemInHand(hand));
        user.startUsingItem(hand);
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) return;
        if (72000 - remainingUseTicks < 20) return;

        final var inventory = player.getInventory();
        if (!inventory.hasAnyOf(Collections.singleton(Items.FIRE_CHARGE))) return;

        if (!world.isClientSide) {
            Vec3 vec3d = player.getViewVector(0.0F);
            double vX = (player.getX() + vec3d.x * 4.0D) - player.getX();
            double vY = (player.getY() + vec3d.y * 4.0D) - player.getY();
            double vZ = (player.getZ() + vec3d.z * 4.0D) - player.getZ();

            LargeFireball fireball = new LargeFireball(world, user, new Vec3(vX, vY, vZ), 3);
            fireball.absMoveTo(player.getX() + vec3d.x * 2.0D, player.getEyeY() - 1, player.getZ() + vec3d.z * 2.0D);
            world.addFreshEntity(fireball);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1, 1);

            stack.hurtAndBreak(1, user, LivingEntity.getSlotForHand(user.getUsedItemHand()));
        }

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            final var ammoStack = inventory.getItem(slot);

            if (ammoStack.is(Items.FIRE_CHARGE)) {
                ammoStack.shrink(1);
                break;
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }
}
