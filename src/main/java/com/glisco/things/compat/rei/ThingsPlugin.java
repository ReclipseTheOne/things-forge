package com.glisco.things.compat.rei;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.items.trinkets.SocksItem;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

import java.util.List;
import java.util.Optional;

public class ThingsPlugin implements REIClientPlugin {

    public static final CategoryIdentifier<BrokenWatchDisplay> BROKEN_WATCH_CATEGORY = CategoryIdentifier.of(Things.id("broken_watch"));

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new BrokenWatchCategory());
        registry.addWorkstations(BROKEN_WATCH_CATEGORY, EntryStacks.of(Blocks.PISTON));
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerDisplays(DisplayRegistry registry) {
        final var awkwardPotion = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        final var swiftnessPotion = PotionContents.createItemStack(Items.POTION, Potions.STRONG_SWIFTNESS);
        registry.add(new BrewingRecipe(Ingredient.of(awkwardPotion), Ingredient.of(Items.ENDER_PEARL), ThingsItems.RECALL_POTION.getDefaultInstance()));

        final var averageSocks = SocksItem.create(0, false);

        registry.add(new SockDisplay(averageSocks, new ItemStack(ThingsItems.RABBIT_FOOT_CHARM), SocksItem.create(0, true), true));
        registry.add(new SockDisplay(averageSocks, swiftnessPotion, SocksItem.create(1, false), false));
        registry.add(new SockDisplay(SocksItem.create(1, false), swiftnessPotion, SocksItem.create(2, false), false));
        registry.add(new BrokenWatchDisplay());
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        registry.removeEntry(EntryStacks.of(ThingsItems.AGGLOMERATION));
        if (!Things.CONFIG.enableAgglomeration()) registry.removeEntry(EntryStacks.of(ThingsItems.EMPTY_AGGLOMERATION));
    }

    private record SockDisplay(ItemStack socc, ItemStack addition, ItemStack result, boolean compound) implements Display {
        public int getWidth() {
            return 2;
        }

        public int getHeight() {
            return 2;
        }

	    public int displaySize() {
		    return 2;
	    }

	    @Override
	    public List<EntryIngredient> getInputEntries() {
		    return List.of(EntryIngredients.of(socc),
				    EntryIngredients.of(compound ? ThingsItems.GLEAMING_COMPOUND : ThingsItems.GLEAMING_POWDER),
				    EntryIngredients.of(addition));
	    }

	    @Override
	    public List<EntryIngredient> getOutputEntries() {
		    return List.of(EntryIngredients.of(result));
	    }

	    @Override
	    public CategoryIdentifier<?> getCategoryIdentifier() {
		    return BROKEN_WATCH_CATEGORY;
	    }
    }
}
