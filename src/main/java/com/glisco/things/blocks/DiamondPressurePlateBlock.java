package com.glisco.things.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class DiamondPressurePlateBlock extends PressurePlateBlock {

    protected DiamondPressurePlateBlock() {
        super(BlockSetType.IRON, BlockBehaviour.Properties.ofFullCopy(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE).sound(SoundType.METAL).mapColor(MapColor.DIAMOND));
    }

    @Override
    protected int getSignalStrength(Level world, BlockPos pos) {
        AABB box = TOUCH_AABB.move(pos);
        List<Player> entities = world.getEntitiesOfClass(Player.class, box);

        if (!entities.isEmpty()) {
            for (Entity entity : entities) {
                if (!entity.isIgnoringBlockTriggers()) {
                    return 15;
                }
            }
        }

        return 0;
    }
}
