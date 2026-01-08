package com.glisco.things.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import java.util.List;

public record TooltipComponentText(TooltipComponent tooltipData) implements Component {

    @Override
    public Style getStyle() {
        return Style.EMPTY;
    }

    @Override
    public ComponentContents getContents() {
        return PlainTextContents.EMPTY;
    }

    @Override
    public List<Component> getSiblings() {
        return List.of();
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return new TooltipDataAsOrderedText(tooltipData());
    }

    public record TooltipDataAsOrderedText(TooltipComponent tooltipData) implements FormattedCharSequence {
        @Override
        public boolean accept(FormattedCharSink visitor) {
            return false;
        }
    }
}
