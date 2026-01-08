package com.glisco.things.items.generic;

import com.glisco.things.Things;
import com.glisco.things.items.ItemWithExtendableTooltip;
import com.glisco.things.mixin.access.ContainerLockAccessor;
import com.glisco.things.mixin.access.LockableContainerBlockEntityAccessor;
import com.mojang.serialization.Codec;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ContainerKeyItem extends ItemWithExtendableTooltip {

    public static final DataComponentType<Integer> LOCK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Things.id("container_key_lock"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    public ContainerKeyItem() {
        super(((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(1));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getPlayer().isShiftKeyDown()) return InteractionResult.PASS;

        createKey(context.getItemInHand(), context.getLevel().random);

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (!(world.getBlockEntity(pos) instanceof BaseContainerBlockEntity)) return InteractionResult.PASS;

        String existingLock = getExistingLock(world, pos);

        if (existingLock.isEmpty()) {
            setLock((BaseContainerBlockEntity) world.getBlockEntity(pos), String.valueOf(stack.get(LOCK)));

            if (world.isClientSide) {
                sendLockedState(context, true);
            }

            return InteractionResult.SUCCESS;
        } else if (existingLock.equals(String.valueOf(stack.get(LOCK)))) {
            setLock((BaseContainerBlockEntity) world.getBlockEntity(pos), "");

            if (world.isClientSide) {
                sendLockedState(context, false);
            }

            return InteractionResult.SUCCESS;
        } else {

            if (world.isClientSide) {
                context.getPlayer().playSound(SoundEvents.CHEST_LOCKED, 1, 1);

                MutableComponent containerName =
                        (MutableComponent) ((BaseContainerBlockEntity) context.getLevel().getBlockEntity(context.getClickedPos())).getDisplayName();
                context.getPlayer().displayClientMessage(containerName.append(Component.literal(" is locked with another key!")), true);
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        createKey(stack, world.random);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        if (stack.has(LOCK)) {
            tooltip.add(Component.literal("§9Key: §7#" + Integer.toHexString(stack.get(LOCK))));
        }

        super.appendHoverText(stack, context, tooltip, type);
    }

    private static void createKey(ItemStack stack, RandomSource random) {
        if (stack.has(LOCK)) return;
        stack.set(LOCK, random.nextInt(200000));
    }

    private static String getExistingLock(Level world, BlockPos pos) {
        final var blockEntity = world.getBlockEntity(pos);
        String existingLock = getKey(blockEntity);

        var chestNeighbor = maybeGetOtherChest(blockEntity);
        if (existingLock.isEmpty() && chestNeighbor != null) {
            if (getKey(chestNeighbor).isEmpty()) return existingLock;

            return getKey(chestNeighbor);
        }

        return existingLock;
    }

    private static void sendLockedState(UseOnContext ctx, boolean locked) {
        ctx.getPlayer().playSound(SoundEvents.CHEST_LOCKED, 1, 1);

        MutableComponent containerName = (MutableComponent) ((BaseContainerBlockEntity) ctx.getLevel().getBlockEntity(ctx.getClickedPos())).getDisplayName();
        ctx.getPlayer().displayClientMessage(containerName.append(Component.literal(locked ? " locked!" : " unlocked!")), true);
    }

    private static void setLock(BaseContainerBlockEntity entity, String lock) {
        CompoundTag lockNbt = new CompoundTag();
        lockNbt.putString("Lock", lock);

        LockCode containerLock = lock.isEmpty() ? LockCode.NO_LOCK : LockCode.fromTag(lockNbt);

        ((LockableContainerBlockEntityAccessor) entity).things$setLock(containerLock);
        final var doubleChestNeighbor = maybeGetOtherChest(entity);
        if (doubleChestNeighbor == null) return;

        ((LockableContainerBlockEntityAccessor) doubleChestNeighbor).things$setLock(containerLock);
    }

    private static String getKey(BlockEntity be) {
        return ((ContainerLockAccessor) (Object) ((LockableContainerBlockEntityAccessor) be).things$getLock()).things$getKey();
    }

    @SuppressWarnings("ConstantConditions")
    private static @Nullable ChestBlockEntity maybeGetOtherChest(BlockEntity potentialChest) {
        if (!(potentialChest instanceof ChestBlockEntity)) return null;
        if (potentialChest.getBlockState().getValue(BlockStateProperties.CHEST_TYPE) == ChestType.SINGLE) return null;
        return (ChestBlockEntity) potentialChest.getLevel().getBlockEntity(potentialChest.getBlockPos().relative(ChestBlock.getConnectedDirection(potentialChest.getBlockState())));
    }
}
