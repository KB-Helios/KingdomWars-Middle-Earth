package galacticwars.clonewars.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.data.LaunchContentDefinitions;
import galacticwars.clonewars.registry.ModWorldgenTypes;
import galacticwars.clonewars.settlement.BaseBlockPlacement;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** One sparse structure type selecting a data-defined faction template for the local biome. */
public final class BlueprintStructure extends Structure {
    private static final int PLANET_POI_EXCLUSION_RADIUS = 160;
    public static final MapCodec<BlueprintStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    BlueprintSiteKind.CODEC.fieldOf("site_kind").forGetter(BlueprintStructure::siteKind)
            ).apply(instance, BlueprintStructure::new));

    private final BlueprintSiteKind siteKind;

    public BlueprintStructure(StructureSettings settings, BlueprintSiteKind siteKind) {
        super(settings);
        this.siteKind = java.util.Objects.requireNonNull(siteKind, "siteKind");
    }

    public BlueprintSiteKind siteKind() {
        return siteKind;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int y = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        Holder<?> biome = context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
                QuartPos.fromBlock(z), context.randomState().sampler());
        String biomeId = biome.unwrapKey().map(key -> key.identifier().toString()).orElse("");
        if (nearProtectedPlanetPoi(biomeId, x, z)) {
            return Optional.empty();
        }
        List<KingdomBaseBlueprint> eligible = eligibleBlueprintsForBiome(y, biomeId, siteKind);
        if (eligible.isEmpty()) {
            return Optional.empty();
        }
        RandomSource random = RandomSource.create(context.seed()
                ^ ((long) context.chunkPos().x() * 341873128712L)
                ^ ((long) context.chunkPos().z() * 132897987541L));
        KingdomBaseBlueprint selected = pickByWeight(eligible, random);
        int degrees = selected.allowedRotations().get(random.nextInt(selected.allowedRotations().size()));
        int rotationSteps = degrees / 90;
        if (terrainSlopeExceedsLimit(selected, rotationSteps, x, z, context)) {
            return Optional.empty();
        }
        BlockPos position = computePlacementPosition(selected, x, y, z);
        return Optional.of(new GenerationStub(new BlockPos(x, y, z), builder -> builder.addPiece(
                new BlueprintStructurePiece(context.structureTemplateManager(), selected, rotationSteps, position))));
    }

    static List<KingdomBaseBlueprint> eligibleBlueprintsForBiome(
            int y, String biomeId, BlueprintSiteKind siteKind
    ) {
        return GameplayDataManager.snapshot().blueprints().values().stream()
                .filter(blueprint -> blueprint.worldgen().isPresent())
                .filter(blueprint -> blueprint.worldgen().orElseThrow().biomes().contains(biomeId))
                .filter(blueprint -> blueprint.worldgen().orElseThrow().siteKind() == siteKind)
                .filter(blueprint -> y >= blueprint.terrainConstraints().minY()
                        && y <= blueprint.terrainConstraints().maxY())
                .sorted(Comparator.comparing(KingdomBaseBlueprint::id))
                .toList();
    }

    static boolean nearProtectedPlanetPoi(String biomeId, int x, int z) {
        if (!biomeId.startsWith("galacticwars:")) {
            return false;
        }
        String planetId = biomeId.substring(biomeId.indexOf(':') + 1);
        LaunchContentDefinitions.PlanetDefinition planet =
                GameplayDataManager.snapshot().launchContent().planets().get(planetId);
        if (planet == null) {
            return false;
        }
        long limit = (long) PLANET_POI_EXCLUSION_RADIUS * PLANET_POI_EXCLUSION_RADIUS;
        return planet.pointsOfInterest().stream()
                .filter(poi -> poi.role().equals("arrival")
                        || poi.role().equals("economy")
                        || poi.role().equals("contested"))
                .anyMatch(poi -> {
                    long dx = (long) poi.x() - x;
                    long dz = (long) poi.z() - z;
                    return dx * dx + dz * dz < limit;
                });
    }

    private static KingdomBaseBlueprint pickByWeight(
            List<KingdomBaseBlueprint> eligible, RandomSource random
    ) {
        int totalWeight = eligible.stream()
                .mapToInt(blueprint -> blueprint.worldgen().orElseThrow().placementWeight())
                .sum();
        int selectedWeight = random.nextInt(totalWeight);
        for (KingdomBaseBlueprint blueprint : eligible) {
            selectedWeight -= blueprint.worldgen().orElseThrow().placementWeight();
            if (selectedWeight < 0) {
                return blueprint;
            }
        }
        return eligible.getFirst();
    }

    private static boolean terrainSlopeExceedsLimit(
            KingdomBaseBlueprint blueprint,
            int rotationSteps,
            int x,
            int z,
            GenerationContext context
    ) {
        int minX = blueprint.placements().stream().mapToInt(BaseBlockPlacement::x).min()
                .orElse(blueprint.anchor().x());
        int maxX = blueprint.placements().stream().mapToInt(BaseBlockPlacement::x).max()
                .orElse(blueprint.anchor().x());
        int minZ = blueprint.placements().stream().mapToInt(BaseBlockPlacement::z).min()
                .orElse(blueprint.anchor().z());
        int maxZ = blueprint.placements().stream().mapToInt(BaseBlockPlacement::z).max()
                .orElse(blueprint.anchor().z());
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int templateX : new int[]{minX, maxX}) {
            for (int templateZ : new int[]{minZ, maxZ}) {
                int relativeX = templateX - blueprint.anchor().x();
                int relativeZ = templateZ - blueprint.anchor().z();
                int dx = switch (rotationSteps) {
                    case 1 -> -relativeZ;
                    case 2 -> -relativeX;
                    case 3 -> relativeZ;
                    default -> relativeX;
                };
                int dz = switch (rotationSteps) {
                    case 1 -> relativeX;
                    case 2 -> -relativeZ;
                    case 3 -> -relativeX;
                    default -> relativeZ;
                };
                int corner = context.chunkGenerator().getBaseHeight(x + dx, z + dz,
                        Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                min = Math.min(min, corner);
                max = Math.max(max, corner);
            }
        }
        return max - min > blueprint.terrainConstraints().maxSlope();
    }

    private static BlockPos computePlacementPosition(KingdomBaseBlueprint blueprint, int x, int y, int z) {
        return new BlockPos(x - blueprint.anchor().x(), y - blueprint.anchor().y(), z - blueprint.anchor().z());
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgenTypes.BLUEPRINT_STRUCTURE.get();
    }
}
