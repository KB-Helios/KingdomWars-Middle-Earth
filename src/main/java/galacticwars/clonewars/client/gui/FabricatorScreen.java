package galacticwars.clonewars.client.gui;

import galacticwars.clonewars.menu.FabricatorMenu;
import galacticwars.clonewars.network.FabricationRequestPayload;
import galacticwars.clonewars.network.GalacticNetwork;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Recipe discovery is informational; every button delegates to the server fabrication authority. */
public final class FabricatorScreen extends AbstractContainerScreen<FabricatorMenu> {
    private static final int IMAGE_WIDTH = 376;
    private static final int IMAGE_HEIGHT = 166;
    private static final int PAGE_SIZE = 6;
    private int page;

    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
        this.inventoryLabelY = 73;
    }

    @Override
    protected void init() {
        super.init();
        int start = page * PAGE_SIZE;
        int end = Math.min(menu.recipeIds().size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            String recipeId = menu.recipeIds().get(index);
            addRenderableWidget(Button.builder(
                            Component.literal(shortId(recipeId)),
                            button -> GalacticNetwork.CHANNEL.sendToServer(
                                    new FabricationRequestPayload(
                                            UUID.randomUUID(),
                                            menu.containerId,
                                            recipeId,
                                            menu.catalogGeneration(),
                                            menu.technologyRevision())))
                    .bounds(leftPos + 198, topPos + 18 + (index - start) * 20, 170, 18)
                    .build());
        }
        int pages = Math.max(1, (menu.recipeIds().size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (pages > 1) {
            Button previous = Button.builder(Component.literal("<"), button -> {
                page = Math.max(0, page - 1);
                rebuildWidgets();
            }).bounds(leftPos + 198, topPos + 141, 30, 18).build();
            previous.active = page > 0;
            addRenderableWidget(previous);
            Button next = Button.builder(
                    Component.literal((page + 1) + "/" + pages + " >"),
                    button -> {
                        page = Math.min(pages - 1, page + 1);
                        rebuildWidgets();
                    }).bounds(leftPos + 230, topPos + 141, 138, 18).build();
            next.active = page + 1 < pages;
            addRenderableWidget(next);
        }
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        graphics.fill(0, 0, width, height, 0x98070B12);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF018202B);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, width, height, 0x98070B12);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF018202B);
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF65788C);
            graphics.fill(x, y, x + 16, y + 16, 0xFF0D141D);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFFE8EEF5, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFAAB7C4, false);
        graphics.text(font, Component.translatable("screen.galacticwars.fabricator.server_authority"),
                198, 6, 0xFFFFD36A, false);
    }

    private static String shortId(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? id : id.substring(separator + 1);
    }
}
