package galacticwars.clonewars.menu;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.fabrication.FabricationRecipe;
import galacticwars.clonewars.fabrication.FabricatorBlockEntity;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class FabricatorMenuProvider implements ExtendedMenuProvider {
    private final BlockPos pos;
    private List<String> recipes = List.of();
    private long generation;
    private int technologyRevision;

    public FabricatorMenuProvider(BlockPos pos) {
        this.pos = pos.immutable();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.galacticwars.fabricator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(player.level().getBlockEntity(pos) instanceof FabricatorBlockEntity fabricator)) {
            return null;
        }
        this.recipes = serverPlayer.level().getServer().getRecipeManager().getRecipes().stream()
                .filter(holder -> holder.value() instanceof FabricationRecipe)
                .map(holder -> holder.id().identifier().toString())
                .sorted(Comparator.naturalOrder())
                .limit(FabricatorMenu.MAX_RECIPE_IDS)
                .toList();
        this.generation = GameplayDataManager.generation();
        this.technologyRevision = KingdomSavedData.get(serverPlayer.level())
                .kingdomForPlayer(player.getUUID())
                .map(kingdom -> KingdomSavedData.get(serverPlayer.level())
                        .technologyStateOrDefault(kingdom.id()).revision())
                .orElse(0);
        return new FabricatorMenu(
                id, inventory, fabricator, pos, recipes, generation, technologyRevision);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(recipes.size());
        recipes.forEach(id -> buffer.writeUtf(id, 128));
        buffer.writeVarLong(generation);
        buffer.writeVarInt(technologyRevision);
    }
}
