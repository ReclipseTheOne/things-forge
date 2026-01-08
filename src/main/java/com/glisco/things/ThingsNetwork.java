package com.glisco.things;

import com.glisco.things.blocks.PlacedItemBlockEntity;
import com.glisco.things.blocks.ThingsBlocks;
import com.glisco.things.client.DisplacementTomeScreen;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.items.trinkets.AgglomerationItem;
import com.glisco.things.items.trinkets.SocksItem;
import com.glisco.things.misc.DisplacementTomeScreenHandler;
import com.glisco.things.misc.DisplacementTomeScreenHandler.ActionPacket.Action;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.ops.WorldOps;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThingsNetwork {

    public static final Logger LOGGER = LogManager.getLogger("things-network");
    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(Things.id("main"));

    private static final MenuProvider ENDER_POUCH_FACTORY = new SimpleMenuProvider((syncId, inv, player) ->
            ChestMenu.threeRows(syncId, inv, player.getEnderChestInventory()),
            Component.translatable("container.enderpouch"));

    public static void init() {
        CHANNEL.registerServerbound(OpenEnderChestPacket.class, (message, access) -> {
            final var player = access.player();
            final var capability = AccessoriesCapability.get(player);

            if (capability == null || !capability.isEquipped(ThingsItems.ENDER_POUCH)) {
                LOGGER.warn("Received illegal openEChest packet");
                return;
            }

            player.openMenu(ENDER_POUCH_FACTORY);
        });

        CHANNEL.registerServerbound(DisplacementTomeScreenHandler.ActionPacket.class, (message, access) -> {
            if (!(access.player().containerMenu instanceof DisplacementTomeScreenHandler handler)) return;
            final var action = message.action();

            switch (action) {
                case TELEPORT -> handler.requestTeleport(message.data());
                case CREATE_POINT -> handler.addPoint(message.data());
                case DELETE_POINT -> {
                    if (!handler.deletePoint(message.data())) ThingsNetwork.LOGGER.warn("Received invalid DELETE_POINT request");
                }
                case RENAME_POINT -> {
                    if (!handler.renamePoint(message.data())) ThingsNetwork.LOGGER.warn("Received invalid RENAME_POINT request");
                }
            }
        });

        CHANNEL.registerClientbound(DisplacementTomeScreenHandler.UpdateClientPacket.class, (message, access) -> {
            if (!(access.runtime().screen instanceof final DisplacementTomeScreen tomeScreen)) return;
            tomeScreen.getMenu().setBook(message.tome());
            tomeScreen.rebuildWidgets();
        });

        CHANNEL.registerServerbound(PlaceItemPacket.class, (message, access) -> {
            final var target = message.target();
            final var pos = target.getBlockPos().relative(target.getDirection());

            final var world = access.player().level();
            if (!world.getBlockState(pos).isAir()) return;
            if (!world.isLoaded(pos)) {
                LOGGER.warn("Received illegal place item packet");
                return;
            }

            final var stack = access.player().getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.isEmpty()) return;

            if (!world.getBlockState(target.getBlockPos()).isFaceSturdy(world, target.getBlockPos(), target.getDirection())) return;

            world.setBlockAndUpdate(pos, ThingsBlocks.PLACED_ITEM.defaultBlockState().setValue(BlockStateProperties.FACING, target.getDirection().getOpposite()));
            ((PlacedItemBlockEntity) world.getBlockEntity(pos)).setItem(ItemOps.singleCopy(stack));
            stack.shrink(1);
        });

        CHANNEL.registerServerbound(ToggleSocksJumpBoostPacket.class, (message, access) -> {
            var player = access.player();
            var capability = AccessoriesCapability.get(player);
            if (capability == null || !capability.isEquipped(ThingsItems.SOCKS)) return;

            var socks = capability.getEquipped(ThingsItems.SOCKS).get(0).stack();
            if (!socks.has(SocksItem.JUMPY_AND_ENABLED)) return;

            socks.set(SocksItem.JUMPY_AND_ENABLED, !socks.get(SocksItem.JUMPY_AND_ENABLED));

            WorldOps.playSound(player.level(), player.position(), SoundEvents.UI_TOAST_IN, SoundSource.PLAYERS, 1, 2);
            Things.TOGGLE_JUMP_BOOST_PARTICLES.spawn(player.level(), player.position());
        });

        CHANNEL.registerServerbound(AgglomerationItem.ScrollHandStackTrinket.class, AgglomerationItem.ScrollHandStackTrinket::scrollItemStack);
        CHANNEL.registerServerbound(AgglomerationItem.ScrollStackFromSlotTrinket.class, AgglomerationItem.ScrollStackFromSlotTrinket::scrollItemStack);
    }


    public record OpenEnderChestPacket() {}

    public record PlaceItemPacket(BlockHitResult target) {}

    public record ToggleSocksJumpBoostPacket() {}
}