package galacticwars.clonewars.registry;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.menu.CommandCenterNavigationMenu;
import galacticwars.clonewars.menu.CommandCenterOperationsMenu;
import galacticwars.clonewars.menu.FactionSelectionMenu;
import galacticwars.clonewars.menu.MerchantTradeMenu;
import galacticwars.clonewars.menu.RecruitCommandMenu;
import galacticwars.clonewars.menu.RecruitLoadoutMenu;
import galacticwars.clonewars.menu.StarterCampSetupMenu;
import galacticwars.clonewars.menu.FabricatorMenu;
import galacticwars.clonewars.menu.WorksiteConfigurationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(GalacticWars.MODID, Registries.MENU);

    public static final RegistrySupplier<MenuType<RecruitCommandMenu>> RECRUIT_COMMAND =
            MENU_TYPES.register("recruit_command", () -> MenuRegistry.ofExtended(
                    (id, inventory, buffer) -> new RecruitCommandMenu(
                            id, inventory, registryBuffer(buffer, inventory))));
    public static final RegistrySupplier<MenuType<RecruitLoadoutMenu>> RECRUIT_LOADOUT =
            MENU_TYPES.register("recruit_loadout", () -> MenuRegistry.ofExtended(
                    (id, inventory, buffer) -> new RecruitLoadoutMenu(
                            id, inventory, registryBuffer(buffer, inventory))));
    public static final RegistrySupplier<MenuType<CommandCenterNavigationMenu>> COMMAND_CENTER_NAVIGATION =
            MENU_TYPES.register("command_center_navigation",
                    () -> MenuRegistry.ofExtended((id, inventory, buffer) ->
                            new CommandCenterNavigationMenu(
                                    id, inventory, registryBuffer(buffer, inventory))));
    public static final RegistrySupplier<MenuType<FactionSelectionMenu>> FACTION_SELECTION =
            MENU_TYPES.register("faction_selection",
                    () -> MenuRegistry.ofExtended((id, inventory, buffer) ->
                            new FactionSelectionMenu(
                                    id, inventory, registryBuffer(buffer, inventory))));
    public static final RegistrySupplier<MenuType<StarterCampSetupMenu>> STARTER_CAMP_SETUP =
            MENU_TYPES.register("starter_camp_setup",
                    () -> MenuRegistry.ofExtended((id, inventory, buffer) ->
                            new StarterCampSetupMenu(
                                    id, inventory, registryBuffer(buffer, inventory))));
    public static final RegistrySupplier<MenuType<MerchantTradeMenu>> MERCHANT_TRADE =
            MENU_TYPES.register("merchant_trade",
                    () -> MenuRegistry.ofExtended((id, inventory, buffer) ->
                            new MerchantTradeMenu(
                                    id, inventory, registryBuffer(buffer, inventory))));
    public static final RegistrySupplier<MenuType<CommandCenterOperationsMenu>> COMMAND_CENTER_OPERATIONS =
            MENU_TYPES.register("command_center_operations",
                    () -> MenuRegistry.ofExtended((id, inventory, buffer) ->
                            new CommandCenterOperationsMenu(
                                    id, inventory, registryBuffer(buffer, inventory))));
    public static final RegistrySupplier<MenuType<FabricatorMenu>> FABRICATOR =
            MENU_TYPES.register("fabricator",
                    () -> MenuRegistry.ofExtended((id, inventory, buffer) ->
                            new FabricatorMenu(id, inventory, registryBuffer(buffer, inventory))));
    public static final RegistrySupplier<MenuType<WorksiteConfigurationMenu>> WORKSITE_CONFIGURATION =
            MENU_TYPES.register("worksite_configuration",
                    () -> MenuRegistry.ofExtended((id, inventory, buffer) ->
                            new WorksiteConfigurationMenu(
                                    id, inventory, registryBuffer(buffer, inventory))));

    private ModMenuTypes() {
    }

    public static void register() {
        MENU_TYPES.register();
    }

    private static RegistryFriendlyByteBuf registryBuffer(
            FriendlyByteBuf buffer,
            Inventory inventory
    ) {
        return new RegistryFriendlyByteBuf(buffer, inventory.player.registryAccess());
    }
}
