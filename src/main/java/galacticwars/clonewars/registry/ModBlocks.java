package galacticwars.clonewars.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.force.ForceShrineBlock;
import galacticwars.clonewars.settlement.CommandCenterBlock;
import galacticwars.clonewars.world.NightsisterWeaveTreeGrower;
import galacticwars.clonewars.world.PlayerTriggeredSaplingBlock;
import galacticwars.clonewars.world.BlueprintSiteAnchorBlock;
import galacticwars.clonewars.world.BlueprintSiteLootBlock;
import galacticwars.clonewars.world.FactionCommandPostBlock;
import galacticwars.clonewars.fabrication.FabricatorBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(GalacticWars.MODID, Registries.BLOCK);

    public static final RegistrySupplier<Block> DURACRETE = registerSimple(
            "duracrete", properties -> properties.mapColor(MapColor.STONE));
    public static final RegistrySupplier<Block> BESKAR_ORE = registerSimple(
            "beskar_ore", properties -> properties
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops());
    public static final RegistrySupplier<RotatedPillarBlock> NIGHTSISTER_WEAVE_LOG = register(
            "nightsister_weave_log", properties -> new RotatedPillarBlock(
                    properties.mapColor(MapColor.COLOR_YELLOW).strength(2.0F).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> NIGHTSISTER_WEAVE_PLANKS = registerSimple(
            "nightsister_weave_planks", properties -> properties
                    .mapColor(MapColor.COLOR_YELLOW).strength(2.0F, 3.0F).sound(SoundType.WOOD));
    public static final RegistrySupplier<UntintedParticleLeavesBlock> NIGHTSISTER_WEAVE_LEAVES = register(
            "nightsister_weave_leaves", properties -> new UntintedParticleLeavesBlock(
                    0.02F, ParticleTypes.CHERRY_LEAVES,
                    properties.mapColor(MapColor.COLOR_YELLOW).strength(0.2F).randomTicks()
                            .sound(SoundType.GRASS).noOcclusion()));
    public static final RegistrySupplier<PlayerTriggeredSaplingBlock> NIGHTSISTER_WEAVE_SAPLING = register(
            "nightsister_weave_sapling", properties -> new PlayerTriggeredSaplingBlock(
                    NightsisterWeaveTreeGrower.INSTANCE,
                    properties.mapColor(MapColor.PLANT).noCollision().randomTicks()
                            .instabreak().sound(SoundType.GRASS)));
    public static final RegistrySupplier<CommandCenterBlock> COMMAND_CENTER = register(
            "command_center", properties -> new CommandCenterBlock(
                    properties.mapColor(MapColor.STONE).strength(4.0F, 1200.0F).sound(SoundType.STONE)));
    public static final RegistrySupplier<FabricatorBlock> FABRICATOR = register(
            "fabricator", properties -> new FabricatorBlock(properties
                    .mapColor(MapColor.METAL).strength(4.0F, 12.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> CONTROL_BEACON = registerSimple(
            "control_beacon", properties -> properties
                    .mapColor(MapColor.METAL).strength(-1.0F, 3_600_000.0F).lightLevel(state -> 10)
                    .sound(SoundType.METAL));
    public static final RegistrySupplier<BlueprintSiteAnchorBlock> BLUEPRINT_SITE_ANCHOR = register(
            "blueprint_site_anchor", properties -> new BlueprintSiteAnchorBlock(properties
                    .mapColor(MapColor.NONE).strength(-1.0F, 3_600_000.0F).noOcclusion()));
    public static final RegistrySupplier<BlueprintSiteLootBlock> BLUEPRINT_SITE_LOOT = register(
            "blueprint_site_loot", properties -> new BlueprintSiteLootBlock(properties
                    .mapColor(MapColor.NONE).strength(-1.0F, 3_600_000.0F).noOcclusion()));
    public static final RegistrySupplier<FactionCommandPostBlock> FACTION_COMMAND_POST = register(
            "faction_command_post", properties -> new FactionCommandPostBlock(properties
                    .mapColor(MapColor.METAL).strength(-1.0F, 3_600_000.0F)
                    .lightLevel(state -> 6).sound(SoundType.METAL)));
    public static final RegistrySupplier<ForceShrineBlock> JEDI_MEDITATION_SHRINE = register(
            "jedi_meditation_shrine", properties -> new ForceShrineBlock("jedi", properties
                    .mapColor(MapColor.STONE).strength(3.5F, 12.0F).lightLevel(state -> 8)
                    .sound(SoundType.AMETHYST).noOcclusion()));
    public static final RegistrySupplier<ForceShrineBlock> SITH_HOLOCRON_PEDESTAL = register(
            "sith_holocron_pedestal", properties -> new ForceShrineBlock("sith", properties
                    .mapColor(MapColor.DEEPSLATE).strength(4.0F, 18.0F).lightLevel(state -> 7)
                    .sound(SoundType.DEEPSLATE).noOcclusion()));
    public static final RegistrySupplier<ForceShrineBlock> NIGHTSISTER_SPIRIT_ALTAR = register(
            "nightsister_spirit_altar", properties -> new ForceShrineBlock("nightsister", properties
                    .mapColor(MapColor.COLOR_GREEN).strength(3.0F, 10.0F).lightLevel(state -> 9)
                    .sound(SoundType.WOOD).noOcclusion()));

    public static final RegistrySupplier<SandBlock> TATOOINE_SAND = register(
            "tatooine_sand", properties -> new SandBlock(
                    new ColorRGBA(0xE8C07AFF),
                    properties.mapColor(MapColor.SAND).strength(0.5F).sound(SoundType.SAND)));
    public static final RegistrySupplier<Block> GEONOSIS_ROCK = registerSimple(
            "geonosis_rock", properties -> properties
                    .mapColor(MapColor.COLOR_ORANGE).strength(1.8F, 6.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final RegistrySupplier<Block> KAMINO_PANEL = registerSimple(
            "kamino_panel", properties -> properties
                    .mapColor(MapColor.METAL).strength(3.0F, 6.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final RegistrySupplier<Block> CORUSCANT_PANEL = registerSimple(
            "coruscant_panel", properties -> properties
                    .mapColor(MapColor.METAL).strength(3.0F, 6.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops());

    private ModBlocks() {
    }

    public static void register() {
        BLOCKS.register();
    }

    private static RegistrySupplier<Block> registerSimple(
            String name,
            UnaryOperator<BlockBehaviour.Properties> propertiesFactory
    ) {
        return register(name, properties -> new Block(propertiesFactory.apply(properties)));
    }

    private static <T extends Block> RegistrySupplier<T> register(
            String name,
            Function<BlockBehaviour.Properties, T> factory
    ) {
        return BLOCKS.register(name, () -> factory.apply(properties(name)));
    }

    private static BlockBehaviour.Properties properties(String name) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(GalacticWars.MODID, name)));
    }
}
