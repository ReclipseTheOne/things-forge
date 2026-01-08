package com.glisco.things.mixin;

import com.glisco.things.Things;
import com.glisco.things.mixin.access.ContainerLockAccessor;
import com.glisco.things.mixin.access.LockableContainerBlockEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Mixin(ExplosionDamageCalculator.class)
public class ExplosionBehaviorMixin {

    @Inject(method = "getBlastResistance", at = @At("HEAD"), cancellable = true)
    private void disallowBreakingLockedContainers(Explosion explosion, BlockGetter world, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfoReturnable<Optional<Float>> cir) {
        if (!Things.CONFIG.makeLockedContainersUnbreakable()) return;

        if (!(world.getBlockEntity(pos) instanceof LockableContainerBlockEntityAccessor lockable) ||
                ((ContainerLockAccessor) (Object) lockable.things$getLock()).things$getKey().isEmpty()) return;

        cir.setReturnValue(Optional.of(3600000f));
    }

}
