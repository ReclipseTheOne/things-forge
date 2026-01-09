package com.glisco.things.misc;

import com.glisco.things.Things;
import com.glisco.things.ThingsNetwork;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.items.generic.DisplacementTomeItem;
import io.wispforest.owo.client.screens.ScreenUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class DisplacementTomeScreenHandler extends AbstractContainerMenu {

    private ItemStack book;

    public DisplacementTomeScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ItemStack.EMPTY);
    }

    public DisplacementTomeScreenHandler(int syncId, Inventory playerInventory, ItemStack book) {
        super(Things.DISPLACEMENT_TOME_SCREEN_HANDLER.get(), syncId);
        this.book = book;
    }

    @Override
    public void addSlotListener(ContainerListener listener) {
        super.addSlotListener(listener);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof DisplacementTomeItem || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof DisplacementTomeItem;
    }

    public void setBook(ItemStack book) {
        this.book = book;
    }

    public void requestTeleport(ServerPlayer player, String location) {
        int currentFuel = book.get(DisplacementTomeItem.FUEL);

        if (currentFuel < Things.CONFIG.displacementTomeFuelConsumption()) {
            player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 0);
            return;
        }

        var targets = book.get(DisplacementTomeItem.TARGETS);
        if (!targets.containsKey(location)) return;

        currentFuel -= Things.CONFIG.displacementTomeFuelConsumption();
        book.set(DisplacementTomeItem.FUEL, currentFuel);

        targets.get(location).teleportPlayer(player);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.MASTER, 1, 1);
        player.closeContainer();
    }

    public void addPoint(ServerPlayer player, String name) {
        player.getInventory().getItem(player.getInventory().findSlotMatchingItem(new ItemStack(ThingsItems.DISPLACEMENT_PAGE))).shrink(1);
        broadcastChanges();
        DisplacementTomeItem.storeTeleportTargetInBook(book,
                DisplacementTomeItem.Target.fromPlayer(player), name, false);
        updateClient(player);
    }

    public boolean deletePoint(ServerPlayer player, String name) {
        boolean result = DisplacementTomeItem.deletePoint(book, name);
        updateClient(player);
        return result;
    }

    public boolean renamePoint(ServerPlayer player, String data) {
        boolean result = DisplacementTomeItem.rename(book, data);
        updateClient(player);
        return result;
    }

    private void updateClient(ServerPlayer player) {
        ThingsNetwork.CHANNEL.serverHandle(player).send(new UpdateClientPacket(book));
    }

    public ItemStack getBook() {
        return book;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer)) {
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ScreenUtils.handleSlotTransfer(this, index, 0);
    }

    public record UpdateClientPacket(ItemStack tome) {}

    public record ActionPacket(Action action, String data) {
        public enum Action {TELEPORT, DELETE_POINT, RENAME_POINT, CREATE_POINT}

        public static ActionPacket teleport(String where) {
            return new ActionPacket(Action.TELEPORT, where);
        }

        public static ActionPacket create(String what) {
            return new ActionPacket(Action.CREATE_POINT, what);
        }

        public static ActionPacket rename(String which) {
            return new ActionPacket(Action.RENAME_POINT, which);
        }

        public static ActionPacket delete(String which) {
            return new ActionPacket(Action.DELETE_POINT, which);
        }
    }
}
