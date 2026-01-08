package com.glisco.things.misc;

import com.mojang.serialization.Codec;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class AnAmazinglyExpensiveMistakeCriterion extends SimpleCriterionTrigger<AnAmazinglyExpensiveMistakeCriterion.Conditions> {

    public void trigger(ServerPlayer player) {
        this.trigger(player, conditions -> true);
    }

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public static class Conditions implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Conditions> CODEC = CodecUtils.toCodec(StructEndecBuilder.of(
                CodecUtils.toEndec(EntityPredicate.ADVANCEMENT_CODEC).optionalFieldOf("player", c -> c.playerPredicate, (ContextAwarePredicate) null),
                Conditions::new
        ), SerializationContext.attributes(SerializationAttributes.HUMAN_READABLE));

        private final ContextAwarePredicate playerPredicate;

        public Conditions(ContextAwarePredicate playerPredicate) {
            this.playerPredicate = playerPredicate;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return Optional.ofNullable(this.playerPredicate);
        }
    }
}
