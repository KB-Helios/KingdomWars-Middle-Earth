package galacticwars.clonewars.registry;

import galacticwars.clonewars.GalacticWars;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(GalacticWars.MODID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> GALACTIC =
            CREATIVE_MODE_TABS.register("galactic", () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("itemGroup.galacticwars.galactic"))
                    .icon(() -> ModItems.BESKAR_INGOT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.DURACRETE.get());
                        output.accept(ModItems.BESKAR_ORE.get());
                        output.accept(ModItems.NIGHTSISTER_WEAVE_LOG.get());
                        output.accept(ModItems.NIGHTSISTER_WEAVE_PLANKS.get());
                        output.accept(ModItems.NIGHTSISTER_WEAVE_LEAVES.get());
                        output.accept(ModItems.NIGHTSISTER_WEAVE_SAPLING.get());
                        output.accept(ModItems.COMMAND_CENTER.get());
                        output.accept(ModItems.FABRICATOR.get());
                        output.accept(ModItems.CONTROL_BEACON.get());
                        output.accept(ModItems.JEDI_MEDITATION_SHRINE.get());
                        output.accept(ModItems.SITH_HOLOCRON_PEDESTAL.get());
                        output.accept(ModItems.NIGHTSISTER_SPIRIT_ALTAR.get());
                        output.accept(ModItems.TATOOINE_SAND.get());
                        output.accept(ModItems.GEONOSIS_ROCK.get());
                        output.accept(ModItems.KAMINO_PANEL.get());
                        output.accept(ModItems.CORUSCANT_PANEL.get());
                        output.accept(ModItems.BESKAR_INGOT.get());
                        output.accept(ModItems.RAW_BESKAR.get());
                        output.accept(ModItems.CREDIT_CHIP.get());
                        output.accept(ModItems.ENERGY_CELL.get());
                        output.accept(ModItems.BARC_SPEEDER_DEPLOYMENT_KIT.get());
                        output.accept(ModItems.AT_RT_DEPLOYMENT_KIT.get());
                        output.accept(ModItems.STAP_DEPLOYMENT_KIT.get());
                        output.accept(ModItems.AAT_DEPLOYMENT_KIT.get());
                        output.accept(ModItems.LAAT_GUNSHIP_DEPLOYMENT_KIT.get());
                        output.accept(ModItems.HYPERSPACE_NAVIGATOR.get());
                        output.accept(ModItems.CLAIM_TRANSPONDER.get());
                        output.accept(ModItems.BLUEPRINT_PROJECTOR.get());
                        output.accept(ModItems.COMMAND_MARKER.get());
                        output.accept(ModItems.REPUBLIC_PLASTOID_INGOT.get());
                        output.accept(ModItems.MANDALORIAN_FIBER.get());
                        output.accept(ModItems.MANDALORIAN_ALLOY_INGOT.get());
                        output.accept(ModItems.SEPARATIST_ALLOY_SHARD.get());
                        output.accept(ModItems.SEPARATIST_ALLOY_INGOT.get());
                        output.accept(ModItems.NIGHTSISTER_WEAVE.get());
                        output.accept(ModItems.DC15_BLASTER.get());
                        output.accept(ModItems.E5_BLASTER.get());
                        output.accept(ModItems.WESTAR_BLASTER.get());
                        output.accept(ModItems.SCATTER_BLASTER.get());
                        output.accept(ModItems.NIGHTSISTER_BOW.get());
                        output.accept(ModItems.BLUE_LIGHTSABER.get());
                        output.accept(ModItems.GREEN_LIGHTSABER.get());
                        output.accept(ModItems.RED_LIGHTSABER.get());
                        output.accept(ModItems.PURPLE_LIGHTSABER.get());
                        output.accept(ModItems.YELLOW_LIGHTSABER.get());
                        output.accept(ModItems.WHITE_LIGHTSABER.get());
                        output.accept(ModItems.VIBROBLADE.get());
                        output.accept(ModItems.POWER_DRILL.get());
                        output.accept(ModItems.PLASMA_CUTTER.get());
                        output.accept(ModItems.SONIC_EXCAVATOR.get());
                        output.accept(ModItems.HYDROSPANNER.get());
                        ModItems.FACTION_EQUIPMENT.values().forEach(item -> output.accept(item.get()));
                        output.accept(ModItems.PHASE_I_CLONE_HELMET.get());
                        output.accept(ModItems.PHASE_I_CLONE_CHESTPLATE.get());
                        output.accept(ModItems.PHASE_I_CLONE_LEGGINGS.get());
                        output.accept(ModItems.PHASE_I_CLONE_BOOTS.get());
                        output.accept(ModItems.REPUBLIC_FACTION_TOKEN.get());
                        output.accept(ModItems.MANDALORIAN_FACTION_TOKEN.get());
                        output.accept(ModItems.SEPARATIST_FACTION_TOKEN.get());
                        output.accept(ModItems.HUTT_CARTEL_FACTION_TOKEN.get());
                        output.accept(ModItems.NIGHTSISTER_FACTION_TOKEN.get());
                        output.accept(ModItems.CLONE_TROOPER_SPAWN_EGG.get());
                        output.accept(ModItems.ARC_TROOPER_SPAWN_EGG.get());
                        output.accept(ModItems.PHASE_I_CLONE_TROOPER_SPAWN_EGG.get());
                        output.accept(ModItems.PHASE_I_ARC_TROOPER_SPAWN_EGG.get());
                        output.accept(ModItems.JEDI_KNIGHT_SPAWN_EGG.get());
                        output.accept(ModItems.SENATE_COMMANDO_SPAWN_EGG.get());
                        output.accept(ModItems.REPUBLIC_HONOR_GUARD_SPAWN_EGG.get());
                        output.accept(ModItems.MANDALORIAN_WARRIOR_SPAWN_EGG.get());
                        output.accept(ModItems.MANDALORIAN_MARKSMAN_SPAWN_EGG.get());
                        output.accept(ModItems.MANDALORIAN_HEAVY_SPAWN_EGG.get());
                        output.accept(ModItems.B1_BATTLE_DROID_SPAWN_EGG.get());
                        output.accept(ModItems.SITH_ACOLYTE_SPAWN_EGG.get());
                        output.accept(ModItems.B1_SECURITY_DROID_SPAWN_EGG.get());
                        output.accept(ModItems.B2_SUPER_BATTLE_DROID_SPAWN_EGG.get());
                        output.accept(ModItems.COMMANDO_DROID_SPAWN_EGG.get());
                        output.accept(ModItems.HUTT_ENFORCER_SPAWN_EGG.get());
                        output.accept(ModItems.BOUNTY_HUNTER_SPAWN_EGG.get());
                        output.accept(ModItems.SMUGGLER_SPAWN_EGG.get());
                        output.accept(ModItems.NIGHTSISTER_ACOLYTE_SPAWN_EGG.get());
                        output.accept(ModItems.NIGHTSISTER_ARCHER_SPAWN_EGG.get());
                        output.accept(ModItems.NIGHTBROTHER_BRUTE_SPAWN_EGG.get());
                        output.accept(ModItems.REPUBLIC_CIVILIAN_SPAWN_EGG.get());
                        output.accept(ModItems.TOGRUTA_CIVILIAN_SPAWN_EGG.get());
                        output.accept(ModItems.SEPARATIST_TECHNICIAN_SPAWN_EGG.get());
                        output.accept(ModItems.MANDALORIAN_CLANSPERSON_SPAWN_EGG.get());
                        output.accept(ModItems.HUTT_CIVILIAN_SPAWN_EGG.get());
                        output.accept(ModItems.NIGHTSISTER_CIVILIAN_SPAWN_EGG.get());
                    })));

    private ModCreativeTabs() {
    }

    public static void register() {
        CREATIVE_MODE_TABS.register();
    }
}
