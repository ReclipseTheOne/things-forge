package com.glisco.things.mixin;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.mixin.access.ForgingScreenHandlerAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin {

    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    private String itemName;

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void outputCheckOverride(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        ForgingScreenHandlerAccessor handler = (ForgingScreenHandlerAccessor) this;

        if (!handler.things$getInput().getItem(1).getItem().equals(ThingsItems.HARDENING_CATALYST)) return;

        cir.setReturnValue(cost.get() <= player.experienceLevel);
        cir.cancel();
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    public void setOutput(CallbackInfo ci) {
        ForgingScreenHandlerAccessor forgingHandler = (ForgingScreenHandlerAccessor) this;

        final var inputInventory = forgingHandler.things$getInput();
        if (!inputInventory.getItem(1).getItem().equals(ThingsItems.HARDENING_CATALYST)) return;

        final var baseStack = inputInventory.getItem(0);

        if (!baseStack.getItem().components().has(DataComponents.MAX_DAMAGE) || baseStack.is(Things.HARDENING_CATALYST_BLACKLIST)) return;
        if (baseStack.has(DataComponents.UNBREAKABLE)) return;

        ItemStack newOutput = baseStack.copy();
        newOutput.set(DataComponents.UNBREAKABLE, new Unbreakable(true));

        if (!StringUtils.isBlank(itemName)) {
            newOutput.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
        } else {
            newOutput.remove(DataComponents.CUSTOM_DATA);
        }

        forgingHandler.things$getOutput().setItem(0, newOutput);
        cost.set(30);

        ci.cancel();
    }
}
