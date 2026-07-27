package galacticwars.clonewars.menu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Dependency-light guard for the cross-loader recruit loadout wiring. */
public final class RecruitLoadoutStructureTest {
    private RecruitLoadoutStructureTest() {
    }

    public static void main(String[] args) throws IOException {
        String menu = read("src/main/java/galacticwars/clonewars/menu/RecruitLoadoutMenu.java");
        String provider = read(
                "src/main/java/galacticwars/clonewars/menu/RecruitLoadoutMenuProvider.java");
        String screen = read(
                "src/main/java/galacticwars/clonewars/client/gui/RecruitLoadoutScreen.java");

        assertContains(menu, "public static final int EQUIPMENT_SLOT_COUNT = 7", "equipment slots");
        assertContains(menu, "public static final int CARGO_SLOT_COUNT = 9", "cargo slots");
        assertContains(menu, "recruit.createCargoContainer()", "shared physical cargo");
        assertContains(menu, "DataComponents.EQUIPPABLE", "component equipment policy");
        assertContains(menu, "RecruitEquipmentTarget.MILITARY_WEAPON", "military weapon slot");
        assertContains(menu, "RecruitEquipmentTarget.WORKER_TOOL", "worker tool slot");
        assertContains(menu, "WorkerDutyLoadoutPolicy.isRecognizedTool", "worker tool routing");
        assertContains(menu, "stack.get(DataComponents.WEAPON) != null", "military weapon routing");
        assertContains(menu, "recruit.canPlayerManageLogistics(player)", "permission guard");
        assertContains(provider, "buffer.writeVarInt(this.recruit.getId())", "entity id payload");
        assertContains(screen,
                "extends AbstractContainerScreen<RecruitLoadoutMenu>", "interactive slot screen");

        System.out.println("RecruitLoadoutStructureTest passed");
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }

    private static void assertContains(String value, String expected, String label) {
        if (!value.contains(expected)) {
            throw new AssertionError(label + " missing <" + expected + ">");
        }
    }
}
