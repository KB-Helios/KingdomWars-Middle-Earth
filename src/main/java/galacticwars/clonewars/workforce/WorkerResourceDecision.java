package galacticwars.clonewars.workforce;

import java.util.Locale;
import java.util.Objects;

public record WorkerResourceDecision(
        WorkerResourceAction action,
        String itemId,
        int quantity,
        String reasonCode
) {
    public WorkerResourceDecision {
        Objects.requireNonNull(action, "action");
        itemId = itemId == null ? "" : itemId.trim().toLowerCase(Locale.ROOT);
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        Objects.requireNonNull(reasonCode, "reasonCode");
        reasonCode = reasonCode.trim();
        if (reasonCode.isEmpty()) {
            throw new IllegalArgumentException("reasonCode cannot be blank");
        }
    }

    public static WorkerResourceDecision idle(String reasonCode) {
        return new WorkerResourceDecision(WorkerResourceAction.IDLE, "", 0, reasonCode);
    }
}
