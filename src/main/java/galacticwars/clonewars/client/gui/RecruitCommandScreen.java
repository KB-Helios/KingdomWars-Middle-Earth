package galacticwars.clonewars.client.gui;

import java.util.ArrayList;
import java.util.List;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.menu.RecruitCommandMenu;
import galacticwars.clonewars.workforce.WorkerProfessionCatalog;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;

public class RecruitCommandScreen extends Screen implements MenuAccess<RecruitCommandMenu> {
    private static final int BUTTON_WIDTH = 102;
    private static final int BUTTON_HEIGHT = 18;
    private static final int GAP = 2;
    private static final int COLUMN_GAP = 6;
    private static final int COLUMN_COUNT = 3;
    private static final int CONTROL_ROW_COUNT = 11;
    private static final int OFFICER_CONTROL_ROW_COUNT = 8;
    private static final int STATUS_PANEL_MIN_WIDTH = 220;
    private static final int COMPACT_STATUS_ROW = 3;
    private static final int STATUS_COLOR = 0xE0E0E0;
    private static final int STATUS_MUTED_COLOR = 0x9CA3AF;
    private static final int FEEDBACK_COLOR = 0xFFD27A;
    private final RecruitCommandMenu menu;
    private Component localFeedback = Component.translatable("screen.galacticwars.recruit.guidance");
    private int refreshDelayTicks;
    private boolean lastTame;
    private boolean lastOwnedByPlayer;

