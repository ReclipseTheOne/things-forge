package com.glisco.things.mixin.client;

import com.glisco.things.ThingsNetwork;
import com.glisco.things.items.trinkets.AgglomerationItem;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.wispforest.accessories.api.components.AccessoriesDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {

    @Shadow
    @Final
    private Minecraft client;

    // TODO agglomeration item select
    @WrapWithCondition(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    private boolean beforePlayerScrollHotbar(Inventory instance, double scrollAmount) {
        LocalPlayer player = this.client.player;

        if (!player.isSecondaryUseActive()) return true;

        boolean scrollMainHandStack;

        var mainHandStack = player.getMainHandItem();
        var offHandStack = player.getOffhandItem();

        if (mainHandStack.getItem() instanceof AgglomerationItem && mainHandStack.has(AccessoriesDataComponents.NESTED_ACCESSORIES)) {
            scrollMainHandStack = true;
        } else if (offHandStack.getItem() instanceof AgglomerationItem && offHandStack.has(AccessoriesDataComponents.NESTED_ACCESSORIES)) {
            scrollMainHandStack = false;
        } else {
            return true;
        }

        ThingsNetwork.CHANNEL.clientHandle().send(new AgglomerationItem.ScrollHandStackTrinket(scrollMainHandStack));

        return false;
    }
}
