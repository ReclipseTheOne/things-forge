package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.client.SimplePlayerTrinketRenderer;
import com.glisco.things.client.ThingsClient;
import com.glisco.things.items.TrinketItemWithOptionalTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;

import java.util.List;

public class EnderPouchItem extends TrinketItemWithOptionalTooltip implements SimplePlayerTrinketRenderer {
    public EnderPouchItem() {
        super(((OwoItemSettingsExtension) new Item.Properties().stacksTo(1)).group(() -> Things.THINGS_GROUP));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void append(List<Component> tooltip) {
        tooltip.add(Component.translatable(this.tooltipTranslationKey(), ThingsClient.OPEN_ENDER_CHEST.getDisplayName()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, HumanoidModel<M> model, PoseStack matrices) {
        AccessoryRenderer.transformToModelPart(matrices, model.body, 1, -0.925, 0);
        matrices.mulPose(Axis.YP.rotationDegrees(-90));
        matrices.scale(.35f, .35f, .35f);
        matrices.translate(0, 0, 0.015);
    }
}
