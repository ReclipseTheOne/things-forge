package com.glisco.things.mixin;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract Vec3 getPos();

    @Shadow
    public abstract boolean isRemoved();

    @Shadow
    public Level world;

    @Shadow
    public abstract BlockPos getBlockPos();

    @Shadow
    public abstract EntityType<?> getType();

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;lengthSquared()D", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void pistonCrushing(MoverType movementType, Vec3 movement, CallbackInfo ci, Vec3 vec3d) {
        if (!((Object) this instanceof ItemEntity itemEntity)) return;

        if (movementType != MoverType.PISTON) return;
        if (vec3d.lengthSqr() != 0) return;
        if (this.isRemoved()) return;

        final var thisItem = itemEntity.getItem().getItem();
        if (!Things.brokenWatchRecipe().contains(thisItem)) return;
        final var recipe = new ArrayList<>(Things.brokenWatchRecipe());

        recipe.remove(thisItem);
        int craftCount = itemEntity.getItem().getCount();

        final var items = this.world.getEntitiesOfClass(ItemEntity.class, new AABB(this.getBlockPos()), ItemEntity::isAlive);
        final var craftingParticipants = new ArrayList<>(Collections.singleton(itemEntity));

        for (var item : items) {
            final var scrutinee = item.getItem();
            if (recipe.contains(scrutinee.getItem())) {
                recipe.remove(scrutinee.getItem());

                craftCount = Math.min(scrutinee.getCount(), craftCount);
                craftingParticipants.add(item);
            }
        }

        if (recipe.isEmpty()) {
            for (var item : craftingParticipants) {
                final var stack = item.getItem();
                stack.shrink(craftCount);

                if (stack.isEmpty()) item.discard();
            }

            for (int i = 0; i < craftCount; i++) {
                this.world.addFreshEntity(new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), ThingsItems.BROKEN_WATCH.getDefaultInstance()));
            }
        }
    }

}
