package com.glisco.things.items;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;

public interface ExtendableTooltipProvider {

    Component TOOLTIP_HINT = Component.translatable("text.things.tooltip_hint");

    String tooltipTranslationKey();

    @OnlyIn(Dist.CLIENT)
    default boolean hasExtendedTooltip() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    default void tryAppend(List<Component> tooltip) {
        if (!this.hasExtendedTooltip()) return;

        if (Screen.hasShiftDown()) this.append(tooltip);
        else tooltip.add(TOOLTIP_HINT);
    }

    @OnlyIn(Dist.CLIENT)
    default void append(List<Component> tooltip) {
        this.appendWrapped(tooltip, Component.translatable(this.tooltipTranslationKey()));
    }

    @OnlyIn(Dist.CLIENT)
    default void appendWrapped(List<Component> tooltip, Component toAppend) {
        Minecraft.getInstance().font.getSplitter().splitLines(toAppend, 220, Style.EMPTY.applyFormat(ChatFormatting.GRAY))
                .stream()
                .map(VisitableTextContent::new)
                .map(MutableComponent::create)
                .forEach(tooltip::add);
    }

    record VisitableTextContent(FormattedText content) implements ComponentContents {

        private static final Type<VisitableTextContent> DUMMY_TYPE = new Type<>(MapCodec.unit(new VisitableTextContent(FormattedText.EMPTY)), "idwtialsimmoedm:visitable_text");

        @Override
        public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> visitor, Style style) {
            return this.content.visit(visitor, style);
        }

        @Override
        public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
            return this.content.visit(visitor);
        }

        @Override
        public Type<?> type() {
            return DUMMY_TYPE;
        }
    }
}
