package galacticwars.clonewars.fabrication;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.world.item.crafting.Ingredient;

public record CountedIngredient(Ingredient ingredient, int count) {
    public static final Codec<CountedIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CountedIngredient::ingredient),
            Codec.intRange(1, 4_096).optionalFieldOf("count", 1).forGetter(CountedIngredient::count)
    ).apply(instance, CountedIngredient::new));

    public CountedIngredient {
        Objects.requireNonNull(ingredient, "ingredient");
        if (ingredient.isEmpty() || count < 1 || count > 4_096) {
            throw new IllegalArgumentException("Invalid counted ingredient");
        }
    }
}
