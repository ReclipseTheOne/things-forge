package com.glisco.things.mixin.access;

import net.minecraft.world.LockCode;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseContainerBlockEntity.class)
public interface LockableContainerBlockEntityAccessor {

    @Accessor("lock")
    void things$setLock(LockCode lock);

    @Accessor("lock")
    LockCode things$getLock();

}
