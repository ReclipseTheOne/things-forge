package com.glisco.things.items;

import com.glisco.things.Things;
import com.glisco.things.items.generic.*;
import com.glisco.things.items.trinkets.*;
import com.glisco.things.mixin.access.ItemAccessor;
import io.wispforest.accessories.Accessories;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.registration.annotations.IterationIgnored;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import io.wispforest.owo.util.TagInjector;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

@SuppressWarnings("unused")
public class ThingsItems implements ItemRegistryContainer {

//    @IterationIgnored
//    public static final Item THINGS_ALMANAC = LavenderBookItem.registerForBook(Things.id("almanac"), Things.id("things_almanac"), ((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(1));

    public static final Item RECALL_POTION = new RecallPotionItem();
    public static final Item CONTAINER_KEY = new ContainerKeyItem();
    public static final Item BATER_WUCKET = new BaterWucketItem();
    public static final Item BEMPTY_UCKET = new BemptyUcketItem();
    public static final Item ENDER_POUCH = new EnderPouchItem();
    public static final Item MONOCLE = new MonocleItem();
    public static final Item MOSS_NECKLACE = new MossNecklaceItem();
    public static final Item PLACEBO = new PlaceboItem();
    public static final Item DISPLACEMENT_TOME = new DisplacementTomeItem();
    public static final Item DISPLACEMENT_PAGE = new Item(((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(8));
    public static final Item MINING_GLOVES = new MiningGlovesItem();
    public static final Item RIOT_GAUNTLET = new RiotGauntletItem();
    public static final Item INFERNAL_SCEPTER = new InfernalScepterItem();
    public static final Item HADES_CRYSTAL = new HadesCrystalItem();
    public static final Item ENCHANTED_WAX_GLAND = new EnchantedWaxGlandItem();
    public static final Item ITEM_MAGNET = new ItemMagnetItem();
    public static final Item RABBIT_FOOT_CHARM = new RabbitFootCharmItem();
    public static final Item LUCK_OF_THE_IRISH = new LuckOfTheIrishItem();
    public static final Item HARDENING_CATALYST = new HardeningCatalystItem();
    public static final Item SOCKS = new SocksItem();
    public static final Item ARM_EXTENDER = new ArmExtenderItem();
    public static final Item SHOCK_ABSORBER = new ShockAbsorberItem();
    public static final Item BROKEN_WATCH = new BrokenWatchItem();

    public static final Item EMPTY_AGGLOMERATION = new EmptyAgglomerationItem();
    public static final Item AGGLOMERATION = new AgglomerationItem();

    public static final Item GLEAMING_POWDER = new GleamingItem();
    public static final Item GLEAMING_COMPOUND = new GleamingItem();

    @Override
    public void afterFieldProcessing() {
        if (Things.CONFIG.appleTrinket()) {
	        AccessoriesAPI.registerAccessory(Items.APPLE, new AppleTrinket());
            TagInjector.inject(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Accessories.MODID, "face"), Items.APPLE);
        }

        BaterWucketItem.registerCauldronBehavior();
        BemptyUcketItem.registerCauldronBehavior();
        ((ItemAccessor) BATER_WUCKET).things$setRecipeRemainder(BATER_WUCKET);
        ((ItemAccessor) Items.POTION).things$setRecipeRemainder(Items.GLASS_BOTTLE);
    }

    private static final class GleamingItem extends Item {
        public GleamingItem() {
            super(((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).rarity(Rarity.UNCOMMON));
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
            tooltip.add(TextOps.translateWithColor("text.things.crafting_component", TextOps.color(ChatFormatting.GRAY)));
        }
    }

    private static final class HardeningCatalystItem extends ItemWithExtendableTooltip {
        public HardeningCatalystItem() {
            super(((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant());
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }
    }

    private static final class EmptyAgglomerationItem extends ItemWithExtendableTooltip {
        public EmptyAgglomerationItem() {
            super(((OwoItemSettingsExtension) new Item.Properties()).group(() -> Things.THINGS_GROUP).stacksTo(1).rarity(Rarity.UNCOMMON));
        }
    }
}
