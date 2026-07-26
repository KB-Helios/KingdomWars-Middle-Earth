package galacticwars.clonewars.registry;

import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.entity.RecruitSpawnEggItem;
import galacticwars.clonewars.combat.BlasterItem;
import galacticwars.clonewars.combat.BlasterHeatPolicy;
import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.faction.IdentityChipItem;
import galacticwars.clonewars.item.GalacticArmorItem;
import galacticwars.clonewars.item.LightsaberItem;
import galacticwars.clonewars.item.TacticalCommandMarkerItem;
import galacticwars.clonewars.world.HyperspaceNavigatorItem;
import galacticwars.clonewars.vehicle.VehicleDeploymentKitItem;
import galacticwars.clonewars.kingdom.ClaimTransponderItem;
import galacticwars.clonewars.settlement.BlueprintProjectorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.equipment.ArmorType;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(GalacticWars.MODID, Registries.ITEM);

    public static final RegistrySupplier<BlockItem> DURACRETE =
            registerBlockItem("duracrete", ModBlocks.DURACRETE);
    public static final RegistrySupplier<BlockItem> BESKAR_ORE =
            registerBlockItem("beskar_ore", ModBlocks.BESKAR_ORE);
    public static final RegistrySupplier<BlockItem> NIGHTSISTER_WEAVE_LOG =
            registerBlockItem("nightsister_weave_log", ModBlocks.NIGHTSISTER_WEAVE_LOG);
    public static final RegistrySupplier<BlockItem> NIGHTSISTER_WEAVE_PLANKS =
            registerBlockItem("nightsister_weave_planks", ModBlocks.NIGHTSISTER_WEAVE_PLANKS);
    public static final RegistrySupplier<BlockItem> NIGHTSISTER_WEAVE_LEAVES =
            registerBlockItem("nightsister_weave_leaves", ModBlocks.NIGHTSISTER_WEAVE_LEAVES);
    public static final RegistrySupplier<BlockItem> NIGHTSISTER_WEAVE_SAPLING =
            registerBlockItem("nightsister_weave_sapling", ModBlocks.NIGHTSISTER_WEAVE_SAPLING);
    public static final RegistrySupplier<BlockItem> COMMAND_CENTER =
            registerBlockItem("command_center", ModBlocks.COMMAND_CENTER);
    public static final RegistrySupplier<BlockItem> FABRICATOR =
            registerBlockItem("fabricator", ModBlocks.FABRICATOR);
    public static final RegistrySupplier<BlockItem> CONTROL_BEACON =
            registerBlockItem("control_beacon", ModBlocks.CONTROL_BEACON);
    public static final RegistrySupplier<BlockItem> JEDI_MEDITATION_SHRINE =
            registerBlockItem("jedi_meditation_shrine", ModBlocks.JEDI_MEDITATION_SHRINE);
    public static final RegistrySupplier<BlockItem> SITH_HOLOCRON_PEDESTAL =
            registerBlockItem("sith_holocron_pedestal", ModBlocks.SITH_HOLOCRON_PEDESTAL);
    public static final RegistrySupplier<BlockItem> NIGHTSISTER_SPIRIT_ALTAR =
            registerBlockItem("nightsister_spirit_altar", ModBlocks.NIGHTSISTER_SPIRIT_ALTAR);
    public static final RegistrySupplier<BlockItem> TATOOINE_SAND =
            registerBlockItem("tatooine_sand", ModBlocks.TATOOINE_SAND);
    public static final RegistrySupplier<BlockItem> GEONOSIS_ROCK =
            registerBlockItem("geonosis_rock", ModBlocks.GEONOSIS_ROCK);
    public static final RegistrySupplier<BlockItem> KAMINO_PANEL =
            registerBlockItem("kamino_panel", ModBlocks.KAMINO_PANEL);
    public static final RegistrySupplier<BlockItem> CORUSCANT_PANEL =
            registerBlockItem("coruscant_panel", ModBlocks.CORUSCANT_PANEL);
    public static final RegistrySupplier<Item> BESKAR_INGOT =
            registerSimpleItem("beskar_ingot", properties -> properties);
    public static final RegistrySupplier<Item> RAW_BESKAR =
            registerSimpleItem("raw_beskar", properties -> properties);
    public static final RegistrySupplier<Item> CREDIT_CHIP =
            registerSimpleItem("credit_chip", properties -> properties.stacksTo(64));
    public static final RegistrySupplier<Item> ENERGY_CELL =
            registerSimpleItem("energy_cell", properties -> properties.stacksTo(64));
    public static final RegistrySupplier<VehicleDeploymentKitItem> BARC_SPEEDER_DEPLOYMENT_KIT =
            vehicleKit("barc_speeder", ModEntityTypes.BARC_SPEEDER);
    public static final RegistrySupplier<VehicleDeploymentKitItem> AT_RT_DEPLOYMENT_KIT =
            vehicleKit("at_rt", ModEntityTypes.AT_RT);
    public static final RegistrySupplier<VehicleDeploymentKitItem> STAP_DEPLOYMENT_KIT =
            vehicleKit("stap", ModEntityTypes.STAP);
    public static final RegistrySupplier<VehicleDeploymentKitItem> AAT_DEPLOYMENT_KIT =
            vehicleKit("aat", ModEntityTypes.AAT);
    public static final RegistrySupplier<VehicleDeploymentKitItem> LAAT_GUNSHIP_DEPLOYMENT_KIT =
            vehicleKit("laat_gunship", ModEntityTypes.LAAT_GUNSHIP);
    public static final RegistrySupplier<Item> BLASTER_BOLT =
            registerSimpleItem("blaster_bolt", properties -> properties.stacksTo(1));
    public static final RegistrySupplier<HyperspaceNavigatorItem> HYPERSPACE_NAVIGATOR =
            registerItem("hyperspace_navigator", HyperspaceNavigatorItem::new);
    public static final RegistrySupplier<ClaimTransponderItem> CLAIM_TRANSPONDER =
            registerItem("claim_transponder", ClaimTransponderItem::new);
    public static final RegistrySupplier<BlueprintProjectorItem> BLUEPRINT_PROJECTOR =
            registerItem("blueprint_projector", BlueprintProjectorItem::new);
    public static final RegistrySupplier<TacticalCommandMarkerItem> COMMAND_MARKER =
            registerItem("command_marker", TacticalCommandMarkerItem::new);
    public static final RegistrySupplier<BlasterItem> DC15_BLASTER =
            registerItem("dc15_blaster", properties -> new BlasterItem("dc15_blaster", blaster(properties, 850), 6.0D, 3.4F, 0.7F));
    public static final RegistrySupplier<BlasterItem> E5_BLASTER =
            registerItem("e5_blaster", properties -> new BlasterItem("e5_blaster", blaster(properties, 700), 5.0D, 3.2F, 1.0F));
    public static final RegistrySupplier<BlasterItem> WESTAR_BLASTER =
            registerItem("westar_blaster", properties -> new BlasterItem("westar_blaster", blaster(properties, 900), 6.5D, 3.5F, 0.6F));
    public static final RegistrySupplier<BlasterItem> SCATTER_BLASTER =
            registerItem("scatter_blaster", properties -> new BlasterItem("scatter_blaster", blaster(properties, 650), 7.0D, 2.8F, 1.4F));
    public static final RegistrySupplier<BowItem> NIGHTSISTER_BOW =
            registerItem("nightsister_bow", properties -> new BowItem(properties.durability(600)));
    public static final RegistrySupplier<LightsaberItem> BLUE_LIGHTSABER = lightsaber("blue");
    public static final RegistrySupplier<LightsaberItem> GREEN_LIGHTSABER = lightsaber("green");
    public static final RegistrySupplier<LightsaberItem> RED_LIGHTSABER = lightsaber("red");
    public static final RegistrySupplier<LightsaberItem> PURPLE_LIGHTSABER = lightsaber("purple");
    public static final RegistrySupplier<LightsaberItem> YELLOW_LIGHTSABER = lightsaber("yellow");
    public static final RegistrySupplier<LightsaberItem> WHITE_LIGHTSABER = lightsaber("white");
    public static final RegistrySupplier<Item> VIBROBLADE =
            registerSimpleItem("vibroblade", properties -> properties.sword(ModEquipmentMaterials.MANDALORIAN.tool(), 3.0F, -2.4F));
    public static final RegistrySupplier<Item> POWER_DRILL =
            registerSimpleItem("power_drill", properties -> properties.pickaxe(ModEquipmentMaterials.REPUBLIC.tool(), 1.0F, -2.8F));
    public static final RegistrySupplier<Item> PLASMA_CUTTER =
            registerItem("plasma_cutter", properties -> new AxeItem(ModEquipmentMaterials.REPUBLIC.tool(), 6.0F, -3.0F, properties));
    public static final RegistrySupplier<Item> SONIC_EXCAVATOR =
            registerItem("sonic_excavator", properties -> new ShovelItem(ModEquipmentMaterials.REPUBLIC.tool(), 1.5F, -3.0F, properties));
    public static final RegistrySupplier<Item> HYDROSPANNER =
            registerItem("hydrospanner", properties -> new HoeItem(ModEquipmentMaterials.REPUBLIC.tool(), -2.0F, -1.0F, properties));
    public static final RegistrySupplier<Item> REPUBLIC_PLASTOID_INGOT =
            registerSimpleItem("republic_plastoid_ingot", properties -> properties);
    public static final RegistrySupplier<Item> MANDALORIAN_FIBER =
            registerSimpleItem("mandalorian_fiber", properties -> properties);
    public static final RegistrySupplier<Item> MANDALORIAN_ALLOY_INGOT =
            registerSimpleItem("mandalorian_alloy_ingot", properties -> properties);
    public static final RegistrySupplier<Item> SEPARATIST_ALLOY_SHARD =
            registerSimpleItem("separatist_alloy_shard", properties -> properties);
    public static final RegistrySupplier<Item> SEPARATIST_ALLOY_INGOT =
            registerSimpleItem("separatist_alloy_ingot", properties -> properties);
    public static final RegistrySupplier<Item> NIGHTSISTER_WEAVE =
            registerSimpleItem("nightsister_weave", properties -> properties);
    public static final java.util.Map<String, RegistrySupplier<Item>> FACTION_EQUIPMENT = registerFactionEquipment();
    public static final RegistrySupplier<GalacticArmorItem> PHASE_I_CLONE_HELMET =
            phaseICloneArmor("helmet", ArmorType.HELMET);
    public static final RegistrySupplier<GalacticArmorItem> PHASE_I_CLONE_CHESTPLATE =
            phaseICloneArmor("chestplate", ArmorType.CHESTPLATE);
    public static final RegistrySupplier<GalacticArmorItem> PHASE_I_CLONE_LEGGINGS =
            phaseICloneArmor("leggings", ArmorType.LEGGINGS);
    public static final RegistrySupplier<GalacticArmorItem> PHASE_I_CLONE_BOOTS =
            phaseICloneArmor("boots", ArmorType.BOOTS);
    public static final RegistrySupplier<IdentityChipItem> REPUBLIC_FACTION_TOKEN = factionToken("republic");
    public static final RegistrySupplier<IdentityChipItem> MANDALORIAN_FACTION_TOKEN = factionToken("mandalorian");
    public static final RegistrySupplier<IdentityChipItem> SEPARATIST_FACTION_TOKEN = factionToken("separatist");
    public static final RegistrySupplier<IdentityChipItem> HUTT_CARTEL_FACTION_TOKEN = factionToken("hutt_cartel");
    public static final RegistrySupplier<IdentityChipItem> NIGHTSISTER_FACTION_TOKEN = factionToken("nightsister");
    public static final RegistrySupplier<RecruitSpawnEggItem> CLONE_TROOPER_SPAWN_EGG =
            registerItem("clone_trooper_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.CLONE_TROOPER.get(), "clone_trooper", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> ARC_TROOPER_SPAWN_EGG =
            registerItem("arc_trooper_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.ARC_TROOPER.get(), "arc_trooper", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> PHASE_I_CLONE_TROOPER_SPAWN_EGG =
            registerItem("phase_i_clone_trooper_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.PHASE_I_CLONE_TROOPER.get(), "phase_i_clone_trooper", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> PHASE_I_ARC_TROOPER_SPAWN_EGG =
            registerItem("phase_i_arc_trooper_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.PHASE_I_ARC_TROOPER.get(), "phase_i_arc_trooper", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> JEDI_KNIGHT_SPAWN_EGG =
            registerItem("jedi_knight_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.JEDI_KNIGHT.get(), "jedi_knight", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> SENATE_COMMANDO_SPAWN_EGG =
            registerItem("senate_commando_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.SENATE_COMMANDO.get(), "senate_commando", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> REPUBLIC_HONOR_GUARD_SPAWN_EGG =
            registerItem("republic_honor_guard_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.REPUBLIC_HONOR_GUARD.get(), "republic_honor_guard", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> MANDALORIAN_WARRIOR_SPAWN_EGG =
            registerItem("mandalorian_warrior_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.MANDALORIAN_WARRIOR.get(), "mandalorian_warrior", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> MANDALORIAN_MARKSMAN_SPAWN_EGG =
            registerItem("mandalorian_marksman_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.MANDALORIAN_MARKSMAN.get(), "mandalorian_marksman", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> MANDALORIAN_HEAVY_SPAWN_EGG =
            registerItem("mandalorian_heavy_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.MANDALORIAN_HEAVY.get(), "mandalorian_heavy", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> B1_BATTLE_DROID_SPAWN_EGG =
            registerItem("b1_battle_droid_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.B1_BATTLE_DROID.get(), "b1_battle_droid", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> SITH_ACOLYTE_SPAWN_EGG =
            registerItem("sith_acolyte_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.SITH_ACOLYTE.get(), "sith_acolyte", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> B1_SECURITY_DROID_SPAWN_EGG =
            registerItem("b1_security_droid_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.B1_SECURITY_DROID.get(), "b1_security_droid", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> B2_SUPER_BATTLE_DROID_SPAWN_EGG =
            registerItem("b2_super_battle_droid_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.B2_SUPER_BATTLE_DROID.get(), "b2_super_battle_droid", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> COMMANDO_DROID_SPAWN_EGG =
            registerItem("commando_droid_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.COMMANDO_DROID.get(), "commando_droid", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> HUTT_ENFORCER_SPAWN_EGG =
            registerItem("hutt_enforcer_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.HUTT_ENFORCER.get(), "hutt_enforcer", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> BOUNTY_HUNTER_SPAWN_EGG =
            registerItem("bounty_hunter_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.BOUNTY_HUNTER.get(), "bounty_hunter", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> SMUGGLER_SPAWN_EGG =
            registerItem("smuggler_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.SMUGGLER.get(), "smuggler", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> NIGHTSISTER_ACOLYTE_SPAWN_EGG =
            registerItem("nightsister_acolyte_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.NIGHTSISTER_ACOLYTE.get(), "nightsister_acolyte", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> NIGHTSISTER_ARCHER_SPAWN_EGG =
            registerItem("nightsister_archer_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.NIGHTSISTER_ARCHER.get(), "nightsister_archer", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> NIGHTBROTHER_BRUTE_SPAWN_EGG =
            registerItem("nightbrother_brute_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.NIGHTBROTHER_BRUTE.get(), "nightbrother_brute", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> REPUBLIC_CIVILIAN_SPAWN_EGG =
            registerItem("republic_civilian_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.REPUBLIC_CIVILIAN.get(), "republic_civilian", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> TOGRUTA_CIVILIAN_SPAWN_EGG =
            registerItem("togruta_civilian_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.TOGRUTA_CIVILIAN.get(), "togruta_civilian", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> SEPARATIST_TECHNICIAN_SPAWN_EGG =
            registerItem("separatist_technician_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.SEPARATIST_TECHNICIAN.get(), "separatist_technician", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> MANDALORIAN_CLANSPERSON_SPAWN_EGG =
            registerItem("mandalorian_clansperson_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.MANDALORIAN_CLANSPERSON.get(), "mandalorian_clansperson", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> HUTT_CIVILIAN_SPAWN_EGG =
            registerItem("hutt_civilian_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.HUTT_CIVILIAN.get(), "hutt_civilian", properties));
    public static final RegistrySupplier<RecruitSpawnEggItem> NIGHTSISTER_CIVILIAN_SPAWN_EGG =
            registerItem("nightsister_civilian_spawn_egg",
                    properties -> new RecruitSpawnEggItem(
                            ModEntityTypes.NIGHTSISTER_CIVILIAN.get(), "nightsister_civilian", properties));

    private ModItems() {
    }

    private static Item.Properties blaster(Item.Properties properties, int durability) {
        return properties.durability(durability).component(
                ModDataComponents.BLASTER_HEAT.get(), BlasterHeatPolicy.BlasterHeatState.ready());
    }

    private static RegistrySupplier<LightsaberItem> lightsaber(String colorId) {
        return registerItem(colorId + "_lightsaber", properties -> new LightsaberItem(
                colorId,
                properties.sword(ModEquipmentMaterials.BESKAR.tool(), 5.0F, -2.1F)));
    }

    private static RegistrySupplier<GalacticArmorItem> phaseICloneArmor(String piece, ArmorType armorType) {
        return registerItem("phase_i_clone_" + piece, properties -> new GalacticArmorItem(
                "phase_i_clone",
                properties.humanoidArmor(ModEquipmentMaterials.REPUBLIC.armor(), armorType)));
    }

    public static void register() {
        ITEMS.register();
    }

    private static RegistrySupplier<IdentityChipItem> factionToken(String factionPath) {
        return registerItem(
                factionPath + "_identity_chip",
                properties -> new IdentityChipItem(FactionId.of(factionPath), properties.stacksTo(16)));
    }

    private static RegistrySupplier<VehicleDeploymentKitItem> vehicleKit(
            String vehicleId,
            RegistrySupplier<EntityType<galacticwars.clonewars.vehicle.GalacticVehicleEntity>> type
    ) {
        return registerItem(vehicleId + "_deployment_kit",
                properties -> new VehicleDeploymentKitItem(type::get, vehicleId, properties));
    }

    private static java.util.Map<String, RegistrySupplier<Item>> registerFactionEquipment() {
        java.util.LinkedHashMap<String, RegistrySupplier<Item>> items = new java.util.LinkedHashMap<>();
        registerFamily(items, ModEquipmentMaterials.MANDALORIAN);
        registerFamily(items, ModEquipmentMaterials.NIGHTSISTER_WEAVE);
        registerFamily(items, ModEquipmentMaterials.REPUBLIC);
        registerFamily(items, ModEquipmentMaterials.SEPARATIST);
        registerFamily(items, ModEquipmentMaterials.BESKAR);
        return java.util.Collections.unmodifiableMap(items);
    }

    private static void registerFamily(
            java.util.Map<String, RegistrySupplier<Item>> items,
            ModEquipmentMaterials.EquipmentFamily family
    ) {
        String prefix = family.id();
        items.put(prefix + "_sword", registerSimpleItem(
                prefix + "_sword", properties -> properties.sword(family.tool(), 3.0F, -2.4F)));
        items.put(prefix + "_pickaxe", registerSimpleItem(
                prefix + "_pickaxe", properties -> properties.pickaxe(family.tool(), 1.0F, -2.8F)));
        items.put(prefix + "_axe", registerItem(
                prefix + "_axe", properties -> new AxeItem(family.tool(), 6.0F, -3.1F, properties)));
        items.put(prefix + "_shovel", registerItem(
                prefix + "_shovel", properties -> new ShovelItem(family.tool(), 1.5F, -3.0F, properties)));
        items.put(prefix + "_hoe", registerItem(
                prefix + "_hoe", properties -> new HoeItem(family.tool(), -2.0F, -1.0F, properties)));
        items.put(prefix + "_helmet", registerItem(
                prefix + "_helmet", properties -> new GalacticArmorItem(
                        prefix, properties.humanoidArmor(family.armor(), ArmorType.HELMET))));
        items.put(prefix + "_chestplate", registerItem(
                prefix + "_chestplate", properties -> new GalacticArmorItem(
                        prefix, properties.humanoidArmor(family.armor(), ArmorType.CHESTPLATE))));
        items.put(prefix + "_leggings", registerItem(
                prefix + "_leggings", properties -> new GalacticArmorItem(
                        prefix, properties.humanoidArmor(family.armor(), ArmorType.LEGGINGS))));
        items.put(prefix + "_boots", registerItem(
                prefix + "_boots", properties -> new GalacticArmorItem(
                        prefix, properties.humanoidArmor(family.armor(), ArmorType.BOOTS))));
    }

    private static RegistrySupplier<BlockItem> registerBlockItem(
            String name,
            RegistrySupplier<? extends Block> block
    ) {
        return registerItem(name, properties -> new BlockItem(
                block.get(), properties.useBlockDescriptionPrefix()));
    }

    private static RegistrySupplier<Item> registerSimpleItem(
            String name,
            UnaryOperator<Item.Properties> propertiesFactory
    ) {
        return registerItem(name, properties -> new Item(propertiesFactory.apply(properties)));
    }

    private static <T extends Item> RegistrySupplier<T> registerItem(
            String name,
            Function<Item.Properties, T> factory
    ) {
        return ITEMS.register(name, () -> factory.apply(properties(name)));
    }

    private static Item.Properties properties(String name) {
        return new Item.Properties().setId(ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(GalacticWars.MODID, name)));
    }

}
