package com.glisco.things.items;

import io.wispforest.accessories.api.AccessoryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for trinket items that use Curios API and support optional tooltips.
 */
public abstract class TrinketItemWithOptionalTooltip extends AccessoryItem implements ExtendableTooltipProvider {

    public TrinketItemWithOptionalTooltip(Properties settings) {
        super(settings);
    }

    @Override
    public String tooltipTranslationKey() {
        return this.getDescriptionId() + ".tooltip";
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag tooltipType) {
        super.appendHoverText(stack, context, tooltips, tooltipType);
        getExtraTooltip(stack, tooltips, context, tooltipType);
    }

    public void getExtraTooltip(ItemStack stack, List<Component> tooltips, TooltipContext tooltipContext, TooltipFlag tooltipType) {
        var extraData = new ArrayList<Component>();
        tryAppend(extraData);
        tooltips.addAll(0, extraData);
    }
}
