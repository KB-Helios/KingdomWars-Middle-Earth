package galacticwars.clonewars.client.gui;

import galacticwars.clonewars.menu.CommandCenterNavigationMenu;
import galacticwars.clonewars.world.PlanetTravelService;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

public final class CommandCenterNavigationScreen extends Screen implements MenuAccess<CommandCenterNavigationMenu> {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 6;
    private final CommandCenterNavigationMenu menu;
    private Component status = Component.translatable("screen.galacticwars.navigation.ready");

    public CommandCenterNavigationScreen(
            CommandCenterNavigationMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(title);
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = Math.min(BUTTON_WIDTH, Math.max(80, this.width - 24));
        int x = (this.width - buttonWidth) / 2;
        int firstY = Math.max(54, (this.height - menu.destinations().size()
                * (BUTTON_HEIGHT + GAP)) / 2);
        for (int index = 0; index < menu.destinations().size(); index++) {
            var destination = menu.destinations().get(index);
            String destinationId = destination.destinationId();
            int buttonId = index;
            Button destinationButton = Button.builder(
                            destinationLabel(destination),
                            button -> this.selectDestination(buttonId))
                    .bounds(x, firstY + index * (BUTTON_HEIGHT + GAP), buttonWidth, BUTTON_HEIGHT)
                    .build();
            destinationButton.active = destination.available();
            if (!destination.available()) {
                destinationButton.setTooltip(Tooltip.create(Component.translatable(
                        "message.galacticwars.travel." + destination.reason())));
            } else if (!PlanetTravelService.HOME_DESTINATION_ID.equals(destinationId)) {
                destinationButton.setTooltip(Tooltip.create(Component.translatable(
                        "screen.galacticwars.navigation.arrival_profile",
                        Component.translatable("arrival.galacticwars." + destination.arrivalProfile()))));
            }
            this.addRenderableWidget(destinationButton);
        }
    }

    private void selectDestination(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
            this.status = Component.translatable("screen.galacticwars.navigation.request_sent");
        }
    }

    private static Component destinationLabel(
            galacticwars.clonewars.world.PlanetTravelService.NavigationDestination destination
    ) {
        Component name = CommandCenterNavigationMenu.destinationName(destination.destinationId());
        if (galacticwars.clonewars.world.PlanetTravelService.HOME_DESTINATION_ID.equals(
                destination.destinationId())) {
            return name;
        }
        return Component.translatable(
                "screen.galacticwars.navigation.destination_with_theme",
                name, Component.translatable("theme.galacticwars." + destination.theme()));
    }

    @Override
    public CommandCenterNavigationMenu getMenu() {
        return menu;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        drawWrappedCentered(graphics, this.title, 12, 0xE5F6FF, 1);
        Component hint = Component.translatable("screen.galacticwars.navigation.hint");
        drawWrappedCentered(graphics, hint, 25, 0x9CA3AF, 2);
        int statusLines = Math.max(1, Math.min(3,
                this.font.split(this.status, Math.max(40, this.width - 24)).size()));
        drawWrappedCentered(graphics, this.status,
                this.height - 7 - statusLines * 10, 0xFFE6C77A, 3);
    }

    private void drawWrappedCentered(
            GuiGraphicsExtractor graphics,
            Component text,
            int y,
            int color,
            int maximumLines
    ) {
        int lineY = y;
        int rendered = 0;
        for (FormattedCharSequence line : this.font.split(text, Math.max(40, this.width - 24))) {
            if (rendered++ >= maximumLines) {
                break;
            }
            graphics.text(this.font, line, (this.width - this.font.width(line)) / 2, lineY, color);
            lineY += 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        }
        super.onClose();
    }
}
