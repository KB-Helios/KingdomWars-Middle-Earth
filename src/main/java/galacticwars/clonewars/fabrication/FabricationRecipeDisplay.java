package galacticwars.clonewars.fabrication;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import galacticwars.clonewars.registry.ModRecipeTypes;
import java.util.List;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record FabricationRecipeDisplay(
        List<SlotDisplay> ingredients,
        SlotDisplay result,
        SlotDisplay craftingStation
) implements RecipeDisplay {
    public static final MapCodec<FabricationRecipeDisplay> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SlotDisplay.CODEC.listOf(1, 32).fieldOf("ingredients")
                            .forGetter(FabricationRecipeDisplay::ingredients),
                    SlotDisplay.CODEC.fieldOf("result").forGetter(FabricationRecipeDisplay::result),
                    SlotDisplay.CODEC.fieldOf("crafting_station")
                            .forGetter(FabricationRecipeDisplay::craftingStation)
            ).apply(instance, FabricationRecipeDisplay::new));

    public FabricationRecipeDisplay {
        ingredients = List.copyOf(ingredients);
    }

    @Override
    public RecipeDisplay.Type<FabricationRecipeDisplay> type() {
        return ModRecipeTypes.FABRICATION_DISPLAY.get();
    }
}
