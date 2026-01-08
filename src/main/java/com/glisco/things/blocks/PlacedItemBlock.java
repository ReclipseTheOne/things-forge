package com.glisco.things.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PlacedItemBlock extends BaseEntityBlock {

    public static DirectionProperty FACING = BlockStateProperties.FACING;

    private final VoxelShape OUTLINE_SHAPE_DOWN = Block.box(5, 0, 5, 11, 1, 11);
    private final VoxelShape OUTLINE_SHAPE_UP = Block.box(5, 15, 5, 11, 16, 11);
    private final VoxelShape OUTLINE_SHAPE_NORTH = Block.box(5, 5, 0, 11, 11, 1);
    private final VoxelShape OUTLINE_SHAPE_SOUTH = Block.box(5, 5, 15, 11, 11, 16);
    private final VoxelShape OUTLINE_SHAPE_EAST = Block.box(15, 5, 5, 16, 11, 11);
    private final VoxelShape OUTLINE_SHAPE_WEST = Block.box(0, 5, 5, 1, 11, 11);
    private final VoxelShape EMPTY_SHAPE = Block.box(0, 0, 0, 0, 0, 0);

    public PlacedItemBlock() {
        super(BlockBehaviour.Properties.of().noOcclusion().destroyTime(-1).sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlacedItemBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case UP:
                return OUTLINE_SHAPE_UP;
            case DOWN:
                return OUTLINE_SHAPE_DOWN;
            case EAST:
                return OUTLINE_SHAPE_EAST;
            case WEST:
                return OUTLINE_SHAPE_WEST;
            case NORTH:
                return OUTLINE_SHAPE_NORTH;
            case SOUTH:
                return OUTLINE_SHAPE_SOUTH;
        }
        return EMPTY_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return EMPTY_SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        PlacedItemBlockEntity entity = (PlacedItemBlockEntity) world.getBlockEntity(pos);
        entity.changeRotation(!player.isShiftKeyDown());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void attack(BlockState state, Level world, BlockPos pos, Player player) {
        player.getInventory().placeItemBackInInventory(((PlacedItemBlockEntity) world.getBlockEntity(pos)).getItem());
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor world, BlockPos pos, BlockPos posFrom) {
        return direction == state.getValue(FACING) && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction);
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isFaceSturdy(world, blockPos, direction.getOpposite());
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), ((PlacedItemBlockEntity) world.getBlockEntity(pos)).getItem());
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof PlacedItemBlockEntity placedItem) {
            return placedItem.getItem().copy();
        } else {
            return super.getCloneItemStack(world, pos, state);
        }
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }
}
