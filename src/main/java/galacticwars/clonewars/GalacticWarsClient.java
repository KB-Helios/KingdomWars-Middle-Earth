package galacticwars.clonewars;

import dev.architectury.registry.client.gui.MenuScreenRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.event.events.client.ClientPlayerEvent;
import galacticwars.clonewars.client.ForceClientState;
import galacticwars.clonewars.client.ForceKeyMappings;
import galacticwars.clonewars.client.ArmyFieldCommandKeyMappings;
import galacticwars.clonewars.client.ClassClientState;
import galacticwars.clonewars.client.ClassKeyMappings;
import galacticwars.clonewars.client.ClientGameplayCatalog;
import galacticwars.clonewars.client.FieldCommandClientState;
import galacticwars.clonewars.client.gui.CommandCenterNavigationScreen;
import galacticwars.clonewars.client.gui.CommandCenterOperationsScreen;
import galacticwars.clonewars.client.gui.FactionSelectionScreen;
import galacticwars.clonewars.client.gui.MerchantTradeScreen;
import galacticwars.clonewars.client.gui.RecruitCommandScreen;
import galacticwars.clonewars.client.gui.RecruitLoadoutScreen;
import galacticwars.clonewars.client.gui.StarterCampSetupScreen;
import galacticwars.clonewars.client.gui.FabricatorScreen;
import galacticwars.clonewars.client.render.GalacticRecruitRenderer;
import galacticwars.clonewars.client.render.GalacticVehicleRenderer;
import galacticwars.clonewars.combat.BlasterBoltEntity;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.network.ClientPacketBridge;
import galacticwars.clonewars.registry.ModEntityTypes;
import galacticwars.clonewars.registry.ModMenuTypes;
import galacticwars.clonewars.vehicle.GalacticVehicleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/** Client-only shared registration invoked exclusively by loader client entrypoints. */
public final class GalacticWarsClient {
    private static final AtomicBoolean RUNTIME_INITIALIZED = new AtomicBoolean();
    private static final AtomicBoolean ARCHITECTURY_BINDINGS_INITIALIZED = new AtomicBoolean();

    private GalacticWarsClient() {
    }

    /**
     * Installs the complete Architectury client surface. Fabric invokes this from its client
     * initializer, where renderer, key, and menu registries are still open.
     */
    public static void init() {
        initRuntime();
        if (!ARCHITECTURY_BINDINGS_INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        ForceKeyMappings.register();
        ClassKeyMappings.register();
        ArmyFieldCommandKeyMappings.register();

        EntityRendererRegistry.register(
                ModEntityTypes.BLASTER_BOLT,
                blasterBoltRenderer());
        ModEntityTypes.recruits().forEach(holder -> EntityRendererRegistry.register(
                holder,
                recruitRenderer(holder)));
        ModEntityTypes.vehicles().forEach(holder -> EntityRendererRegistry.register(
                holder,
                vehicleRenderer(holder)));

        MenuScreenRegistry.registerScreenFactory(
                ModMenuTypes.RECRUIT_COMMAND.get(), RecruitCommandScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                ModMenuTypes.RECRUIT_LOADOUT.get(), RecruitLoadoutScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                ModMenuTypes.COMMAND_CENTER_NAVIGATION.get(), CommandCenterNavigationScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                ModMenuTypes.FACTION_SELECTION.get(), FactionSelectionScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                ModMenuTypes.STARTER_CAMP_SETUP.get(), StarterCampSetupScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                ModMenuTypes.MERCHANT_TRADE.get(), MerchantTradeScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                ModMenuTypes.COMMAND_CENTER_OPERATIONS.get(), CommandCenterOperationsScreen::new);
        MenuScreenRegistry.registerScreenFactory(
                ModMenuTypes.FABRICATOR.get(), FabricatorScreen::new);
    }

    /**
     * Installs loader-neutral client state without attaching lifecycle-sensitive registries.
     * NeoForge invokes this from client setup and owns those registrations through its direct
     * mod-bus event handlers.
     */
    public static void initRuntime() {
        if (!RUNTIME_INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        galacticwars.clonewars.client.ClientConfig.load();
        ClientPacketBridge.installForceHudHandler(ForceClientState::update);
        ClientPacketBridge.installForceProgressionHandler(
                galacticwars.clonewars.client.gui.ForceProgressionScreen::open);
        ClientPacketBridge.installClassHudHandler(ClassClientState::update);
        ClientPacketBridge.installGameplayCatalogHandler(ClientGameplayCatalog::replace);
        ClientPacketBridge.installObjectiveMarkerHandler(
                galacticwars.clonewars.client.ObjectiveMarkerClientState::update);
        ClientPacketBridge.installServerPolicyHandler(
                galacticwars.clonewars.client.ServerPolicyClientState::update);
        ClientPacketBridge.installFieldCommandStateHandler(FieldCommandClientState::update);
        ClientPacketBridge.installFieldCommandOpenHandler(ArmyFieldCommandKeyMappings::openCommandScreen);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            ClientGameplayCatalog.clear();
            FieldCommandClientState.clear();
            galacticwars.clonewars.client.ObjectiveMarkerClientState.clear();
            galacticwars.clonewars.client.ServerPolicyClientState.clear();
        });
        ForceKeyMappings.registerTickHandler();
        ClassKeyMappings.registerTickHandler();
        ArmyFieldCommandKeyMappings.registerTickHandler();

        GalacticWars.LOGGER.info("Galactic Wars: Clone Wars client foundation loaded.");
    }

    public static EntityRendererProvider<BlasterBoltEntity> blasterBoltRenderer() {
        return context -> new ThrownItemRenderer<>(context, 1.0F, true);
    }

    public static EntityRendererProvider<GalacticRecruitEntity> recruitRenderer(
            Supplier<EntityType<GalacticRecruitEntity>> holder
    ) {
        return context -> new GalacticRecruitRenderer<>(context, holder.get());
    }

    public static EntityRendererProvider<GalacticVehicleEntity> vehicleRenderer(
            Supplier<EntityType<GalacticVehicleEntity>> holder
    ) {
        return context -> new GalacticVehicleRenderer<>(context, holder.get());
    }
}
