package com.glisco.things.client;

import com.glisco.things.blocks.PlacedItemBlockEntity;
import com.glisco.things.blocks.ThingsBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PlacedItemBlockEntityRenderer implements BlockEntityRenderer<PlacedItemBlockEntity> {

    public PlacedItemBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(PlacedItemBlockEntity entity, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light, int overlay) {
        ItemStack item = entity.getItem();
        BakedModel itemModel = Minecraft.getInstance().getItemRenderer().getModel(item, null, null, 0);

        float scaleFactor = item.getItem() instanceof BlockItem ? 0.5f : 0.4f;

        if (!entity.getLevel().getBlockState(entity.getBlockPos()).is(ThingsBlocks.PLACED_ITEM)) return;

        matrixStack.pushPose();
        switch (entity.getLevel().getBlockState(entity.getBlockPos()).getValue(BlockStateProperties.FACING)) {
            case UP -> {
                matrixStack.translate(0.5, 0.97, 0.5);
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
                matrixStack.mulPose(Axis.YN.rotationDegrees(entity.getRotation() * 45));
            }
            case DOWN -> {
                matrixStack.translate(0.5, 0.03, 0.5);
                matrixStack.mulPose(Axis.YN.rotationDegrees(entity.getRotation() * 45));
            }
            case EAST -> {
                matrixStack.translate(0.97, 0.5, 0.5);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90 - entity.getRotation() * 45));
                matrixStack.mulPose(Axis.ZP.rotationDegrees(90));
            }
            case WEST -> {
                matrixStack.translate(0.03, 0.5, 0.5);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90 + entity.getRotation() * 45));
                matrixStack.mulPose(Axis.ZN.rotationDegrees(90));
            }
            case NORTH -> {
                matrixStack.translate(0.5, 0.5, 0.03);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90));
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
                matrixStack.mulPose(Axis.YP.rotationDegrees(-entity.getRotation() * 45));
            }
            case SOUTH -> {
                matrixStack.translate(0.5, 0.5, 0.97);
                matrixStack.mulPose(Axis.XN.rotationDegrees(90));
                matrixStack.mulPose(Axis.YP.rotationDegrees(-entity.getRotation() * 45));
            }
        }
        matrixStack.scale(scaleFactor, scaleFactor, scaleFactor);
        matrixStack.mulPose(Axis.XP.rotationDegrees(90f));
        Minecraft.getInstance().getItemRenderer().render(item, ItemDisplayContext.FIXED, false, matrixStack, vertexConsumers, light, OverlayTexture.NO_OVERLAY, itemModel);
        matrixStack.popPose();
    }
}
