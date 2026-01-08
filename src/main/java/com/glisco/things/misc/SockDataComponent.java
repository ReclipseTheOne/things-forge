package com.glisco.things.misc;

import com.glisco.things.Things;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Stores sock-related data for players using NeoForge's attachment system.
 */
public class SockDataComponent implements INBTSerializable<CompoundTag> {
    private Player bearer;

    public boolean jumpySocksEquipped = false;

    private float speedModification = 0;
    private final Int2IntMap sockSpeeds = new Int2IntOpenHashMap();

    public SockDataComponent() {
        // Default constructor for attachment creation
    }

    public SockDataComponent(Player bearer) {
        this.bearer = bearer;
    }

    public void setBearer(Player bearer) {
        this.bearer = bearer;
    }

    public void updateSockSpeed(int slot, int speed) {
        if (this.sockSpeeds.get(slot) == speed) return;

        this.modifySpeed(-Things.CONFIG.sockPerLevelSpeedAmplifier() * this.sockSpeeds.get(slot));

        this.sockSpeeds.put(slot, speed);
        this.modifySpeed(Things.CONFIG.sockPerLevelSpeedAmplifier() * speed);
    }

    public void modifySpeed(float amount) {
        if (amount == 0 || bearer == null) return;

        float cleanWalkSpeed = bearer.getAbilities().getWalkingSpeed() - speedModification;
        speedModification += amount;
        if (speedModification < 0) speedModification = 0;

        final var modifiedSpeed = cleanWalkSpeed + speedModification;

        bearer.getAbilities().setWalkingSpeed(modifiedSpeed);
        bearer.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(modifiedSpeed);
        bearer.onUpdateAbilities();
    }

    public void setModifier(float target) {
        modifySpeed(target - speedModification);
    }

    public void clearSockSpeed(int slot) {
        this.sockSpeeds.remove(slot);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("SpeedModification", speedModification);

        var list = new ListTag();
        this.sockSpeeds.forEach((slot, speed) -> {
            var nbt = new CompoundTag();
            nbt.putInt("Slot", slot);
            nbt.putInt("Speed", speed);
            list.add(nbt);
        });
        tag.put("SockSpeeds", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.speedModification = tag.getFloat("SpeedModification");

        this.sockSpeeds.clear();
        tag.getList("SockSpeeds", Tag.TAG_COMPOUND).forEach(element -> {
            var nbt = (CompoundTag) element;
            this.sockSpeeds.put(nbt.getInt("Slot"), nbt.getInt("Speed"));
        });
    }
}
