package com.glisco.things.mixin.access;

import net.minecraft.world.LockCode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LockCode.class)
public interface ContainerLockAccessor {

    @Accessor("key")
    String things$getKey();
}
