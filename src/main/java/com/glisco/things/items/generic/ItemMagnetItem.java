package com.glisco.things.items.generic;

import com.glisco.things.Things;
import com.mojang.serialization.Codec;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import io.wispforest.owo.particles.ClientParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ItemMagnetItem extends Item {

    private static final int USE_COST = 50;
    private static final int MAX_CHARGE = 200;

    public static final DataComponentType<Integer> CHARGE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Things.id("item_magnet_charge"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    public ItemMagnetItem() {
        super(((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(1).component(CHARGE, MAX_CHARGE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        var stack = user.getItemInHand(hand);
        if (stack.get(CHARGE) < USE_COST) return InteractionResultHolder.pass(stack);

        var teleportedItems = new HashSet<>();
        boolean blue = true;

        for (double i = 2; i < 10; i += 0.15) {
            var result = user.pick(i, 0, false);

            if (world.isClientSide) {
                blue = !blue;
                var particle = new DustParticleOptions(new Vector3f(blue ? 0.5f : 1, 0, blue ? 1 : 0.5f), 1);
                world.addParticle(particle, result.getLocation().x, result.getLocation().y, result.getLocation().z, 0, 0, 0);

                if (i > 9.5) {
                    displayTerminator(world, result.getLocation(), 0.65);
                }
            }

            if (!result.getType().equals(HitResult.Type.MISS)) {

                if (world.isClientSide) {
                    HitResult terminatorPosition = user.pick(i - 0.75, 0, false);
                    displayTerminator(world, terminatorPosition.getLocation(), 0.25);
                }

                break;
            }

            double radius = 1.25 + (stack.get(CHARGE) / (double) MAX_CHARGE) * 2;
            Vec3 box1 = result.getLocation().add(-radius, -radius, -radius);
            Vec3 box2 = result.getLocation().add(radius, radius, radius);

            for (var item : world.getEntitiesOfClass(ItemEntity.class, new AABB(box1, box2))) {
                if (!teleportedItems.add(item)) continue;

                if (world.isClientSide) {
                    ClientParticles.setParticleCount(2);
                    ClientParticles.spawn(ParticleTypes.POOF, world, item.position().add(0, .35, 0), .1f);
                } else {
                    item.absMoveTo(user.getX(), user.getY(), user.getZ());
                    item.setNoGravity(true);
                    item.setDeltaMovement(Vec3.ZERO);
                    item.setPickUpDelay(0);
                }
            }
        }

        user.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.125f, 2);
        stack.set(CHARGE, stack.get(CHARGE) - USE_COST);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable(this.getDescriptionId() + ".tooltip", stack.get(CHARGE)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (stack.get(CHARGE) >= MAX_CHARGE) return;
        stack.update(CHARGE, MAX_CHARGE, energy -> Math.min(energy + 1 + energy / 80, MAX_CHARGE));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.get(CHARGE) < MAX_CHARGE;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) (13 * (stack.get(CHARGE) / (float) MAX_CHARGE));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float energy = stack.get(CHARGE) / (float) MAX_CHARGE;

        int r = (int) (100 + 155 * (1 - energy));
        int b = (int) (127 + 128 * energy);

        return r << 16 | b;
    }

    @OnlyIn(Dist.CLIENT)
    private static void displayTerminator(Level world, Vec3 at, double spread) {
        ClientParticles.setParticleCount(5);
        ClientParticles.spawn(ParticleTypes.WITCH, world, at, spread);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }
}
