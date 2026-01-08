package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.client.SimplePlayerTrinketRenderer;
import com.glisco.things.items.TrinketItemWithOptionalTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.client.AccessoryRenderer;
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
import net.minecraft.world.level.LightLayer;

public class MossNecklaceItem extends TrinketItemWithOptionalTooltip implements SimplePlayerTrinketRenderer {

    public MossNecklaceItem() {
        super(((OwoItemSettingsExtension) new Item.Properties().stacksTo(1)).group(() -> Things.THINGS_GROUP));
    }

    @Override
    public void tick(ItemStack stack, SlotReference reference) {
        if (!(reference.entity() instanceof ServerPlayer player)) return;

        int daytime = (int) player.level().getDayTime() % 24000;
        if (player.level().getBrightness(LightLayer.BLOCK, player.blockPosition()) > 7 ||
                (player.level().getBrightness(LightLayer.SKY, player.blockPosition()) > 7 && (daytime > 23500 || daytime < 12500))) {

            if (player.getEffect(MobEffects.REGENERATION) != null
                    && player.getEffect(MobEffects.REGENERATION).getDuration() > 10) return;

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 610,
                    Things.CONFIG.effectLevels.mossNecklaceRegen() - 1, true, false, true));
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference reference) {
        if (!(reference.entity() instanceof ServerPlayer player)) return;

        if (player.hasEffect(MobEffects.REGENERATION))
            player.removeEffect(MobEffects.REGENERATION);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, HumanoidModel<M> model, PoseStack matrices) {
        AccessoryRenderer.transformToModelPart(matrices, model.body, 0, 0.7, 1);
        matrices.scale(.5f, .5f, .5f);
        matrices.translate(0, 0, 0.025);
    }
}
