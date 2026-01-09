package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.client.SimplePlayerTrinketRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.model.HumanoidModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AppleTrinket implements Accessory {

    @Override
    public void tick(ItemStack stack, SlotReference reference) {
        if (!(reference.entity() instanceof ServerPlayer player)) return;

        if (player.getFoodData().getFoodLevel() > 16) return;

        var capability = AccessoriesCapability.get(player);

        if (capability == null || !capability.isEquipped(Items.APPLE)) return;

        player.getFoodData().eat(Items.APPLE.components().get(DataComponents.FOOD));
        stack.shrink(1);

        player.playSound(SoundEvents.PLAYER_BURP, 1, 1);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer implements SimplePlayerTrinketRenderer {
        @Override
        public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, PoseStack matrices, EntityModel<M> model, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!Things.CONFIG.renderAppleTrinket()) return;

            var stackCount = stack.getCount();

            matrices.pushPose();

            for (int i = 0; i < stackCount; i++) {
                matrices.pushPose();

                align(stack, reference, model, matrices);

                //matrices.translate(0, 0, i * 0.025);
                matrices.translate(0, 0, i * (1f/16f));

                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, matrices, multiBufferSource, reference.entity().level(), 0);

                matrices.popPose();
            }

            matrices.popPose();

        }

        @Override
        public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, HumanoidModel<M> model, PoseStack matrices) {
	        AccessoryRenderer.transformToModelPart(matrices, model.head,0,0,1);
	        matrices.translate(0, 0, .03);
        }
    }
}
