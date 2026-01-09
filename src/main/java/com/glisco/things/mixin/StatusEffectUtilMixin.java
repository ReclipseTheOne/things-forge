package com.glisco.things.mixin;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.misc.ExtendedStatusEffectInstance;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectUtil.class)
public abstract class StatusEffectUtilMixin {

    @Inject(method = "hasDigSpeed", at = @At("HEAD"), cancellable = true)
    private static void hasMomentum(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Things.MOMENTUM.get()))) cir.setReturnValue(true);
    }

    @ModifyVariable(method = "getDigSpeedAmplification", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getAmplifier()I", ordinal = 1), ordinal = 0)
    private static int getMomentumAmplifier(LivingEntity entity, @Local int i) {
        if (entity.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Things.MOMENTUM.get()))) {
            i += entity.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Things.MOMENTUM.get())).getAmplifier();
            if (entity.hasEffect(MobEffects.DIG_SPEED) && i == 0) i++;
        }

        return i;
    }

    @ModifyVariable(method = "formatDuration", at = @At(value = "HEAD"), argsOnly = true, ordinal = 0)
    private static float extendTime(float multiplier, MobEffectInstance instance, float unused) {
        var entity = ((ExtendedStatusEffectInstance) instance).things$getAttachedEntity();

        if (entity != null) {
            var capability = AccessoriesCapability.get(entity);

            if(capability != null && capability.isEquipped(ThingsItems.BROKEN_WATCH)) return multiplier * 1.5F;
        }

        return multiplier;
    }
}
