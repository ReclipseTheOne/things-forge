package com.glisco.things.misc;

import net.minecraft.world.entity.LivingEntity;

public interface ExtendedStatusEffectInstance {
    void things$setAttachedEntity(LivingEntity entity);

    LivingEntity things$getAttachedEntity();
}
