package com.glisco.things.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

@SuppressWarnings("deprecation")
public class GlowstoneFixtureBlock extends DirectionalBlock implements SimpleWaterloggedBlock {

    private static final VoxelShape BASE_DOWN = Block.box(5, 0, 5, 11, 1, 11);
    private static final VoxelShape GLOWSTONE_DOWN = Block.box(6, 1, 6, 10, 2, 10);
    private static final VoxelShape BASE_UP = Block.box(5, 15, 5, 11, 16, 11);
    private static final VoxelShape GLOWSTONE_UP = Block.box(6, 14, 6, 10, 15, 10);

    private static final VoxelShape BASE_NORTH = Block.box(5, 5, 0, 11, 11, 1);
    private static final VoxelShape GLOWSTONE_NORTH = Block.box(6, 6, 1, 10, 10, 2);
    private static final VoxelShape BASE_SOUTH = Block.box(5, 5, 15, 11, 11, 16);
    private static final VoxelShape GLOWSTONE_SOUTH = Block.box(6, 6, 14, 10, 10, 15);

    private static final VoxelShape BASE_WEST = Block.box(0, 5, 5, 1, 11, 11);
    private static final VoxelShape GLOWSTONE_WEST = Block.box(1, 6, 6, 2, 10, 10);
    private static final VoxelShape BASE_EAST = Block.box(15, 5, 5, 16, 11, 11);
    private static final VoxelShape GLOWSTONE_EAST = Block.box(14, 6, 6, 15, 10, 10);

    private static final VoxelShape SHAPE_DOWN = Shapes.or(BASE_DOWN, GLOWSTONE_DOWN);
    private static final VoxelShape SHAPE_UP = Shapes.or(BASE_UP, GLOWSTONE_UP);
    private static final VoxelShape SHAPE_NORTH = Shapes.or(BASE_NORTH, GLOWSTONE_NORTH);
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(BASE_SOUTH, GLOWSTONE_SOUTH);
    private static final VoxelShape SHAPE_WEST = Shapes.or(BASE_WEST, GLOWSTONE_WEST);
    private static final VoxelShape SHAPE_EAST = Shapes.or(BASE_EAST, GLOWSTONE_EAST);

    public GlowstoneFixtureBlock() {
        super(BlockBehaviour.Properties.of().noOcclusion().lightLevel((a) ->15).requiresCorrectToolForDrops().destroyTime(1));
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(BlockStateProperties.FACING, BlockStateProperties.WATERLOGGED);
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, ctx.getClickedFace().getOpposite()).setValue(BlockStateProperties.WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).is(FluidTags.WATER));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> SHAPE_UP;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            default -> SHAPE_DOWN;
        };
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {

        if (random.nextDouble() > 0.15) return;

        var facing = state.getValue(FACING);
        var center = Vec3.atCenterOf(pos);

        double x = center.x() + facing.getStepX() * .35f;
        double y = center.y() + facing.getStepY() * .35f;
        double z = center.z() + facing.getStepZ() * .35f;

        world.addParticle(new DustParticleOptions(new Vector3f(1, 1, 1), 1),
                x, y, z, 0, 0, 0);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction);
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isFaceSturdy(world, blockPos, direction.getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor world, BlockPos pos, BlockPos posFrom) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        return direction == state.getValue(FACING) && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return null;
    }
}
