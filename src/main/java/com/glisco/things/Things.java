package com.glisco.things;

import com.glisco.things.blocks.ThingsBlocks;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.items.trinkets.AgglomerationItem;
import com.glisco.things.misc.*;
import com.glisco.things.misc.ThingsConfig;
import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.arguments.FloatArgumentType;
import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import io.wispforest.owo.util.Maldenhagen;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod(Things.MOD_ID)
public class Things {

    public static final String MOD_ID = "things";

    public static final ThingsConfig CONFIG = ThingsConfig.createAndLoad();

    public static OwoItemGroup THINGS_GROUP;
    public static final ResourceKey<Enchantment> RETRIBUTION = ResourceKey.create(Registries.ENCHANTMENT, id("retribution"));


    // NeoForge Deferred Registers
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredRegister<CriterionTrigger<?>> CRITERIA = DeferredRegister.create(Registries.TRIGGER_TYPE, MOD_ID);

    public static final Supplier<AttachmentType<SockDataComponent>> SOCK_DATA = ATTACHMENT_TYPES.register(
            "sock_data",
            () -> AttachmentType.serializable(a -> {
				if (a instanceof Player player)
					return new SockDataComponent(player);
				else throw new RuntimeException("SockDataComponent can only be attached to players");
            }).build()
    );

    public static final DeferredHolder<MobEffect, MomentumStatusEffect> MOMENTUM = MOB_EFFECTS.register("momentum", MomentumStatusEffect::new);

    public static final Supplier<MenuType<DisplacementTomeScreenHandler>> DISPLACEMENT_TOME_SCREEN_HANDLER = MENUS.register(
            "displacement_tome",
            () -> new MenuType<>(DisplacementTomeScreenHandler::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final DeferredHolder<CriterionTrigger<?>, AnAmazinglyExpensiveMistakeCriterion> AN_AMAZINGLY_EXPENSIVE_MISTAKE_CRITERION =
            CRITERIA.register("an_amazingly_expensive_mistake", AnAmazinglyExpensiveMistakeCriterion::new);

    private static final ResourceKey<PlacedFeature> GLEAMING_ORE = ResourceKey.create(Registries.PLACED_FEATURE, id("ore_gleaming"));

    public static final TagKey<Item> HARDENING_CATALYST_BLACKLIST = TagKey.create(Registries.ITEM, id("hardening_catalyst_blacklist"));
    public static final TagKey<Item> AGGLOMERATION_BLACKLIST = TagKey.create(Registries.ITEM, id("agglomeration_blacklist"));
    public static final TagKey<Item> DISPLACEMENT_TOME_FUELS = TagKey.create(Registries.ITEM, id("displacement_tome_fuels"));
    public static final TagKey<Item> ENCHANTABLE_WITH_RETRIBUTION = TagKey.create(Registries.ITEM, id("enchantable/retribution"));

    private static Set<Item> BROKEN_WATCH_RECIPE;

    private static ParticleSystemController CONTROLLER;
    private static ParticleSystem<Void> TOGGLE_JUMP_BOOST_PARTICLES;

    public static ParticleSystem<Void> getToggleJumpBoostParticles() {
        if (CONTROLLER == null) {
            CONTROLLER = new ParticleSystemController(id("particles"));
            TOGGLE_JUMP_BOOST_PARTICLES = CONTROLLER.register(Void.class, (world, pos, data) -> {
                ClientParticles.setParticleCount(25);
                ClientParticles.spawnPrecise(ParticleTypes.WAX_OFF, world, pos.add(0, 1, 0), 1, 2, 1);
            });
        }
        return TOGGLE_JUMP_BOOST_PARTICLES;
    }

    public Things(IEventBus modEventBus, ModContainer modContainer) {
        // Register deferred registers
        ATTACHMENT_TYPES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        MENUS.register(modEventBus);
        CRITERIA.register(modEventBus);

        modEventBus.addListener(this::onRegister);

        // Register common setup event
        modEventBus.addListener(this::commonSetup);

        // Register game events
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegister(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            FieldRegistrationHandler.register(ThingsItems.class, MOD_ID, false);
        });

        event.register(Registries.BLOCK, helper -> {
            FieldRegistrationHandler.register(ThingsBlocks.class, MOD_ID, false);
        });

        event.register(Registries.RECIPE_TYPE, helper -> {
            Registry.register(BuiltInRegistries.RECIPE_TYPE, id("sock_upgrade_crafting"), SockUpgradeRecipe.Type.INSTANCE);
            Registry.register(BuiltInRegistries.RECIPE_TYPE, id("jumpy_sock_crafting"), JumpySocksRecipe.Type.INSTANCE);
        });

        event.register(Registries.RECIPE_SERIALIZER, helper -> {
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("sock_upgrade_crafting"), SockUpgradeRecipe.Serializer.INSTANCE);
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("jumpy_sock_crafting"), JumpySocksRecipe.Serializer.INSTANCE);
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("agglomerate"), AgglomerateRecipe.Serializer.INSTANCE);
        });

        event.register(Registries.DATA_COMPONENT_TYPE, helper -> {
            Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("agglomeration_selected_stack"), AgglomerationItem.SelectedStackComponent.COMPONENT_TYPE);
        });

        event.register(Registries.CREATIVE_MODE_TAB, helper -> {
            THINGS_GROUP = OwoItemGroup.builder(ResourceLocation.fromNamespaceAndPath("things", "things"), () -> Icon.of(ThingsItems.BATER_WUCKET)).build();
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (CONFIG.generateGleamingOre()) {
                Maldenhagen.injectCopium(ThingsBlocks.GLEAMING_ORE);
            }

            ThingsNetwork.init();
            THINGS_GROUP.initialize();
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        if (Owo.DEBUG) {
            event.getDispatcher().register(literal("things:set_walk_speed_modifier")
                    .then(argument("speed", FloatArgumentType.floatArg()).executes(context -> {
                        float speed = FloatArgumentType.getFloat(context, "speed");
                        context.getSource().getPlayer().getData(SOCK_DATA.get()).setModifier(speed);
                        return 0;
                    })));
        }
    }

    public static Set<Item> brokenWatchRecipe() {
        if (BROKEN_WATCH_RECIPE == null) {
            BROKEN_WATCH_RECIPE = ImmutableSet.of(Items.LEATHER, Items.CLOCK, ThingsItems.GLEAMING_COMPOUND);
        }
        return BROKEN_WATCH_RECIPE;
    }

    public static @Nullable Item recallPotionIngredient() {
        if (!CONFIG.enableRecallPotionRecipe()) return null;
        return BuiltInRegistries.ITEM.getOptional(CONFIG.recallPotionIngredient()).orElse(null);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Gets sock data from a living entity using NeoForge attachments.
     */
    public static SockDataComponent getSockData(LivingEntity entity) {
        return entity.getData(SOCK_DATA.get());
    }
}
