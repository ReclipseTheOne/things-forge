package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.client.ThingsClient;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.items.TrinketItemWithOptionalTooltip;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Socks trinket that provides movement speed and optional jump boost.
 */
public class SocksItem extends TrinketItemWithOptionalTooltip {
    public static final DataComponentType<Integer> SPEED = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Things.id("socks_speed"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    public static final DataComponentType<Boolean> JUMPY_AND_ENABLED = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Things.id("jumpy_and_enabled"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    public SocksItem() {
        super(((OwoItemSettingsExtension) new Item.Properties().stacksTo(1)).group(() -> Things.THINGS_GROUP).component(SPEED, 0));
    }

    public static ItemStack create(int speed, boolean jumpy) {
        var stack = new ItemStack(ThingsItems.SOCKS);
        stack.set(SPEED, speed);
        if (jumpy) stack.set(JUMPY_AND_ENABLED, true);
        return stack;
    }

	@Override
    public void getDynamicModifiers(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
		if(!stack.has(JUMPY_AND_ENABLED)) return;

        if (stack.has(JUMPY_AND_ENABLED)) {
	        builder.addExclusive(Attributes.STEP_HEIGHT, new AttributeModifier(Things.id("socks.step_height"), 0.45, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void tick(ItemStack stack, SlotReference slotContext) {
        LivingEntity entity = slotContext.entity();
        if (!(entity instanceof Player player)) return;

        final var sockData = Things.getSockData(player);

        sockData.jumpySocksEquipped = stack.has(JUMPY_AND_ENABLED);
        sockData.setBearer(player);

        if (player.level().isClientSide) return;

        sockData.updateSockSpeed(slotContext.slot(), stack.get(SPEED) + 1);

        if (!sockData.jumpySocksEquipped || !stack.getOrDefault(JUMPY_AND_ENABLED, true)) return;
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 5, 1, true, false, true));
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slotContext) {
        LivingEntity entity = slotContext.entity();

        Things.getSockData(entity).jumpySocksEquipped = false;

        if (!(entity instanceof ServerPlayer player)) return;
        int speed = stack.getOrDefault(SPEED, 0);

        var sockData = Things.getSockData(player);
        sockData.setBearer(player);
        sockData.modifySpeed(-Things.CONFIG.sockPerLevelSpeedAmplifier() * (speed + 1));
        sockData.clearSockSpeed(slotContext.slot());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void append(List<Component> tooltip) {
        this.appendWrapped(tooltip, Component.translatable(this.tooltipTranslationKey(),
                ThingsClient.TOGGLE_SOCKS_JUMP_BOOST != null
                        ? ThingsClient.TOGGLE_SOCKS_JUMP_BOOST.getTranslatedKeyMessage()
                        : "?"));
    }

    @Override
    public void getExtraTooltip(ItemStack stack, List<Component> tooltips, TooltipContext tooltipContext, TooltipFlag tooltipType) {
        var extraTooltips = new ArrayList<Component>();

        tryAppend(extraTooltips);

        if (stack.has(JUMPY_AND_ENABLED)) {
            extraTooltips.add(TextOps.withColor("↑ ", !stack.get(JUMPY_AND_ENABLED) ? TextOps.color(ChatFormatting.GRAY) : 0x34d49c)
                    .append(TextOps.translateWithColor("item.things.socks.jumpy", TextOps.color(ChatFormatting.GRAY))));
        }

        int speed = stack.getOrDefault(SPEED, 0);
        if (speed < 3) {
            extraTooltips.add(TextOps.withColor("☄ ", 0x34b1d4)
                    .append(TextOps.translateWithColor("item.things.socks.speed_" + speed, TextOps.color(ChatFormatting.GRAY))));
        } else {
            extraTooltips.add(TextOps.withColor("☄ ", 0x34b1d4)
                    .append(TextOps.translateWithColor("item.things.socks.speed_illegal", TextOps.color(ChatFormatting.RED)))
                    .append(TextOps.withColor(" (" + speed + ")", TextOps.color(ChatFormatting.RED))));
        }

        extraTooltips.add(Component.literal(" "));

        tooltips.addAll(0, extraTooltips);
    }
}
