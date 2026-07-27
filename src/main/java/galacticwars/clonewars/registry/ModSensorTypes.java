package galacticwars.clonewars.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.entity.ai.ArmyGroupStateSensor;
import galacticwars.clonewars.entity.ai.ArmyThreatSensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;

/** Common, loader-neutral identities for recruit SmartBrain sensors. */
public final class ModSensorTypes {
    private static final DeferredRegister<SensorType<?>> SENSOR_TYPES =
            DeferredRegister.create(GalacticWars.MODID, Registries.SENSOR_TYPE);

    public static final RegistrySupplier<SensorType<ArmyGroupStateSensor>> ARMY_GROUP_STATE =
            SENSOR_TYPES.register(
                    "army_group_state",
                    () -> new SensorType<>(ArmyGroupStateSensor::new));
    public static final RegistrySupplier<SensorType<ArmyThreatSensor>> ARMY_THREAT =
            SENSOR_TYPES.register(
                    "army_threat",
                    () -> new SensorType<>(ArmyThreatSensor::new));

    private ModSensorTypes() {
    }

    public static void register() {
        SENSOR_TYPES.register();
    }
}
