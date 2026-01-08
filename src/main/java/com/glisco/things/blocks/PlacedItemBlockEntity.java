package com.glisco.things.blocks;

import io.wispforest.accessories.endec.NbtMapCarrier;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlacedItemBlockEntity extends BlockEntity {

    private static final KeyedEndec<ItemStack> ITEM_KEY = MinecraftEndecs.ITEM_STACK.keyed("item", ItemStack.EMPTY);
    private static final KeyedEndec<Integer> ROTATION_KEY = Endec.INT.keyed("rotation", 0);

    private @NotNull ItemStack item = ItemStack.EMPTY;
    private int rotation = 0;

    public PlacedItemBlockEntity(BlockPos pos, BlockState state) {
        super(ThingsBlocks.PLACED_ITEM_BLOCK_ENTITY, pos, state);
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = item;
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        var ctx = SerializationContext.attributes(RegistriesAttribute.of((RegistryAccess) registries));
	    NbtMapCarrier nbt = new NbtMapCarrier(tag);

        nbt.put(ctx, ITEM_KEY, this.item);
        nbt.put(ctx, ROTATION_KEY, rotation);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

	    var ctx = SerializationContext.attributes(RegistriesAttribute.of((RegistryAccess) registries));
	    NbtMapCarrier nbt = new NbtMapCarrier(tag);

        this.item = nbt.get(ctx, ITEM_KEY);
        this.rotation = nbt.get(ctx, ROTATION_KEY);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        WorldOps.updateIfOnServer(level, worldPosition);
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
        if (this.rotation > 7) this.rotation = 0;
        if (this.rotation < 0) this.rotation = 7;
        this.setChanged();
    }

    public void changeRotation(boolean direction) {
        setRotation(direction ? rotation + 1 : rotation - 1);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }
}
