package com.glisco.things.client;

import com.glisco.things.Things;
import com.glisco.things.ThingsNetwork;
import com.glisco.things.blocks.ThingsBlocks;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.items.generic.DisplacementTomeItem;
import com.glisco.things.items.trinkets.AgglomerationItem;
import com.glisco.things.items.trinkets.AppleTrinket;
import com.glisco.things.items.trinkets.SocksItem;
import com.glisco.things.mixin.client.access.CreativeSlotAccessor;
import com.glisco.things.mixin.client.access.HandledScreenAccessor;
import com.glisco.things.text.AgglomerationTooltipComponent;
import com.glisco.things.text.AgglomerationTooltipData;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.components.AccessoriesDataComponents;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Client-side initialization for the Things mod on NeoForge.
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Things.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ThingsClient {

    public static final String THINGS_CATEGORY = "category." + Things.MOD_ID + "." + Things.MOD_ID;

    public static KeyMapping PLACE_ITEM;
    public static KeyMapping OPEN_ENDER_CHEST;
    public static KeyMapping TOGGLE_SOCKS_JUMP_BOOST;

	public ThingsClient(IEventBus eventBus, ModContainer modContainer) {
		eventBus.addListener(this::registerMenus);
	}

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        PLACE_ITEM = new KeyMapping(keybindId("place_item"), GLFW.GLFW_KEY_J, THINGS_CATEGORY);
        OPEN_ENDER_CHEST = new KeyMapping(keybindId("openenderchest"), GLFW.GLFW_KEY_G, THINGS_CATEGORY);
        TOGGLE_SOCKS_JUMP_BOOST = new KeyMapping(keybindId("toggle_socks_jump_boost"), GLFW.GLFW_KEY_CAPS_LOCK, THINGS_CATEGORY);

        event.register(PLACE_ITEM);
        event.register(OPEN_ENDER_CHEST);
        event.register(TOGGLE_SOCKS_JUMP_BOOST);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ThingsBlocks.PLACED_ITEM_BLOCK_ENTITY, PlacedItemBlockEntityRenderer::new);

            ItemProperties.register(ThingsItems.DISPLACEMENT_TOME, ResourceLocation.parse("pages"), new DisplacementTomeItem.PredicateProvider());
            ItemProperties.register(ThingsItems.SOCKS, ResourceLocation.parse("jumpy"), (stack, world, entity, seed) -> stack.has(SocksItem.JUMPY_AND_ENABLED) ? 1 : 0);

            // Register Curios renderers
            // TODO: Register Curios renderers when porting to Curios API
            // For now, trinket rendering will need to be handled via Curios' system
        });

        // Register game event listeners
        NeoForge.EVENT_BUS.addListener(ThingsClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(ThingsClient::onScreenInit);
        NeoForge.EVENT_BUS.addListener(ThingsClient::onMouseScroll);
        NeoForge.EVENT_BUS.addListener(ThingsClient::onGatherTooltipComponents);
    }

	private void registerMenus(RegisterMenuScreensEvent event) {
		event.register(Things.DISPLACEMENT_TOME_SCREEN_HANDLER, DisplacementTomeScreen::new);
	}


	private static void onClientTick(ClientTickEvent.Post event) {
		Minecraft client = Minecraft.getInstance();
	    while (PLACE_ITEM.isDown()) {
		    if (!(client.hitResult instanceof BlockHitResult blockResult)) break;
		    ThingsNetwork.CHANNEL.clientHandle().send(new ThingsNetwork.PlaceItemPacket(blockResult));
	    }

	    while (OPEN_ENDER_CHEST.isDown()) {
		    var capability = AccessoriesCapability.get(client.player);

		    if (capability == null || !capability.isEquipped(ThingsItems.ENDER_POUCH)) break;
		    ThingsNetwork.CHANNEL.clientHandle().send(new ThingsNetwork.OpenEnderChestPacket());
	    }

	    while (TOGGLE_SOCKS_JUMP_BOOST.isDown()) {
		    var capability = AccessoriesCapability.get(client.player);

		    if (capability == null || !capability.isEquipped(ThingsItems.SOCKS)) break;
		    ThingsNetwork.CHANNEL.clientHandle().send(new ThingsNetwork.ToggleSocksJumpBoostPacket());
	    }
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        // Screen initialization handled in mouse scroll event
    }

    private static void onMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;
        if (!Things.CONFIG.enableAgglomerationInvScrollSelection()) return;

        var slot = ((HandledScreenAccessor) containerScreen).thing$getSlotAt(event.getMouseX(), event.getMouseY());
        if (slot == null) return;

        var slotStack = slot.getItem();
        int slotId = slot.index;

        // Handle Creative Mode inventory mismatch
        boolean fromPlayerInv = containerScreen instanceof CreativeModeInventoryScreen
                && slot.container instanceof Inventory
                && slot.getContainerSlot() < 9;

        if (slot instanceof CreativeSlotAccessor creativeSlot) {
            slotId = creativeSlot.things$getSlot().index;
        }

        // TODO: Update for Curios API data components
        if (slotStack.getItem() instanceof AgglomerationItem) {
            ThingsNetwork.CHANNEL.clientHandle().send(
                    new AgglomerationItem.ScrollStackFromSlotTrinket(fromPlayerInv, fromPlayerInv ? slot.getContainerSlot() : slotId)
            );
            event.setCanceled(true);
        }
    }

    private static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        // Custom tooltip components handled via item's getTooltipImage method
    }

    private static String keybindId(String name) {
        return "key." + Things.MOD_ID + "." + name;
    }
}
