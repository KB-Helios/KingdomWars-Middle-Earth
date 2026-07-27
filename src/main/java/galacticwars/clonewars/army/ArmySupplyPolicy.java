package galacticwars.clonewars.army;

import galacticwars.clonewars.faction.FactionBalanceService;

/** Pure conversion and availability rules for persisted squad supplies. */
public final class ArmySupplyPolicy {
    public static final int BASE_UNITS_PER_ENERGY_CELL = 16;

    private ArmySupplyPolicy() {
    }

    public static int unitsPerEnergyCell(int supplyEfficiencyPercent) {
        return Math.max(1, FactionBalanceService.applyPercentFloor(
                BASE_UNITS_PER_ENERGY_CELL, supplyEfficiencyPercent));
    }
}
