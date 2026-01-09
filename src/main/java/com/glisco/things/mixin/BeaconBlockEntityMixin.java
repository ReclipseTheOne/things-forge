package com.glisco.things.mixin;

import com.glisco.things.Things;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {

    @Inject(method = "applyEffects", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void nerfHaste(Level world, BlockPos pos, int beaconLevel, @Nullable Holder<MobEffect> primaryEffect, @Nullable Holder<MobEffect> secondaryEffect, CallbackInfo ci, double d, int i, int j, AABB box, List<Player> list) {
        if (!Things.CONFIG.nerfBeaconsWithMomentum() || secondaryEffect != MobEffects.DIG_SPEED) return;
        list.removeIf(playerEntity -> playerEntity.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Things.MOMENTUM.get())));
    }

}
