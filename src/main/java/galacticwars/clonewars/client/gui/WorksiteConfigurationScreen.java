package galacticwars.clonewars.client.gui;

import galacticwars.clonewars.menu.WorksiteConfigurationAction;
import galacticwars.clonewars.menu.WorksiteConfigurationMenu;
import galacticwars.clonewars.menu.WorksiteConfigurationSnapshot;
import galacticwars.clonewars.network.GalacticNetwork;
import galacticwars.clonewars.network.WorksiteActionPayload;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Accessible in-world editor for the bounded, authoritative worksite projection. */
public final class WorksiteConfigurationScreen extends Screen
        implements MenuAccess<WorksiteConfigurationMenu> {
    private static final int BUTTON_WIDTH = 106;
    private static final int BUTTON_HEIGHT = 18;
    private static final int GAP = 3;
    private static final int COLUMNS = 3;
    private static final int INFO_COLOR = 0xE5E7EB;
    private static final int MUTED_COLOR = 0x9CA3AF;
    private static final int ACCENT_COLOR = 0x67E8F9;
    private static final int WARNING_COLOR = 0xFBBF24;

    private final WorksiteConfigurationMenu menu;
    private WorksiteConfigurationSnapshot renderedSnapshot;
    private int filterIndex;
    private int routeIndex;
    private Component localFeedback = Component.translatable(
            "screen.galacticwars.worksite.feedback.ready");

    public WorksiteConfigurationScreen(
            WorksiteConfigurationMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(title);
        this.menu = menu;
        this.renderedSnapshot = menu.snapshot();
    }

    @Override
    protected void init() {
        super.init();
        WorksiteConfigurationSnapshot snapshot = menu.snapshot();
        clampSelections(snapshot);
        int gridWidth = BUTTON_WIDTH * COLUMNS + GAP * (COLUMNS - 1);
        int startX = (width - gridWidth) / 2;
        int startY = Math.max(82, (height - 8 * (BUTTON_HEIGHT + GAP)) / 2 + 24);
        List<ActionButton> actions = List.of(
                action("screen.galacticwars.worksite.width.decrease",
                        WorksiteConfigurationAction.WIDTH_DECREASE),
                action("screen.galacticwars.worksite.width.increase",
                        WorksiteConfigurationAction.WIDTH_INCREASE),
                action("screen.galacticwars.worksite.height.decrease",
                        WorksiteConfigurationAction.HEIGHT_DECREASE),
                action("screen.galacticwars.worksite.height.increase",
                        WorksiteConfigurationAction.HEIGHT_INCREASE),
                action("screen.galacticwars.worksite.depth.decrease",
                        WorksiteConfigurationAction.DEPTH_DECREASE),
                action("screen.galacticwars.worksite.depth.increase",
                        WorksiteConfigurationAction.DEPTH_INCREASE),
                action("screen.galacticwars.worksite.priority.decrease",
                        WorksiteConfigurationAction.PRIORITY_DECREASE),
                action("screen.galacticwars.worksite.priority.increase",
                        WorksiteConfigurationAction.PRIORITY_INCREASE),
                action("screen.galacticwars.worksite.access.toggle",
                        WorksiteConfigurationAction.TOGGLE_KINGDOM_ACCESS),
                action("screen.galacticwars.worksite.overlay.toggle",
                        WorksiteConfigurationAction.TOGGLE_OVERLAY),
                action("screen.galacticwars.worksite.dispatch.cycle",
                        WorksiteConfigurationAction.CYCLE_DISPATCH_MODE),
                action("screen.galacticwars.worksite.route_mode.cycle",
                        WorksiteConfigurationAction.CYCLE_ROUTE_MODE),
                action("screen.galacticwars.worksite.filter.held",
                        WorksiteConfigurationAction.ADD_HELD_ITEM_FILTER),
                action("screen.galacticwars.worksite.filter.block",
                        WorksiteConfigurationAction.ADD_LOOKED_BLOCK_FILTER),
                action("screen.galacticwars.worksite.filter.entity",
                        WorksiteConfigurationAction.ADD_LOOKED_ENTITY_FILTER),
                indexedAction("screen.galacticwars.worksite.filter.remove",
                        WorksiteConfigurationAction.REMOVE_FILTER, () -> filterIndex),
                action("screen.galacticwars.worksite.filter.clear",
                        WorksiteConfigurationAction.CLEAR_FILTERS),
                action("screen.galacticwars.worksite.storage.set",
                        WorksiteConfigurationAction.SET_STORAGE_FROM_LOOK),
                action("screen.galacticwars.worksite.route.take",
                        WorksiteConfigurationAction.ADD_ROUTE_TAKE_FROM_LOOK),
                action("screen.galacticwars.worksite.route.put",
                        WorksiteConfigurationAction.ADD_ROUTE_PUT_FROM_LOOK),
                indexedAction("screen.galacticwars.worksite.route.remove",
                        WorksiteConfigurationAction.REMOVE_ROUTE_WAYPOINT, () -> routeIndex),
                action("screen.galacticwars.worksite.route.clear",
                        WorksiteConfigurationAction.CLEAR_ROUTE));
        for (int index = 0; index < actions.size(); index++) {
            ActionButton action = actions.get(index);
            int x = startX + (index % COLUMNS) * (BUTTON_WIDTH + GAP);
            int y = startY + (index / COLUMNS) * (BUTTON_HEIGHT + GAP);
            addRenderableWidget(Button.builder(
                            Component.translatable(action.translationKey()),
                            button -> send(action.action(), action.selectedIndex().getAsInt()))
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        }

        int selectorY = Math.max(60, startY - 20);
        addRenderableWidget(Button.builder(
                        Component.literal("<"),
                        button -> {
                            filterIndex = cycle(filterIndex, -1, snapshot.itemFilters().size());
                        })
                .bounds(startX, selectorY, 20, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal(">"),
                        button -> {
                            filterIndex = cycle(filterIndex, 1, menu.snapshot().itemFilters().size());
                        })
                .bounds(startX + 22, selectorY, 20, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("<"),
                        button -> {
                            routeIndex = cycle(routeIndex, -1, menu.snapshot().route().size());
                        })
                .bounds(startX + gridWidth - 42, selectorY, 20, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal(">"),
                        button -> {
                            routeIndex = cycle(routeIndex, 1, menu.snapshot().route().size());
                        })
                .bounds(startX + gridWidth - 20, selectorY, 20, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public WorksiteConfigurationMenu getMenu() {
        return menu;
    }

    @Override
    public void tick() {
        super.tick();
        WorksiteConfigurationSnapshot current = menu.snapshot();
        if (!current.equals(renderedSnapshot)) {
            renderedSnapshot = current;
            clampSelections(current);
            localFeedback = Component.translatable(
                    "screen.galacticwars.worksite.feedback." + current.feedbackCode());
            rebuildWidgets();
        }
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        WorksiteConfigurationSnapshot state = menu.snapshot();
        Component heading = Component.translatable(
                "screen.galacticwars.worksite.title",
                state.recruitName());
        graphics.text(font, heading, (width - font.width(heading)) / 2, 8, ACCENT_COLOR);
        Component status = Component.translatable(
                "screen.galacticwars.worksite.status",
                state.professionId(),
                state.phase(),
                state.reasonCode(),
                state.completedQuantity(),
                state.totalQuantity());
        graphics.text(font, status, (width - font.width(status)) / 2, 21, INFO_COLOR);
        Component geometry = Component.translatable(
                "screen.galacticwars.worksite.geometry",
                state.centerX(),
                state.centerY(),
                state.centerZ(),
                state.bounds().width(),
                state.bounds().height(),
                state.bounds().depth(),
                state.priority(),
                state.configurationRevision());
        graphics.text(font, geometry, (width - font.width(geometry)) / 2, 33, MUTED_COLOR);
        String storage = state.storageTarget()
                .map(pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ())
                .orElse("-");
        Component controls = Component.translatable(
                "screen.galacticwars.worksite.settings",
                state.kingdomAccess(),
                state.overlayVisible(),
                state.dispatchMode().id(),
                state.routeMode().id(),
                storage,
                state.requiredResource().isBlank() ? "-" : state.requiredResource());
        graphics.text(font, controls, (width - font.width(controls)) / 2, 45, MUTED_COLOR);

        Component selectedFilter = Component.translatable(
                "screen.galacticwars.worksite.filter.selected",
                selectedFilter(state),
                state.itemFilters().size());
        graphics.text(font, selectedFilter, Math.max(4, (width - 320) / 2 + 46), 65, INFO_COLOR);
        Component selectedRoute = Component.translatable(
                "screen.galacticwars.worksite.route.selected",
                selectedRoute(state),
                state.route().size());
        graphics.text(font, selectedRoute,
                Math.max(4, (width + 320) / 2 - 46 - font.width(selectedRoute)),
                65, INFO_COLOR);

        graphics.text(font, localFeedback,
                (width - font.width(localFeedback)) / 2,
                height - 13,
                state.feedbackCode().equals("updated")
                        || state.feedbackCode().equals("storage_assigned")
                        ? ACCENT_COLOR
                        : WARNING_COLOR);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
        super.onClose();
    }

    private void send(WorksiteConfigurationAction action, int selectedIndex) {
        WorksiteConfigurationSnapshot state = menu.snapshot();
        GalacticNetwork.CHANNEL.sendToServer(new WorksiteActionPayload(
                UUID.randomUUID(),
                menu.containerId,
                action.id(),
                state.configurationRevision(),
                selectedIndex));
        localFeedback = Component.translatable(
                "screen.galacticwars.worksite.feedback.request_sent");
    }

    private ActionButton action(String translationKey, WorksiteConfigurationAction action) {
        return indexedAction(translationKey, action, () -> -1);
    }

    private ActionButton indexedAction(
            String translationKey,
            WorksiteConfigurationAction action,
            java.util.function.IntSupplier selectedIndex
    ) {
        return new ActionButton(translationKey, action, selectedIndex);
    }

    private void clampSelections(WorksiteConfigurationSnapshot snapshot) {
        filterIndex = clampIndex(filterIndex, snapshot.itemFilters().size());
        routeIndex = clampIndex(routeIndex, snapshot.route().size());
    }

    private String selectedFilter(WorksiteConfigurationSnapshot snapshot) {
        return snapshot.itemFilters().isEmpty()
                ? "-"
                : snapshot.itemFilters().get(clampIndex(filterIndex, snapshot.itemFilters().size()));
    }

    private String selectedRoute(WorksiteConfigurationSnapshot snapshot) {
        if (snapshot.route().isEmpty()) {
            return "-";
        }
        WorksiteConfigurationSnapshot.RouteWaypointView waypoint =
                snapshot.route().get(clampIndex(routeIndex, snapshot.route().size()));
        return waypoint.x() + "," + waypoint.y() + "," + waypoint.z()
                + " " + waypoint.action();
    }

    private static int cycle(int current, int delta, int size) {
        return size <= 0 ? 0 : Math.floorMod(current + delta, size);
    }

    private static int clampIndex(int current, int size) {
        return size <= 0 ? 0 : Math.max(0, Math.min(current, size - 1));
    }

    private record ActionButton(
            String translationKey,
            WorksiteConfigurationAction action,
            java.util.function.IntSupplier selectedIndex
    ) {
    }
}
