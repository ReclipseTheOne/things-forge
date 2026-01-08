package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.client.SimplePlayerTrinketRenderer;
import com.glisco.things.items.TrinketItemWithOptionalTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.client.Side;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class HadesCrystalItem extends TrinketItemWithOptionalTooltip implements SimplePlayerTrinketRenderer {

    public HadesCrystalItem() {
        super(((OwoItemSettingsExtension) new Item.Properties().stacksTo(1)).group(() -> Things.THINGS_GROUP).fireResistant());
    }

    @Override
    public void tick(ItemStack stack, SlotReference reference) {
        if (!(reference.entity() instanceof ServerPlayer player)) return;

        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 5, 0, true, false, true));
        if (player.isOnFire()) player.setRemainingFireTicks(0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, HumanoidModel<M> model, PoseStack matrices) {
        AccessoryRenderer.transformToModelPart(matrices, model.body, 0, 0.65, 1);
        matrices.scale(.5f, .5f, .5f);
        matrices.translate(0, 0, 0.025);
    }
}
