package galacticwars.clonewars.combat;

import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.registry.ModItems;
import java.util.Objects;
import java.util.function.IntPredicate;
import net.minecraft.world.Container;

/** Uses shared physical cargo as the ammunition authority for player-managed recruits. */
public final class RecruitAmmunitionService {
    private RecruitAmmunitionService() {
    }

    /**
     * Atomically authorizes one blaster shot and consumes its Energy Cell when required.
     * Natural faction NPCs remain outside player-managed cargo logistics.
     */
    public static boolean tryConsumeForShot(GalacticRecruitEntity recruit) {
        Objects.requireNonNull(recruit, "recruit");
        if (!recruit.isTame()) {
            return true;
        }
        Container cargo = recruit.createCargoContainer();
        return tryConsumeForShot(
                true,
                cargo.getContainerSize(),
                slot -> cargo.getItem(slot).is(ModItems.ENERGY_CELL.get()),
                slot -> !cargo.removeItem(slot, 1).isEmpty());
    }

    static boolean tryConsumeForShot(
            boolean playerManaged,
            int slotCount,
            IntPredicate containsAmmunition,
            IntPredicate consumeOne
    ) {
        if (!playerManaged) {
            return true;
        }
        if (slotCount < 0) {
            throw new IllegalArgumentException("slotCount must be non-negative");
        }
        Objects.requireNonNull(containsAmmunition, "containsAmmunition");
        Objects.requireNonNull(consumeOne, "consumeOne");
        for (int slot = 0; slot < slotCount; slot++) {
            if (containsAmmunition.test(slot) && consumeOne.test(slot)) {
                return true;
            }
        }
        return false;
    }
}
