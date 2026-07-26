package galacticwars.clonewars.workforce;

import java.util.List;
import java.util.Optional;

public final class WorkerProfessionCatalog {
    public static final int FIRST_COMMAND_BUTTON_ID = 20;

    private static final List<WorkerProfessionDefinition> PROFESSIONS = List.of(
            new WorkerProfessionDefinition(
                    WorkerProfession.FARMER,
                    WorkAreaType.CROP_FARM,
                    20,
                    FIRST_COMMAND_BUTTON_ID,
                    "minecraft:iron_hoe"),
            new WorkerProfessionDefinition(
                    WorkerProfession.LUMBERJACK,
                    WorkAreaType.LUMBER_AREA,
                    20,
                    FIRST_COMMAND_BUTTON_ID + 1,
                    "minecraft:iron_axe"),
            new WorkerProfessionDefinition(
                    WorkerProfession.FISHERMAN,
                    WorkAreaType.FISHING_AREA,
                    18,
                    FIRST_COMMAND_BUTTON_ID + 2,
                    "minecraft:fishing_rod"),
            new WorkerProfessionDefinition(
                    WorkerProfession.ANIMAL_FARMER,
                    WorkAreaType.ANIMAL_FARM,
                    22,
                    FIRST_COMMAND_BUTTON_ID + 3,
                    "minecraft:wheat"),
            new WorkerProfessionDefinition(
                    WorkerProfession.MINER,
                    WorkAreaType.MINING_AREA,
                    28,
                    FIRST_COMMAND_BUTTON_ID + 4,
                    "minecraft:iron_pickaxe"),
            new WorkerProfessionDefinition(
                    WorkerProfession.BUILDER,
                    WorkAreaType.BUILDING_AREA,
                    30,
                    FIRST_COMMAND_BUTTON_ID + 5,
                    "minecraft:bricks"),
            new WorkerProfessionDefinition(
                    WorkerProfession.COOK,
                    WorkAreaType.KITCHEN,
                    18,
                    FIRST_COMMAND_BUTTON_ID + 6,
                    "minecraft:bread"),
            new WorkerProfessionDefinition(
                    WorkerProfession.MERCHANT,
                    WorkAreaType.MARKET,
                    26,
                    FIRST_COMMAND_BUTTON_ID + 7,
                    "galacticwars:credit_chip"),
            new WorkerProfessionDefinition(
                    WorkerProfession.COURIER,
                    WorkAreaType.COURIER_ROUTE,
                    16,
                    FIRST_COMMAND_BUTTON_ID + 8,
                    "minecraft:chest"),
            new WorkerProfessionDefinition(
                    WorkerProfession.TECHNICIAN,
                    WorkAreaType.LABORATORY,
                    16,
                    FIRST_COMMAND_BUTTON_ID + 13,
                    "minecraft:redstone"));
    private WorkerProfessionCatalog() {
    }

    public static List<WorkerProfessionDefinition> professions() {
        return PROFESSIONS;
    }

    public static Optional<WorkerProfessionDefinition> definition(WorkerProfession profession) {
        return PROFESSIONS.stream()
                .filter(definition -> definition.profession() == profession)
                .findFirst();
    }

    public static Optional<WorkerProfession> professionForButton(int buttonId) {
        return PROFESSIONS.stream()
                .filter(definition -> definition.commandButtonId() == buttonId)
                .filter(definition -> isEnabled(definition.profession()))
                .map(WorkerProfessionDefinition::profession)
                .findFirst();
    }

    public static Optional<WorkerProfessionDefinition> definitionForButton(int buttonId) {
        return PROFESSIONS.stream()
                .filter(definition -> definition.commandButtonId() == buttonId)
                .findFirst();
    }

    public static List<WorkerProfessionDefinition> enabledProfessions() {
        return PROFESSIONS.stream()
                .filter(definition -> isEnabled(definition.profession()))
                .toList();
    }

    public static boolean isEnabled(WorkerProfession profession) {
        return definition(profession).isPresent();
    }
}
