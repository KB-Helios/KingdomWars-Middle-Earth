package galacticwars.clonewars.workforce;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** One bounded profession action planned for the server-authoritative runtime. */
public record WorkerAction(
        Type type,
        Optional<WorkerTarget> target,
        String itemId,
        int quantity,
        String reasonCode
) {
    public WorkerAction {
        Objects.requireNonNull(type, "type");
        target = target == null ? Optional.empty() : target;
        itemId = itemId == null ? "" : itemId.trim().toLowerCase(Locale.ROOT);
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        reasonCode = Objects.requireNonNull(reasonCode, "reasonCode").trim();
        if (reasonCode.isEmpty()) {
            throw new IllegalArgumentException("reasonCode cannot be blank");
        }
    }

    public static WorkerAction idle(String reasonCode) {
        return new WorkerAction(Type.IDLE, Optional.empty(), "", 0, reasonCode);
    }

    public enum Type {
        IDLE,
        NAVIGATE,
        INTERACT,
        WITHDRAW,
        DEPOSIT,
        REQUEST_SUPPLY
    }
}
