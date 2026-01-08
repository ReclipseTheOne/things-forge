package com.glisco.things.misc;

import com.glisco.things.items.ThingsItems;
import com.glisco.things.items.trinkets.SocksItem;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SockUpgradeRecipe extends CustomRecipe {

    public SockUpgradeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        if (!matchOnce(input, stack -> stack.is(ThingsItems.GLEAMING_POWDER))) return false;
        if (!matchOnce(input, stack -> stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.STRONG_SWIFTNESS))) return false;

        return matchOnce(input, stack -> stack.is(ThingsItems.SOCKS) && stack.getOrDefault(SocksItem.SPEED, 1) < 2);
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider lookup) {
        ItemStack socc = null;

        for (int i = 0; i < input.size(); i++) {
            final var stack = input.getItem(i);
            if (!stack.is(ThingsItems.SOCKS)) continue;

            socc = stack.copy();
            break;
        }

        if (socc == null) return ItemStack.EMPTY;
        socc.update(SocksItem.SPEED, 0, speed -> speed + 1);

        return socc;
    }

    private static boolean matchOnce(CraftingInput input, Predicate<ItemStack> condition) {
        boolean found = false;

        for (int i = 0; i < input.size(); i++) {
            if (!condition.test(input.getItem(i))) continue;
            if (found) return false;

            found = true;
        }

        return found;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width > 1 && height > 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Type implements RecipeType<SockUpgradeRecipe> {
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer extends SimpleCraftingRecipeSerializer<SockUpgradeRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
            super(SockUpgradeRecipe::new);
        }
    }
}
