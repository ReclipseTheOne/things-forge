package com.glisco.things.mixin;

import com.glisco.things.text.TooltipComponentText;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface TooltipComponentMixin {
    @Inject(method = "create(Lnet/minecraft/util/FormattedCharSequence;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void things$unwrapOrderedTooltipHolder(FormattedCharSequence text, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if(text instanceof TooltipComponentText.TooltipDataAsOrderedText holder) {
            cir.setReturnValue(ClientTooltipComponent.create(holder.tooltipData()));
        }
    }
}
