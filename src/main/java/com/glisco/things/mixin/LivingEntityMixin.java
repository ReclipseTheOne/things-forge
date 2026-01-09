package com.glisco.things.mixin;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.misc.ExtendedStatusEffectInstance;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.pond.AccessoriesAPIAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements AccessoriesAPIAccess {

    @Shadow public abstract double getAttributeValue(Holder<Attribute> attribute);

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"))
    public void onShieldHit(float amount, CallbackInfo ci) {
        LivingEntity user = (LivingEntity) (Object) this;

        if (!user.getUseItem().is(Things.ENCHANTABLE_WITH_RETRIBUTION)) return;
        if (user.getUseItem().getEnchantments().getLevel(level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(Things.RETRIBUTION).get()) < 1) return;
        user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0));
    }

    @Inject(method = "isDamageSourceBlocked", at = @At("RETURN"))
    public void onShieldBlock(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;

        LivingEntity user = (LivingEntity) (Object) this;

        if (!user.getUseItem().is(Things.ENCHANTABLE_WITH_RETRIBUTION)) return;
        if (user.getUseItem().getEnchantments().getLevel(level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(Things.RETRIBUTION).get()) < 1) return;
        user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0));
    }

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 1), ordinal = 1)
    public float waxGlandWater(float j) {
        var capability = this.accessoriesCapability();

        if (capability == null || !capability.isEquipped(ThingsItems.ENCHANTED_WAX_GLAND)) return j;

        return j * Things.CONFIG.waxGlandMultiplier();
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"))
    public float waxGlandLava(float speed) {
        var capability = this.accessoriesCapability();

        if (capability != null && capability.isEquipped(ThingsItems.ENCHANTED_WAX_GLAND) && capability.isEquipped(ThingsItems.HADES_CRYSTAL)) {
            float depthStrider = (float) (this.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY) * 3);
            return 0.0175f * Things.CONFIG.waxGlandMultiplier() + 0.1f * depthStrider;
        }

        return speed;
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "causeFallDamage", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(FF)I"))
    private int decreaseFallDamage(int originalFallDamage) {
        var capability = this.accessoriesCapability();

        if (capability != null && capability.isEquipped(ThingsItems.SHOCK_ABSORBER)) {
            return originalFallDamage - (int) Math.min(16, originalFallDamage * 0.20f);
        }

        return originalFallDamage;
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float decreaseKineticDamage(DamageSource source, float damage) {
        if (source.type() != this.level().damageSources().flyIntoWall().type())
            return damage;

        var capability = this.accessoriesCapability();

        if (capability != null && capability.isEquipped(ThingsItems.SHOCK_ABSORBER)) {
            return damage / 4;
        }

        return damage;
    }

    @ModifyArg(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), index = 1)
    private Object attachPlayerToEffect(Object effect) {
        ((ExtendedStatusEffectInstance) effect).things$setAttachedEntity((LivingEntity) (Object) this);

        return effect;
    }

    @Inject(method = "onEffectAdded", at = @At("HEAD"))
    private void attachPlayerToEffect(MobEffectInstance effect, Entity source, CallbackInfo ci) {
        ((ExtendedStatusEffectInstance) effect).things$setAttachedEntity((LivingEntity) (Object) this);
    }

    @Inject(method = "onEffectUpdated", at = @At("HEAD"))
    private void attachPlayerToEffect(MobEffectInstance effect, boolean reapplyEffect, Entity source, CallbackInfo ci) {
        ((ExtendedStatusEffectInstance) effect).things$setAttachedEntity((LivingEntity) (Object) this);
    }
}
