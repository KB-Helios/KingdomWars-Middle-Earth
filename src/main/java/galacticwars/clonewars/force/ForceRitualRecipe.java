package galacticwars.clonewars.force;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import galacticwars.clonewars.fabrication.CountedIngredient;
import galacticwars.clonewars.fabrication.FabricationInput;
import galacticwars.clonewars.fabrication.FabricationRecipeDisplay;
import galacticwars.clonewars.fabrication.RecipeIngredientPlanner;
import galacticwars.clonewars.registry.ModItems;
import galacticwars.clonewars.registry.ModRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.Item;
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

public record ForceRitualRecipe(
        List<CountedIngredient> ingredients,
        ItemStackTemplate result,
        String shrine,
        String path,
        int requiredRank,
        boolean showNotification
) implements Recipe<FabricationInput> {
    public static final MapCodec<ForceRitualRecipe> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    CountedIngredient.CODEC.listOf(1, 16).fieldOf("ingredients")
                            .forGetter(ForceRitualRecipe::ingredients),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ForceRitualRecipe::result),
                    com.mojang.serialization.Codec.STRING.fieldOf("shrine")
                            .forGetter(ForceRitualRecipe::shrine),
                    com.mojang.serialization.Codec.STRING.fieldOf("path")
                            .forGetter(ForceRitualRecipe::path),
                    com.mojang.serialization.Codec.intRange(1, 10).fieldOf("required_rank")
                            .forGetter(ForceRitualRecipe::requiredRank),
                    com.mojang.serialization.Codec.BOOL.optionalFieldOf("show_notification", true)
                            .forGetter(ForceRitualRecipe::showNotification)
            ).apply(instance, ForceRitualRecipe::new));

    public ForceRitualRecipe {
        ingredients = List.copyOf(ingredients);
        Objects.requireNonNull(result, "result");
        if (shrine == null || shrine.isBlank() || path == null || path.isBlank()) {
            throw new IllegalArgumentException("Ritual shrine and path are required");
        }
    }

    @Override
    public boolean matches(FabricationInput input, Level level) {
        return removals(input).length == input.size();
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
        return "galacticwars:lightsabers";
    }

    @Override
    public RecipeSerializer<? extends Recipe<FabricationInput>> getSerializer() {
        return ModRecipeTypes.FORCE_RITUAL_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<FabricationInput>> getType() {
        return ModRecipeTypes.FORCE_RITUAL.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredients.stream().map(CountedIngredient::ingredient).toList());
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeTypes.FORCE_RITUAL_CATEGORY.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        ArrayList<SlotDisplay> displays = new ArrayList<>();
        ingredients.forEach(ingredient -> {
            for (int count = 0; count < ingredient.count(); count++) {
                displays.add(ingredient.ingredient().display());
            }
        });
        Item station = shrine.equals("sith")
                ? ModItems.SITH_HOLOCRON_PEDESTAL.get()
                : ModItems.JEDI_MEDITATION_SHRINE.get();
        return List.of(new FabricationRecipeDisplay(
                displays,
                new SlotDisplay.ItemStackSlotDisplay(result),
                new SlotDisplay.ItemSlotDisplay(station)));
    }
}
