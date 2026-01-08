package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.client.SimplePlayerTrinketRenderer;
import com.glisco.things.items.TrinketItemWithOptionalTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LuckOfTheIrishItem extends TrinketItemWithOptionalTooltip implements SimplePlayerTrinketRenderer {

    public LuckOfTheIrishItem() {
        super(((OwoItemSettingsExtension) new Item.Properties().stacksTo(1)).group(() -> Things.THINGS_GROUP));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, HumanoidModel<M> model, PoseStack matrices) {
        AccessoryRenderer.transformToModelPart(matrices, model.body, -0.55, .65, 1);
        matrices.mulPose(Axis.YP.rotationDegrees(180));
        matrices.scale(.25f, .25f, .25f);
        matrices.translate(0, 0, -.055);
    }
}
