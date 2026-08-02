package galacticwars.clonewars.menu;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class WorksiteConfigurationMenuProvider implements ExtendedMenuProvider {
    private final GalacticRecruitEntity recruit;
    private final Optional<BlockPos> commandCenterAnchor;
    private WorksiteConfigurationSnapshot preparedSnapshot;

    public WorksiteConfigurationMenuProvider(GalacticRecruitEntity recruit) {
        this(recruit, Optional.empty());
    }

    public WorksiteConfigurationMenuProvider(
            GalacticRecruitEntity recruit,
            BlockPos commandCenterAnchor
    ) {
        this(recruit, Optional.of(commandCenterAnchor.immutable()));
    }

    private WorksiteConfigurationMenuProvider(
            GalacticRecruitEntity recruit,
            Optional<BlockPos> commandCenterAnchor
    ) {
        this.recruit = recruit;
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
                recruit,
                commandCenterAnchor);
        preparedSnapshot = menu.snapshot();
        return menu;
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buffer) {
        if (preparedSnapshot == null) {
            throw new IllegalStateException("worksite menu data requested before menu creation");
        }
        WorksiteConfigurationSnapshot.write(buffer, preparedSnapshot);
        buffer.writeBoolean(commandCenterAnchor.isPresent());
        commandCenterAnchor.ifPresent(buffer::writeBlockPos);
    }
}
