package com.glisco.things.misc;

import com.glisco.things.Things;
import com.glisco.things.items.ThingsItems;
import com.glisco.things.items.trinkets.AgglomerationItem;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoryNest;
import io.wispforest.accessories.api.slot.SlotType;
import java.util.Collection;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class AgglomerateRecipe extends CustomRecipe {
    public AgglomerateRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        int totalItems = 0;
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).isEmpty()) continue;
            totalItems++;
        }
        if (totalItems != 3) return false;

        if (!matchOnce(input, stack -> stack.is(ThingsItems.EMPTY_AGGLOMERATION))) return false;

        ItemStack firstStack = matchOne(input, AgglomerateRecipe::isValidItem);
        if (firstStack == null) return false;

        var firstValidSlots = AccessoriesAPI.getStackSlotTypes(world, firstStack);

        return matchOnce(input, stack -> {
            boolean anyCompatibleSlot = false;

            for (var slotType : AccessoriesAPI.getStackSlotTypes(world, stack)) {
                if(firstValidSlots.contains(slotType)) {
                    anyCompatibleSlot = true;

                    break;
                }
            }

            return anyCompatibleSlot && !ItemStack.isSameItem(stack, firstStack) && isValidItem(stack);
        });
    }

    private static boolean isValidItem(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(ThingsItems.EMPTY_AGGLOMERATION)
                && !(AccessoriesAPI.getAccessory(stack.getItem()) instanceof AccessoryNest)
                && !stack.is(Things.AGGLOMERATION_BLACKLIST);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack firstTrinket = matchOne(input, stack -> !stack.isEmpty() && !stack.is(ThingsItems.EMPTY_AGGLOMERATION));
        ItemStack secondTrinket = matchOne(input, stack -> !stack.isEmpty() && stack != firstTrinket && !stack.is(ThingsItems.EMPTY_AGGLOMERATION));

        return AgglomerationItem.createStack(firstTrinket, secondTrinket);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
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

    private static ItemStack matchOne(CraftingInput input, Predicate<ItemStack> condition) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (!condition.test(stack)) continue;

            return stack;
        }

        return null;
    }

    public static class Serializer extends SimpleCraftingRecipeSerializer<AgglomerateRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
            super(AgglomerateRecipe::new);
        }
    }
}