    public RecruitCommandScreen(RecruitCommandMenu menu, Inventory inventory, Component title) {
        super(title);
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();
        Entity entity = (this.minecraft != null && this.minecraft.level != null)
                ? this.minecraft.level.getEntity(this.menu.recruitEntityId())
                : null;
        boolean ownedByPlayer = entity instanceof GalacticRecruitEntity recruit
                && this.minecraft != null
                && this.minecraft.player != null
                && recruit.isOwnedBy(this.minecraft.player);
        boolean tame = entity instanceof GalacticRecruitEntity recruit && recruit.isTame();
        boolean armyCommandAccess = this.menu.armyCommandAccess();
        boolean logisticsAccess = this.menu.logisticsAccess();
        boolean worksiteAccess = this.menu.worksiteAccess();
        this.lastTame = tame;
        this.lastOwnedByPlayer = ownedByPlayer;

        int x = (this.width - BUTTON_WIDTH) / 2;
        int visibleRows = ownedByPlayer ? CONTROL_ROW_COUNT
                : armyCommandAccess
                        ? OFFICER_CONTROL_ROW_COUNT
                                + (logisticsAccess ? 1 : 0)
                                + (worksiteAccess ? 1 : 0)
                        : (logisticsAccess ? 1 : 0) + (worksiteAccess ? 1 : 0);
        int y = Math.max(8, (this.height - (visibleRows * (BUTTON_HEIGHT + GAP))) / 2);
        if (!tame) {
            this.addButton(x, y, "screen.galacticwars.recruit.hire", RecruitCommandMenu.BUTTON_HIRE);
            return;
        }
        if (!ownedByPlayer && !armyCommandAccess && !logisticsAccess && !worksiteAccess) {
            this.addRenderableWidget(Button.builder(
                            Component.translatable("screen.galacticwars.recruit.locked"),
                            button -> this.onClose())
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            return;
        }
        if (!ownedByPlayer) {
            if (!armyCommandAccess) {
                int row = 0;
                if (logisticsAccess) {
                    this.addButton(
                            x,
                            y + row++ * (BUTTON_HEIGHT + GAP),
                            "screen.galacticwars.recruit.loadout.open",
                            RecruitCommandMenu.BUTTON_OPEN_LOADOUT);
                }
                if (worksiteAccess) {
                    this.addButton(
                            x,
                            y + row * (BUTTON_HEIGHT + GAP),
                            "screen.galacticwars.recruit.worksite.configure",
                            RecruitCommandMenu.BUTTON_OPEN_WORKSITE_CONFIGURATION);
                }
                return;
            }
            this.addButton(x, y, "screen.galacticwars.recruit.follow", RecruitCommandMenu.BUTTON_FOLLOW);
            this.addButton(x, y + (BUTTON_HEIGHT + GAP),
                    "screen.galacticwars.recruit.hold", RecruitCommandMenu.BUTTON_HOLD);
            this.addButton(x, y + 2 * (BUTTON_HEIGHT + GAP),
                    "screen.galacticwars.recruit.move", RecruitCommandMenu.BUTTON_MOVE);
            this.addButton(x, y + 3 * (BUTTON_HEIGHT + GAP),
                    "screen.galacticwars.recruit.protect", RecruitCommandMenu.BUTTON_PROTECT);
            this.addButton(x, y + 4 * (BUTTON_HEIGHT + GAP),
                    "screen.galacticwars.recruit.attack", RecruitCommandMenu.BUTTON_ATTACK);
            this.addButton(x, y + 5 * (BUTTON_HEIGHT + GAP),
                    "screen.galacticwars.recruit.clear", RecruitCommandMenu.BUTTON_CLEAR);
            this.addButton(x, y + 6 * (BUTTON_HEIGHT + GAP),
                    "screen.galacticwars.recruit.commander.patrol",
                    RecruitCommandMenu.BUTTON_PATROL);
            this.addButton(x, y + 7 * (BUTTON_HEIGHT + GAP),
                    "screen.galacticwars.recruit.commander.formation",
                    RecruitCommandMenu.BUTTON_CYCLE_FORMATION);
            if (logisticsAccess) {
                this.addButton(
                        x,
                        y + 8 * (BUTTON_HEIGHT + GAP),
                        "screen.galacticwars.recruit.loadout.open",
                        RecruitCommandMenu.BUTTON_OPEN_LOADOUT);
            }
            if (worksiteAccess) {
                this.addButton(
                        x,
                        y + (8 + (logisticsAccess ? 1 : 0)) * (BUTTON_HEIGHT + GAP),
                        "screen.galacticwars.recruit.worksite.configure",
                        RecruitCommandMenu.BUTTON_OPEN_WORKSITE_CONFIGURATION);
            }
            return;
        }

        int commandX = (this.width - (BUTTON_WIDTH * COLUMN_COUNT + COLUMN_GAP * (COLUMN_COUNT - 1))) / 2;
        int professionX = commandX + BUTTON_WIDTH + COLUMN_GAP;
        int kingdomX = professionX + BUTTON_WIDTH + COLUMN_GAP;

        this.addButton(commandX, y, "screen.galacticwars.recruit.follow", RecruitCommandMenu.BUTTON_FOLLOW);
        this.addButton(commandX, y + (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.hold", RecruitCommandMenu.BUTTON_HOLD);
        this.addButton(commandX, y + 2 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.move", RecruitCommandMenu.BUTTON_MOVE);
        this.addButton(commandX, y + 3 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.protect", RecruitCommandMenu.BUTTON_PROTECT);
        this.addButton(commandX, y + 4 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.attack", RecruitCommandMenu.BUTTON_ATTACK);
        this.addButton(commandX, y + 5 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.clear", RecruitCommandMenu.BUTTON_CLEAR);
        this.addButton(commandX, y + 6 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.worksite.set", RecruitCommandMenu.BUTTON_SET_WORKSITE);
        this.addButton(commandX, y + 7 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.worksite.return", RecruitCommandMenu.BUTTON_RETURN_WORKSITE);
        this.addButton(commandX, y + 8 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.worksite.clear", RecruitCommandMenu.BUTTON_CLEAR_WORKSITE);
        this.addButton(commandX, y + 9 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.storage.set", RecruitCommandMenu.BUTTON_SET_STORAGE);
        this.addButton(commandX, y + 10 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.base.forward_base", RecruitCommandMenu.BUTTON_BUILD_STARTER_KEEP);
        this.addButton(professionX, y + 6 * (BUTTON_HEIGHT + GAP), "screen.galacticwars.recruit.base.next", RecruitCommandMenu.BUTTON_NEXT_BLUEPRINT);
        this.addButton(professionX, y + 7 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.soldier.return",
                RecruitCommandMenu.BUTTON_RETURN_TO_SOLDIER);
        this.addButton(professionX, y + 8 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.base.cancel",
                RecruitCommandMenu.BUTTON_CANCEL_BUILD);
        this.addButton(professionX, y + 9 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.base.rotate",
                RecruitCommandMenu.BUTTON_ROTATE_BLUEPRINT);

        int[] workerProfessionButtonIds = RecruitCommandMenu.workerProfessionButtonIds();
        for (int i = 0; i < workerProfessionButtonIds.length; i++) {
            this.addButton(
                    professionX,
                    y + i * (BUTTON_HEIGHT + GAP),
                    WorkerProfessionCatalog.definitionForButton(workerProfessionButtonIds[i]).orElseThrow().translationKey(),
                    workerProfessionButtonIds[i]);
        }
        this.addButton(
                kingdomX,
                y,
                "screen.galacticwars.recruit.commander.promote",
                RecruitCommandMenu.BUTTON_PROMOTE_COMMANDER);
        this.addButton(
                kingdomX,
                y + (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.commander.auto",
                RecruitCommandMenu.BUTTON_TOGGLE_AUTO_RECRUITMENT);
        this.addButton(
                kingdomX,
                y + 2 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.commander.recruit",
                RecruitCommandMenu.BUTTON_START_RECRUITMENT);
        this.addButton(
                kingdomX,
                y + 3 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.commander.formation",
                RecruitCommandMenu.BUTTON_CYCLE_FORMATION);
        this.addButton(
                kingdomX,
                y + 4 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.worksite.radius.decrease",
                RecruitCommandMenu.BUTTON_WORK_RADIUS_DECREASE);
        this.addButton(
                kingdomX,
                y + 5 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.worksite.radius.increase",
                RecruitCommandMenu.BUTTON_WORK_RADIUS_INCREASE);
        this.addButton(
                kingdomX,
                y + 6 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.commander.patrol",
                RecruitCommandMenu.BUTTON_PATROL);
        this.addButton(
                kingdomX,
                y + 7 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.loadout.open",
                RecruitCommandMenu.BUTTON_OPEN_LOADOUT);
        this.addButton(
                kingdomX,
                y + 8 * (BUTTON_HEIGHT + GAP),
                "screen.galacticwars.recruit.worksite.configure",
                RecruitCommandMenu.BUTTON_OPEN_WORKSITE_CONFIGURATION);
    }

    @Override
    public RecruitCommandMenu getMenu() {
        return this.menu;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.refreshDelayTicks <= 0 || --this.refreshDelayTicks > 0
                || this.minecraft == null || this.minecraft.level == null) {
            return;
        }
        Entity entity = this.minecraft.level.getEntity(this.menu.recruitEntityId());
        boolean tame = entity instanceof GalacticRecruitEntity recruit && recruit.isTame();
        boolean ownedByPlayer = entity instanceof GalacticRecruitEntity recruit
                && this.minecraft.player != null
                && recruit.isOwnedBy(this.minecraft.player);
        this.localFeedback = Component.translatable("screen.galacticwars.recruit.guidance");
        if (tame != this.lastTame || ownedByPlayer != this.lastOwnedByPlayer) {
            this.rebuildWidgets();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, this.localFeedback,
                (this.width - this.font.width(this.localFeedback)) / 2,
                this.height - 14, FEEDBACK_COLOR);
        if (this.minecraft != null && this.minecraft.level != null) {
            Entity entity = this.minecraft.level.getEntity(this.menu.recruitEntityId());
            if (entity instanceof GalacticRecruitEntity recruit) {
                this.drawRecruitStatusPanel(graphics, recruit, mouseX, mouseY);
            }
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

    private void addButton(int x, int y, String translationKey, int buttonId) {
        this.addRenderableWidget(Button.builder(Component.translatable(translationKey), button -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
                        this.localFeedback = Component.translatable(
                                "screen.galacticwars.recruit.request_sent");
                        this.refreshDelayTicks = 10;
                    }
                })
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private void drawRecruitStatusPanel(
            GuiGraphicsExtractor graphics,
            GalacticRecruitEntity recruit,
            int mouseX,
            int mouseY
    ) {
        boolean ownedByPlayer = this.minecraft != null
                && this.minecraft.player != null
                && recruit.isOwnedBy(this.minecraft.player);
        int columnCount = ownedByPlayer ? COLUMN_COUNT : 1;
        int rowCount = ownedByPlayer ? CONTROL_ROW_COUNT
                : this.menu.armyCommandAccess()
                        ? OFFICER_CONTROL_ROW_COUNT
                                + (this.menu.logisticsAccess() ? 1 : 0)
                                + (this.menu.worksiteAccess() ? 1 : 0)
                        : (this.menu.logisticsAccess() ? 1 : 0)
                                + (this.menu.worksiteAccess() ? 1 : 0);
        int controlsWidth = BUTTON_WIDTH * columnCount + COLUMN_GAP * (columnCount - 1);
        int controlsLeft = (this.width - controlsWidth) / 2;
        int controlsRight = controlsLeft + controlsWidth;
        List<Component> statusLines = recruit.recruitStatusSnapshot().lines();
        if (this.width - controlsRight < STATUS_PANEL_MIN_WIDTH) {
            this.drawCompactStatusTooltip(graphics, statusLines, controlsRight, rowCount, mouseX, mouseY);
            return;
        }

        int x = controlsRight + 8;
        int y = 8;
        graphics.text(this.font, Component.translatable("screen.galacticwars.recruit.status.title"), x, y, STATUS_COLOR);
        int lineY = y + 12;
        for (Component line : statusLines) {
            graphics.text(this.font, line, x, lineY, STATUS_MUTED_COLOR);
            lineY += 10;
        }
    }

    private void drawCompactStatusTooltip(
            GuiGraphicsExtractor graphics,
            List<Component> statusLines,
            int controlsRight,
            int rowCount,
            int mouseX,
            int mouseY
    ) {
        int controlsTop = Math.max(8, (this.height - rowCount * (BUTTON_HEIGHT + GAP)) / 2);
        int x = controlsRight - BUTTON_WIDTH;
        int y = controlsTop + COMPACT_STATUS_ROW * (BUTTON_HEIGHT + GAP);
        Component title = Component.translatable("screen.galacticwars.recruit.status.title");
        graphics.text(this.font, title, x, y + 5, STATUS_COLOR);

        if (mouseX >= x && mouseX < x + BUTTON_WIDTH && mouseY >= y && mouseY < y + BUTTON_HEIGHT) {
            List<Component> tooltipLines = new ArrayList<>(statusLines.size() + 1);
            tooltipLines.add(title);
            tooltipLines.addAll(statusLines);
            graphics.setComponentTooltipForNextFrame(this.font, tooltipLines, mouseX, mouseY);
        }
    }
}
