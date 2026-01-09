package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.mixin.ItemUsageContextAccessor;
import com.glisco.things.text.AgglomerationTooltipData;
import com.glisco.things.text.TooltipComponentText;
import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.Accessories;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoryItem;
import io.wispforest.accessories.api.AccessoryNest;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.components.AccessoriesDataComponents;
import io.wispforest.accessories.api.components.AccessoryNestContainerContents;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.api.slot.SlotType;
import io.wispforest.accessories.data.SlotTypeLoader;
import io.wispforest.accessories.impl.AccessoryNestUtils;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.network.ServerAccess;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class AgglomerationItem extends AccessoryItem implements AccessoryNest, AccessoryRenderer {

    public AgglomerationItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    //--------

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        return getStackAndRun(stack, player, innerStack -> {
            return innerStack.overrideOtherStackedOnMe(ItemStack.EMPTY, slot, clickType, player, cursorStackReference);
        }, () -> false);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return getStackAndRun(context.getItemInHand(), context.getPlayer(), innerStack -> {
            return innerStack.useOn(new UseOnContext(context.getLevel(), context.getPlayer(), context.getHand(), innerStack, ((ItemUsageContextAccessor)context).things$getHitResult()));
        }, () -> InteractionResult.FAIL);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        return getStackAndRun(stack, user instanceof Player player ? player : null, innerStack -> {
            return innerStack.finishUsingItem(world, user);
        }, () -> stack);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
        return getStackAndRun(stack, player, innerStack -> innerStack.overrideStackedOnOther(slot, clickType, player), () -> false);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return getStackAndRun(stack, attacker instanceof Player player ? player : null, innerStack -> {
            innerStack.hurtEnemy(target, ((Player) attacker));

            return true;
        }, () -> false);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        return getStackAndRun(stack, miner instanceof Player player ? player : null, innerStack -> {
            innerStack.mineBlock(world, state, pos, ((Player) miner));

            return true;
        }, () -> false);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        return getStackAndRun(stack, user, innerStack -> innerStack.interactLivingEntity(user, entity, hand), () -> InteractionResult.FAIL);
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return getStackAndRun(stack, null, ItemStack::useOnRelease, () -> false);
    }

    public <T> T getStackAndRun(ItemStack stack, Player player, Function<ItemStack, T> methodPassthru, Supplier<T> error){
        var value = AccessoryNest.attemptFunction(stack, player, map -> {
            int index = 0;

            var selectedTrinket = getSelectedIndex(stack);

            if(selectedTrinket >= map.size()) return error.get();

            for (var entry : map.entrySet()) {
                if(index == selectedTrinket) return methodPassthru.apply(entry.getKey());

                index++;
            }

            return null;
        }, null);

        return value != null ? value : error.get();
    }

    //--------

    public static void scrollSelectedStack(ItemStack stack){
        stack.update(SelectedStackComponent.COMPONENT_TYPE, SelectedStackComponent.DEFAULT, component -> {
            return new SelectedStackComponent(component.index() == 0 ? 1 : 0);
        });
    }

    public static int getSelectedIndex(ItemStack stack) {
        return stack.getOrDefault(SelectedStackComponent.COMPONENT_TYPE, SelectedStackComponent.DEFAULT).index();
    }

    public static ItemStack createStack(ItemStack... items) {
        var stack = new ItemStack(ThingsItems.AGGLOMERATION, 1);

        stack.set(AccessoriesDataComponents.NESTED_ACCESSORIES, new AccessoryNestContainerContents(Arrays.stream(items).map(ItemStack::copy).toList()));
        stack.set(SelectedStackComponent.COMPONENT_TYPE, new SelectedStackComponent((byte) 0));

        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        var data = AccessoryNestUtils.getData(user.getItemInHand(hand));

        if(data != null) {
            for (var stack : data.accessories()) {
                if (stack.isEmpty()) {
                    var cake = new ItemStack(Items.CAKE);
                    cake.set(DataComponents.ITEM_NAME, Component.translatable("item.things.consolation_cake"));

                    user.getInventory().placeItemBackInInventory(cake);
                    return InteractionResultHolder.success(ItemStack.EMPTY);
                }
            }
        }

        return super.use(world, user, hand);
    }

    //--

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot) {
        var isInnerStacksValid = AccessoryNest.attemptFunction(stack, slot, map -> {
            for (var entryRef : map.keySet()) {
                if(!AccessoriesAPI.canInsertIntoSlot(entryRef.stack(), entryRef.reference())) return false;
            }

            return true;
        }, false);

        var slotType = SlotTypeLoader.getSlotType(slot.entity(), slot.slotName());

        var validators = slotType.validators();

        if(!isInnerStacksValid && validators.contains(Accessories.of("component"))) {
            var state = AccessoriesAPI.getPredicate(Accessories.of("component"))
                    .isValid(slot.entity().level(), slotType, slot.slot(), stack);

            if(state == TriState.TRUE) return true;
        }

        return isInnerStacksValid;
    }

    @Override
    public boolean canUnequip(ItemStack stack, SlotReference reference) {
        if(AccessoryNestUtils.getData(stack) == null) return true;

        return AccessoryNest.super.canUnequip(stack, reference);
    }

    @Override
    public void getExtraTooltip(ItemStack stack, List<Component> tooltips, TooltipContext tooltipContext, TooltipFlag tooltipType) {
        var data = AccessoryNestUtils.getData(stack);

        if(data == null) return;

        var subStacks = data.accessories();

        var innerTooltipData = new ArrayList<Component>();

        for (int i = 0; i < subStacks.size(); i++) {
            var subStack = subStacks.get(i);

            var subTooltip = subStacks.get(i).getTooltipLines(tooltipContext,null, tooltipType);

            for (int j = 0; j < subTooltip.size(); j++) {
                if (j == 0) {
                    var text = new TooltipComponentText(new AgglomerationTooltipData(Component.literal(getSelectedIndex(stack) == i ? "> " : "• "), subStack, subTooltip.get(j)));

                    innerTooltipData.add(text);
                } else {
                    innerTooltipData.add(Component.literal("  ").append(subTooltip.get(j)));
                }
            }
        }

        for (var subStack : subStacks) {
            if (!subStack.isEmpty()) continue;
            innerTooltipData.add(Component.empty());
            innerTooltipData.add(Component.translatable("item.things.consolation_cake.hint"));
        }

        tooltips.addAll(0, innerTooltipData);
    }

    @Override
    public void onStackChanges(ItemStack holderStack, AccessoryNestContainerContents data, @Nullable LivingEntity livingEntity) {
        for (var accessory : data.accessories()) {
            if (accessory.is(Items.AIR) && livingEntity instanceof ServerPlayer player) {
                Things.AN_AMAZINGLY_EXPENSIVE_MISTAKE_CRITERION.get().trigger(player);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, PoseStack matrices, EntityModel<M> model, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!Things.CONFIG.renderAgglomerationTrinket()) return;

        AccessoryNest.attemptConsumer(stack, reference, map -> {
            map.forEach((slotEntryReference, accessory) -> {
                var subStack = slotEntryReference.stack();
                var renderer = AccessoriesRendererRegistry.getRender(subStack);

                if (renderer != null) {
                    matrices.pushPose();
                    renderer.render(subStack, reference, matrices, model, multiBufferSource, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
                    matrices.popPose();
                }
            });
        });
    }

    //--

    public record ScrollHandStackTrinket(boolean mainHandStack){
        public static void scrollItemStack(ScrollHandStackTrinket message, ServerAccess access){
            var stack = message.mainHandStack ? access.player().getMainHandItem() : access.player().getOffhandItem();

            AgglomerationItem.scrollSelectedStack(stack);

            var data = AccessoryNestUtils.getData(stack);

            access.player().sendSystemMessage(Component.literal("> ")
                    .append(Component.translatable(data.accessories().get(getSelectedIndex(stack)).getDescriptionId())), true);
        }
    }

    public record ScrollStackFromSlotTrinket(boolean fromPlayerInv, int slotId){
        public static void scrollItemStack(ScrollStackFromSlotTrinket message, ServerAccess access){
            var stack = message.fromPlayerInv
                    ? access.player().getInventory().getItem(message.slotId)
                    : access.player().containerMenu.getSlot(message.slotId).getItem();

            if(stack == null) return;

            AgglomerationItem.scrollSelectedStack(stack);
        }
    }

    public record SelectedStackComponent(int index) {
        public static final SelectedStackComponent DEFAULT = new SelectedStackComponent(0);

        public static final Endec<SelectedStackComponent> ENDEC = StructEndecBuilder.of(
                Endec.ifAttr(SerializationAttributes.HUMAN_READABLE, Endec.INT).orElse(Endec.VAR_INT).fieldOf("index", SelectedStackComponent::index),
                SelectedStackComponent::new
        );

        public static final DataComponentType<SelectedStackComponent> COMPONENT_TYPE = DataComponentType.<SelectedStackComponent>builder()
                .persistent(CodecUtils.toCodec(ENDEC))
                .networkSynchronized(CodecUtils.toPacketCodec(ENDEC))
                .cacheEncoding()
                .build();
    }
}
