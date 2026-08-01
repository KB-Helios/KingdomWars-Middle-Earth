package galacticwars.clonewars.menu;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class RecruitCommandMenuProvider implements ExtendedMenuProvider {
    private final GalacticRecruitEntity recruit;
    private boolean preparedArmyCommandAccess;
    private boolean preparedLogisticsAccess;
    private boolean preparedWorksiteAccess;

    public RecruitCommandMenuProvider(GalacticRecruitEntity recruit) {
        this.recruit = recruit;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.galacticwars.recruit_command");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        RecruitCommandMenu menu = new RecruitCommandMenu(
                containerId, playerInventory, this.recruit.getId());
        preparedArmyCommandAccess = menu.armyCommandAccess();
        preparedLogisticsAccess = menu.logisticsAccess();
        preparedWorksiteAccess = menu.worksiteAccess();
        return menu;
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.recruit.getId());
        buffer.writeBoolean(preparedArmyCommandAccess);
        buffer.writeBoolean(preparedLogisticsAccess);
        buffer.writeBoolean(preparedWorksiteAccess);
    }
}
