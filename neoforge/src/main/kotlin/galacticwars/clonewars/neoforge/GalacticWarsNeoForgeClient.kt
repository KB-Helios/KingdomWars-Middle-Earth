package galacticwars.clonewars.neoforge

import dev.architectury.platform.Platform
import dev.architectury.platform.client.ConfigurationScreenRegistry
import galacticwars.clonewars.GalacticWars
import galacticwars.clonewars.GalacticWarsClient
import galacticwars.clonewars.client.ArmyFieldCommandKeyMappings
import galacticwars.clonewars.client.ClassKeyMappings
import galacticwars.clonewars.client.ForceKeyMappings
import galacticwars.clonewars.client.gui.BlasterHeatHud
import galacticwars.clonewars.client.gui.ClassHud
import galacticwars.clonewars.client.gui.CommandCenterNavigationScreen
import galacticwars.clonewars.client.gui.CommandCenterOperationsScreen
import galacticwars.clonewars.client.gui.FabricatorScreen
import galacticwars.clonewars.client.gui.FactionSelectionScreen
import galacticwars.clonewars.client.gui.ForceHud
import galacticwars.clonewars.client.gui.ObjectiveMarkerHud
import galacticwars.clonewars.client.gui.GalacticWarsConfigScreen
import galacticwars.clonewars.client.gui.MerchantTradeScreen
import galacticwars.clonewars.client.gui.RecruitCommandScreen
import galacticwars.clonewars.client.gui.RecruitLoadoutScreen
import galacticwars.clonewars.client.gui.StarterCampSetupScreen
import galacticwars.clonewars.client.gui.VehicleHud
import galacticwars.clonewars.client.render.BlasterClientExtensions
import galacticwars.clonewars.client.render.LightsaberClientExtensions
import galacticwars.clonewars.registry.ModEntityTypes
import galacticwars.clonewars.registry.ModItems
import galacticwars.clonewars.registry.ModMenuTypes
import net.minecraft.resources.Identifier
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent
import net.neoforged.neoforge.client.gui.VanillaGuiLayers

@EventBusSubscriber(modid = GalacticWars.MODID, value = [Dist.CLIENT])
object GalacticWarsNeoForgeClient {
    @SubscribeEvent
    fun registerKeyMappings(event: RegisterKeyMappingsEvent) {
        ForceKeyMappings.mappings().forEach { event.register(it) }
        ClassKeyMappings.mappings().forEach { event.register(it) }
        ArmyFieldCommandKeyMappings.mappings().forEach { event.register(it) }
    }

    @SubscribeEvent
    fun registerMenuScreens(event: RegisterMenuScreensEvent) {
        event.register(ModMenuTypes.RECRUIT_COMMAND.get(), ::RecruitCommandScreen)
        event.register(ModMenuTypes.RECRUIT_LOADOUT.get(), ::RecruitLoadoutScreen)
        event.register(
            ModMenuTypes.COMMAND_CENTER_NAVIGATION.get(),
            ::CommandCenterNavigationScreen,
        )
        event.register(ModMenuTypes.FACTION_SELECTION.get(), ::FactionSelectionScreen)
        event.register(ModMenuTypes.STARTER_CAMP_SETUP.get(), ::StarterCampSetupScreen)
        event.register(ModMenuTypes.MERCHANT_TRADE.get(), ::MerchantTradeScreen)
        event.register(
            ModMenuTypes.COMMAND_CENTER_OPERATIONS.get(),
            ::CommandCenterOperationsScreen,
        )
        event.register(ModMenuTypes.FABRICATOR.get(), ::FabricatorScreen)
    }

    @SubscribeEvent
    fun registerEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(
            ModEntityTypes.BLASTER_BOLT.get(),
            GalacticWarsClient.blasterBoltRenderer(),
        )
        ModEntityTypes.recruits().forEach { holder ->
            event.registerEntityRenderer(holder.get(), GalacticWarsClient.recruitRenderer(holder))
        }
        ModEntityTypes.vehicles().forEach { holder ->
            event.registerEntityRenderer(holder.get(), GalacticWarsClient.vehicleRenderer(holder))
        }
    }

    @SubscribeEvent
    fun clientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            GalacticWarsClient.initRuntime()
            ConfigurationScreenRegistry.register(Platform.getMod(GalacticWars.MODID)) { parent ->
                GalacticWarsConfigScreen.create(parent)
            }
        }
    }

    @SubscribeEvent
    fun registerClientExtensions(event: RegisterClientExtensionsEvent) {
        event.registerItem(
            BlasterClientExtensions.INSTANCE,
            ModItems.DC15_BLASTER.get(),
            ModItems.E5_BLASTER.get(),
            ModItems.WESTAR_BLASTER.get(),
            ModItems.SCATTER_BLASTER.get(),
        )
        event.registerItem(
            LightsaberClientExtensions.INSTANCE,
            ModItems.BLUE_LIGHTSABER.get(),
            ModItems.GREEN_LIGHTSABER.get(),
            ModItems.RED_LIGHTSABER.get(),
            ModItems.PURPLE_LIGHTSABER.get(),
            ModItems.YELLOW_LIGHTSABER.get(),
            ModItems.WHITE_LIGHTSABER.get(),
        )
    }

    @SubscribeEvent
    fun registerGuiLayers(event: RegisterGuiLayersEvent) {
        event.registerAbove(
            VanillaGuiLayers.HOTBAR,
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "blaster_heat"),
        ) { graphics, _ -> BlasterHeatHud.render(graphics) }
        event.registerAbove(
            VanillaGuiLayers.HOTBAR,
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "force_hud"),
        ) { graphics, _ -> ForceHud.render(graphics) }
        event.registerAbove(
            VanillaGuiLayers.HOTBAR,
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "class_hud"),
        ) { graphics, _ -> ClassHud.render(graphics) }
        event.registerAbove(
            VanillaGuiLayers.HOTBAR,
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "vehicle_hud"),
        ) { graphics, _ -> VehicleHud.render(graphics) }
        event.registerAbove(
            VanillaGuiLayers.HOTBAR,
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "objective_marker"),
        ) { graphics, _ -> ObjectiveMarkerHud.render(graphics) }
    }
}
