package com.glisco.things.mixin;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.misc.ExtendedStatusEffectInstance;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectInstance.class)
public abstract class StatusEffectInstanceMixin implements ExtendedStatusEffectInstance {
    @Shadow private int duration;
    private int things$tickNum = 0;
    private LivingEntity things$attachedEntity;

    @Inject(method = "tickDownDuration", at = @At("HEAD"), cancellable = true)
    private void skipUpdate(CallbackInfoReturnable<Integer> cir) {
        if (things$attachedEntity == null) return;

        var capability = AccessoriesCapability.get(things$attachedEntity);

        if (capability == null || !capability.isEquipped(ThingsItems.BROKEN_WATCH))
	        if (things$tickNum == 2) {
	            cir.setReturnValue(duration);
	            things$tickNum = 0;
	        } else {
	            things$tickNum++;
	        }
    }

    @Override
    public void things$setAttachedEntity(LivingEntity entity) {
        things$attachedEntity = entity;
    }

    @Override
    public LivingEntity things$getAttachedEntity() {
        return things$attachedEntity;
    }
}
