package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.client.SimplePlayerTrinketRenderer;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.items.TrinketItemWithOptionalTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EnchantedWaxGlandItem extends TrinketItemWithOptionalTooltip implements SimplePlayerTrinketRenderer {

    public EnchantedWaxGlandItem() {
        super(((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void tick(ItemStack stack, SlotReference reference) {
        var entity = reference.entity();

        if (entity.isInWater()) {
            entity.push(0, 0.005, 0);
        } else if (entity.isInLava() && AccessoriesCapability.get(entity).isEquipped(ThingsItems.HADES_CRYSTAL)) {
            entity.push(0, 0.02, 0);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, HumanoidModel<M> model, PoseStack matrices) {
        AccessoryRenderer.transformToModelPart(matrices, model.body, 0,-0.6,-1);
        matrices.scale(.5f, .5f, .5f);
        matrices.translate(0, 0, -.04);
    }
}
