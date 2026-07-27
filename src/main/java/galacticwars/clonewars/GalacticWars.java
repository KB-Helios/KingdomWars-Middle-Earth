package galacticwars.clonewars;

import com.mojang.logging.LogUtils;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import galacticwars.clonewars.conquest.ConquestRuntimeEvents;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.network.GalacticNetwork;
import galacticwars.clonewars.registry.ModBlockEntityTypes;
import galacticwars.clonewars.registry.ModBlocks;
import galacticwars.clonewars.registry.ModCreativeTabs;
import galacticwars.clonewars.registry.ModDataComponents;
import galacticwars.clonewars.registry.ModEntityTypes;
import galacticwars.clonewars.registry.ModItems;
import galacticwars.clonewars.registry.ModMenuTypes;
import galacticwars.clonewars.registry.ModSounds;
import galacticwars.clonewars.registry.ModSensorTypes;
import galacticwars.clonewars.registry.ModRecipeTypes;
import galacticwars.clonewars.registry.ModWorldgenTypes;
import galacticwars.clonewars.runtime.GalacticRuntimeEvents;
import galacticwars.clonewars.world.FactionNaturalSpawnRules;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/** Shared loader-neutral bootstrap invoked by the Fabric and NeoForge entrypoints. */
public final class GalacticWars {
    public static final String MODID = "galacticwars";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    private GalacticWars() {
    }

    public static void init() {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        ModBlocks.register();
        ModBlockEntityTypes.register();
        ModEntityTypes.register();
        ModSensorTypes.register();
        ModDataComponents.register();
        ModItems.register();
        ModRecipeTypes.register();
        ModMenuTypes.register();
        ModSounds.register();
        ModWorldgenTypes.register();
        ModCreativeTabs.register();
        GalacticNetwork.init();
        GameplayDataManager.register();
        ConquestRuntimeEvents.register();
        GalacticRuntimeEvents.register();

        ModEntityTypes.recruits().forEach(entityType -> EntityAttributeRegistry.register(
                entityType,
                GalacticRecruitEntity::createAttributes));
        ModEntityTypes.recruits().forEach(entityType -> SpawnPlacementsRegistry.register(
                entityType,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FactionNaturalSpawnRules::check));

        Config.load();
        if (Config.LOG_STARTUP.getAsBoolean()) {
            LOGGER.info("Galactic Wars: Clone Wars common foundation loaded.");
        }
    }
}
