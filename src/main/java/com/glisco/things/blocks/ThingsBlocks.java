package com.glisco.things.blocks;

import com.glisco.things.Things;
import com.mojang.datafixers.types.Type;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import io.wispforest.owo.registration.reflect.BlockRegistryContainer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ThingsBlocks implements BlockRegistryContainer {

    public static final Block STONE_GLOWSTONE_FIXTURE = new GlowstoneFixtureBlock();
    public static final Block QUARTZ_GLOWSTONE_FIXTURE = new GlowstoneFixtureBlock();
    public static final Block DEEPSLATE_GLOWSTONE_FIXTURE = new GlowstoneFixtureBlock();

    public static final Block GLEAMING_ORE = new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE).lightLevel($ -> 5).requiresCorrectToolForDrops());
    public static final Block DEEPSLATE_GLEAMING_ORE = new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE).lightLevel($ -> 5).requiresCorrectToolForDrops());

    public static final Block DIAMOND_PRESSURE_PLATE = new DiamondPressurePlateBlock();
    public static final BlockItem DIAMOND_PRESSURE_PLATE_ITEM = new BlockItem(ThingsBlocks.DIAMOND_PRESSURE_PLATE, ((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP)) {
        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
            super.appendHoverText(stack, context, tooltip, type);
            tooltip.add(Component.literal("Players only").withStyle(ChatFormatting.GRAY));
        }
    };

    @NoBlockItem
    public static final Block PLACED_ITEM = new PlacedItemBlock();
    public static final BlockEntityType<PlacedItemBlockEntity> PLACED_ITEM_BLOCK_ENTITY = BlockEntityType.Builder.of(PlacedItemBlockEntity::new, PLACED_ITEM).build(null);

    @Override
    public BlockItem createBlockItem(Block block, String identifier) {
        return block == DIAMOND_PRESSURE_PLATE ? DIAMOND_PRESSURE_PLATE_ITEM : new BlockItem(block, ((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP));
    }

    @Override
    public void afterFieldProcessing() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Things.id("placed_item"), PLACED_ITEM_BLOCK_ENTITY);
    }
}
