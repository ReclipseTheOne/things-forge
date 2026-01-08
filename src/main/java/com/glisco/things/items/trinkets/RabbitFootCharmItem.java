package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.items.TrinketItemWithOptionalTooltip;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RabbitFootCharmItem extends TrinketItemWithOptionalTooltip {

    public RabbitFootCharmItem() {
        super(((OwoItemSettingsExtension) new Item.Properties().stacksTo(1)).group(() -> Things.THINGS_GROUP));
    }

    @Override
    public void tick(ItemStack stack, SlotReference reference) {
        if (!(reference.entity() instanceof ServerPlayer player)) return;

        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 5, 1, true, false, true));
    }
}