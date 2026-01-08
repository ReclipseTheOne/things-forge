package com.glisco.things.items;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public abstract class ItemWithExtendableTooltip extends Item implements ExtendableTooltipProvider {

    public ItemWithExtendableTooltip(Properties settings) {
        super(settings);
    }

    @Override
    public String tooltipTranslationKey() {
        return this.getDescriptionId() + ".tooltip";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        this.tryAppend(tooltip);
    }
}
