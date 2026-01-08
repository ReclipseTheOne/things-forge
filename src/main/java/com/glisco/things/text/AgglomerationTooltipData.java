package com.glisco.things.text;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record AgglomerationTooltipData(Component beginningText, ItemStack stack, Component endText) implements TooltipComponent {}
