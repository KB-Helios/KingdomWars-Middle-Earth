package galacticwars.clonewars.fabrication;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import galacticwars.clonewars.registry.ModItems;
import galacticwars.clonewars.registry.ModRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public record FabricationRecipe(
        List<CountedIngredient> ingredients,
        ItemStackTemplate result,
        String faction,
        String technology,
        boolean exportable,
        boolean showNotification
) implements Recipe<FabricationInput> {
    public static final MapCodec<FabricationRecipe> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    CountedIngredient.CODEC.listOf(1, 32).fieldOf("ingredients")
                            .forGetter(FabricationRecipe::ingredients),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(FabricationRecipe::result),
                    com.mojang.serialization.Codec.STRING.fieldOf("faction")
                            .forGetter(FabricationRecipe::faction),
                    com.mojang.serialization.Codec.STRING.fieldOf("technology")
                            .forGetter(FabricationRecipe::technology),
                    com.mojang.serialization.Codec.BOOL.optionalFieldOf("exportable", false)
                            .forGetter(FabricationRecipe::exportable),
                    com.mojang.serialization.Codec.BOOL.optionalFieldOf("show_notification", true)
                            .forGetter(FabricationRecipe::showNotification)
            ).apply(instance, FabricationRecipe::new));

    public FabricationRecipe {
        ingredients = List.copyOf(ingredients);
        Objects.requireNonNull(result, "result");
        if (faction == null || faction.isBlank() || technology == null || technology.isBlank()) {
            throw new IllegalArgumentException("Fabrication recipe access metadata cannot be blank");
        }
    }

    @Override
    public boolean matches(FabricationInput input, Level level) {
        return RecipeIngredientPlanner.removals(ingredients, input).length == input.size();
    }

    public int[] removals(FabricationInput input) {
        return RecipeIngredientPlanner.removals(ingredients, input);
    }

    @Override
    public ItemStack assemble(FabricationInput input) {
        return result.create();
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<? extends Recipe<FabricationInput>> getSerializer() {
        return ModRecipeTypes.FABRICATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<FabricationInput>> getType() {
        return ModRecipeTypes.FABRICATION.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredients.stream().map(CountedIngredient::ingredient).toList());
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeTypes.FABRICATION_CATEGORY.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        ArrayList<SlotDisplay> displays = new ArrayList<>();
        ingredients.forEach(ingredient -> {
            for (int count = 0; count < ingredient.count(); count++) {
                displays.add(ingredient.ingredient().display());
            }
        });
        return List.of(new FabricationRecipeDisplay(
                displays,
                new SlotDisplay.ItemStackSlotDisplay(result),
                new SlotDisplay.ItemSlotDisplay(ModItems.FABRICATOR.get())));
    }
}
