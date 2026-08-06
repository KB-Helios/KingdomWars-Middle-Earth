package galacticwars.clonewars.menu;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class WorksiteConfigurationMenuProvider implements ExtendedMenuProvider {
    private final WorksiteConfigurationSnapshot preparedSnapshot;
    private final Optional<BlockPos> commandCenterAnchor;

    public static Optional<WorksiteConfigurationMenuProvider> prepare(
            ServerPlayer player,
            GalacticRecruitEntity recruit
    ) {
        return prepare(player, recruit, Optional.empty());
    }

    public static Optional<WorksiteConfigurationMenuProvider> prepare(
            ServerPlayer player,
            GalacticRecruitEntity recruit,
            BlockPos commandCenterAnchor
    ) {
        return prepare(
                player,
                recruit,
                Optional.of(Objects.requireNonNull(
                        commandCenterAnchor, "commandCenterAnchor").immutable()));
    }

    private static Optional<WorksiteConfigurationMenuProvider> prepare(
            ServerPlayer player,
            GalacticRecruitEntity recruit,
            Optional<BlockPos> commandCenterAnchor
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(recruit, "recruit");
        return WorksiteConfigurationMenu.capture(player, recruit, "ready")
                .map(snapshot -> new WorksiteConfigurationMenuProvider(
                        snapshot, commandCenterAnchor));
    }

    private WorksiteConfigurationMenuProvider(
            WorksiteConfigurationSnapshot preparedSnapshot,
            Optional<BlockPos> commandCenterAnchor
    ) {
        this.preparedSnapshot = Objects.requireNonNull(preparedSnapshot, "preparedSnapshot");
        this.commandCenterAnchor = commandCenterAnchor;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.galacticwars.worksite_configuration");
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {
        WorksiteConfigurationMenu menu = new WorksiteConfigurationMenu(
                containerId,
                playerInventory,
                preparedSnapshot,
                commandCenterAnchor);
        return menu;
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buffer) {
        WorksiteConfigurationSnapshot.write(buffer, preparedSnapshot);
        buffer.writeBoolean(commandCenterAnchor.isPresent());
        commandCenterAnchor.ifPresent(buffer::writeBlockPos);
    }
}
