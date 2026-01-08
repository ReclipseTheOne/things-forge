package com.glisco.things.items.generic;

import com.glisco.things.Things;
import com.glisco.things.ThingsNetwork;
import com.glisco.things.items.ItemWithExtendableTooltip;
import com.glisco.things.misc.DisplacementTomeScreenHandler;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DisplacementTomeItem extends ItemWithExtendableTooltip {

    private static final Endec<ImmutableMap<String, Target>> TARGETS_ENDEC = Target.ENDEC
            .mapOf()
            .xmap(ImmutableMap::copyOf, map -> map);

    public static final DataComponentType<Integer> FUEL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Things.id("displacement_tome_fuel"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    public static final DataComponentType<ImmutableMap<String, Target>> TARGETS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Things.id("displacement_tome_targets"),
            DataComponentType.<ImmutableMap<String, Target>>builder()
                    .persistent(CodecUtils.toCodec(TARGETS_ENDEC))
                    .networkSynchronized(CodecUtils.toPacketCodec(TARGETS_ENDEC))
                    .build()
    );

    public DisplacementTomeItem() {
        super(((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(1)
                .component(TARGETS, ImmutableMap.of())
                .component(FUEL, 0));
    }

    public static void storeTeleportTargetInBook(ItemStack stack, Target target, String name, boolean replaceIfExisting) {
        var targets = new HashMap<>(stack.get(TARGETS));

        if (targets.containsKey(name) && !replaceIfExisting) {
            throw new IllegalStateException("Teleport point '" + name + "' already exists and replaceIfExisting was not set");
        }

        targets.put(name, target);
        stack.set(TARGETS, ImmutableMap.copyOf(targets));
    }

    public static void addFuel(ItemStack stack, int fuel) {
        stack.update(FUEL, 0, f -> f + fuel);
    }

    public static boolean deletePoint(ItemStack stack, String name) {
        Map<String, Target> targets = stack.get(TARGETS);
        if (!targets.containsKey(name)) return false;

        targets = new HashMap<>(targets);
        targets.remove(name);

        stack.set(TARGETS, ImmutableMap.copyOf(targets));
        return true;
    }

    public static boolean rename(ItemStack stack, String data) {
        var name = data.split(":")[0];
        var newName = data.split(":")[1];

        Map<String, Target> targets = stack.get(TARGETS);
        if (!targets.containsKey(name)) return false;

        targets = new HashMap<>(targets);

        targets.put(newName, targets.get(name));
        targets.remove(name);

        stack.set(TARGETS, ImmutableMap.copyOf(targets));
        return true;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
        if (clickType != ClickAction.SECONDARY) return false;

        var slotStack = slot.getItem();
        if (!slotStack.is(Things.DISPLACEMENT_TOME_FUELS)) return false;

        addFuel(stack, slotStack.getCount());
        slot.setByPlayer(ItemStack.EMPTY);

        player.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE, .5f, 2f);

        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        user.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) ->
                new DisplacementTomeScreenHandler(i, playerInventory, user.getItemInHand(hand)), Component.literal("help")));

        if (user instanceof ServerPlayer) {
            ThingsNetwork.CHANNEL.serverHandle(user).send(new DisplacementTomeScreenHandler.UpdateClientPacket(user.getItemInHand(hand)));
        } else {
            user.playSound(SoundEvents.BOOK_PAGE_TURN, 1, 1);
        }

        return InteractionResultHolder.success(user.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.things.displacement_tome.tooltip.charges", stack.get(FUEL)));
        super.appendHoverText(stack, context, tooltip, type);
    }

    public record Target(BlockPos pos, ResourceKey<Level> world, float headYaw, float headPitch) {

        public static final Endec<Target> ENDEC = StructEndecBuilder.of(
                Endec.LONG.xmap(BlockPos::of, BlockPos::asLong).fieldOf("Pos", Target::pos),
                MinecraftEndecs.IDENTIFIER.xmap(identifier -> ResourceKey.create(Registries.DIMENSION, identifier), ResourceKey::location).fieldOf("World", Target::world),
                Endec.FLOAT.fieldOf("HeadYaw", Target::headYaw),
                Endec.FLOAT.fieldOf("HeadPitch", Target::headPitch),
                Target::new
        );

        public void teleportPlayer(ServerPlayer player) {
            WorldOps.teleportToWorld(player, player.getServer().getLevel(this.world), Vec3.atCenterOf(this.pos), this.headYaw, this.headPitch);
        }

        public static Target fromPlayer(ServerPlayer player) {
            return new Target(player.blockPosition(), player.level().dimension(), player.yHeadRot, player.getXRot());
        }
    }

    public static class PredicateProvider implements ClampedItemPropertyFunction {
        @Override
        public float call(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            int size = stack.get(TARGETS).size();
            if (size == 0) {
                return 0;
            } else if (size < 4) {
                return 1;
            } else {
                return 2;
            }
        }

        @Override
        public float unclampedCall(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            throw new AssertionError("respectfully, get fucked");
        }
    }
}
