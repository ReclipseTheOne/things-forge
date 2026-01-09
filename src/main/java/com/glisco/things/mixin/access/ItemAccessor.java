package com.glisco.things.mixin.access;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {

    @Accessor("craftingRemainingItem")
    @Mutable
    void things$setRecipeRemainder(Item recipeRemainder);

}
