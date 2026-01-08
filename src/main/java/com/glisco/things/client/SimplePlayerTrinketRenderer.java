package com.glisco.things.client;

import com.glisco.things.Things;
import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.client.SimpleAccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@FunctionalInterface
public interface SimplePlayerTrinketRenderer extends SimpleAccessoryRenderer {

    @OnlyIn(Dist.CLIENT)
    @Override
    default <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, PoseStack matrices, EntityModel<M> model, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!Things.CONFIG.renderTrinkets() || !(model instanceof HumanoidModel<M>)) return;

        SimpleAccessoryRenderer.super.render(stack, reference, matrices, model, multiBufferSource, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    default <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, EntityModel<M> model, PoseStack matrices) {
        align(stack, reference, (HumanoidModel<M>) model, matrices);
    }

    @OnlyIn(Dist.CLIENT)
    <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, HumanoidModel<M> model, PoseStack matrices);
}
