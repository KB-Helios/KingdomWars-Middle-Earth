package galacticwars.clonewars.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.fabrication.FabricationRecipe;
import galacticwars.clonewars.fabrication.FabricationRecipeDisplay;
import galacticwars.clonewars.force.ForceRitualRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public final class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(GalacticWars.MODID, Registries.RECIPE_TYPE);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(GalacticWars.MODID, Registries.RECIPE_SERIALIZER);
    public static final DeferredRegister<RecipeDisplay.Type<?>> RECIPE_DISPLAYS =
            DeferredRegister.create(GalacticWars.MODID, Registries.RECIPE_DISPLAY);
    public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES =
            DeferredRegister.create(GalacticWars.MODID, Registries.RECIPE_BOOK_CATEGORY);

    public static final RegistrySupplier<RecipeType<FabricationRecipe>> FABRICATION =
            RECIPE_TYPES.register("fabrication", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return GalacticWars.MODID + ":fabrication";
                }
            });
    public static final RegistrySupplier<RecipeSerializer<FabricationRecipe>> FABRICATION_SERIALIZER =
            RECIPE_SERIALIZERS.register("fabrication", () -> new RecipeSerializer<>(
                    FabricationRecipe.MAP_CODEC,
                    ByteBufCodecs.fromCodecWithRegistries(FabricationRecipe.MAP_CODEC.codec())));
    public static final RegistrySupplier<RecipeDisplay.Type<FabricationRecipeDisplay>> FABRICATION_DISPLAY =
            RECIPE_DISPLAYS.register("fabrication", () -> new RecipeDisplay.Type<>(
                    FabricationRecipeDisplay.MAP_CODEC,
                    ByteBufCodecs.fromCodecWithRegistries(FabricationRecipeDisplay.MAP_CODEC.codec())));
    public static final RegistrySupplier<RecipeBookCategory> FABRICATION_CATEGORY =
            RECIPE_BOOK_CATEGORIES.register("fabrication", RecipeBookCategory::new);
    public static final RegistrySupplier<RecipeType<ForceRitualRecipe>> FORCE_RITUAL =
            RECIPE_TYPES.register("force_ritual", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return GalacticWars.MODID + ":force_ritual";
                }
            });
    public static final RegistrySupplier<RecipeSerializer<ForceRitualRecipe>> FORCE_RITUAL_SERIALIZER =
            RECIPE_SERIALIZERS.register("force_ritual", () -> new RecipeSerializer<>(
                    ForceRitualRecipe.MAP_CODEC,
                    ByteBufCodecs.fromCodecWithRegistries(ForceRitualRecipe.MAP_CODEC.codec())));
    public static final RegistrySupplier<RecipeBookCategory> FORCE_RITUAL_CATEGORY =
            RECIPE_BOOK_CATEGORIES.register("force_ritual", RecipeBookCategory::new);

    private ModRecipeTypes() {
    }

    public static void register() {
        RECIPE_TYPES.register();
        RECIPE_SERIALIZERS.register();
        RECIPE_DISPLAYS.register();
        RECIPE_BOOK_CATEGORIES.register();
    }
}
