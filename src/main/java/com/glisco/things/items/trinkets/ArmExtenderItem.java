package com.glisco.things.items.trinkets;

import com.glisco.things.Things;
import com.glisco.things.items.TrinketItemWithOptionalTooltip;
import io.wispforest.accessories.api.components.AccessoriesDataComponents;
import io.wispforest.accessories.api.components.AccessoryItemAttributeModifiers;
import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

public class ArmExtenderItem extends TrinketItemWithOptionalTooltip {

    public ArmExtenderItem() {
        super(((OwoItemSettingsExtension) new Item.Properties().stacksTo(1)).group(() -> Things.THINGS_GROUP)
                .component(
                        AccessoriesDataComponents.ATTRIBUTES,
                        AccessoryItemAttributeModifiers.builder()
                                .addForSlot(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(Things.id("arm_extender"), 2d, AttributeModifier.Operation.ADD_VALUE), "hand", false)
                                .addForSlot(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Things.id("arm_extender"), 2d, AttributeModifier.Operation.ADD_VALUE), "hand", false)
                                .build())
        );
    }
}
