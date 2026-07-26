package galacticwars.clonewars.fabrication;

import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.menu.FabricatorMenu;
import galacticwars.clonewars.network.FabricationRequestPayload;
import galacticwars.clonewars.progression.GameplayAccessResolver;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class FabricationService {
    private FabricationService() {
    }

    public static FabricationResult fabricate(
            ServerPlayer player,
            FabricatorMenu menu,
            FabricationRequestPayload request
    ) {
        if (player.containerMenu != menu || menu.containerId != request.containerId()
                || !menu.stillValid(player)) {
            return reject(player, "invalid_container");
        }
        if (request.catalogGeneration() != GameplayDataManager.generation()) {
            return reject(player, "stale_catalog");
        }
        int revision = KingdomSavedData.get(player.level()).kingdomForPlayer(player.getUUID())
                .map(kingdom -> KingdomSavedData.get(player.level())
                        .technologyStateOrDefault(kingdom.id()).revision())
                .orElse(-1);
        if (request.technologyRevision() != revision) {
            return reject(player, "stale_technology");
        }
        if (!(player.level().getBlockEntity(menu.blockPos()) instanceof FabricatorBlockEntity fabricator)
                || menu.container() != fabricator) {
            return reject(player, "invalid_fabricator");
        }
        if (fabricator.processed(request.replayId())) {
            return reject(player, "replay");
        }
        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key;
        try {
            key = ResourceKey.create(Registries.RECIPE, Identifier.parse(request.recipeId()));
        } catch (RuntimeException exception) {
            return reject(player, "invalid_recipe");
        }
        var holder = player.level().getServer().getRecipeManager().byKey(key).orElse(null);
        if (holder == null || !(holder.value() instanceof FabricationRecipe recipe)) {
            return reject(player, "invalid_recipe");
        }
        GameplayAccessResolver.AccessDecision access = GameplayAccessResolver.fabrication(
                player,
                request.recipeId(),
                recipe.faction(),
                recipe.technology(),
                recipe.exportable(),
                true,
                true);
        if (!access.allowed()) {
            return reject(player, access.reason());
        }
        FabricationInput input = fabricator.input();
        int[] removals = recipe.removals(input);
        ItemStack output = recipe.assemble(input);
        if (removals.length != FabricatorBlockEntity.INPUT_SLOTS || output.isEmpty()
                || !fabricator.fabricate(request.replayId(), removals, output)) {
            return reject(player, removals.length == 0 ? "missing_ingredients" : "output_blocked");
        }
        player.sendSystemMessage(Component.translatable(
                "message.galacticwars.fabrication.success", output.getHoverName()));
        return new FabricationResult(true, "", output.copy());
    }

    private static FabricationResult reject(ServerPlayer player, String reason) {
        player.sendSystemMessage(Component.translatable(
                "message.galacticwars.fabrication.rejected",
                Component.translatable("message.galacticwars.fabrication.reason." + reason)));
        return new FabricationResult(false, reason, ItemStack.EMPTY);
    }

    public record FabricationResult(boolean accepted, String reason, ItemStack output) {
    }
}
