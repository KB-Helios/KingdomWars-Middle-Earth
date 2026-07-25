package galacticwars.clonewars.settlement;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import galacticwars.clonewars.faction.ai.NpcRole;
import net.minecraft.resources.Identifier;

public record BlueprintRosterEntry(
        String entityTypeId,
        int minimum,
        int maximum,
        int weight,
        String serviceBranch,
        String role
) {
    public BlueprintRosterEntry {
        entityTypeId = normalize(entityTypeId, "entityTypeId");
        try {
            Identifier.parse(entityTypeId);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid entity type identifier: " + entityTypeId);
        }
        serviceBranch = normalize(serviceBranch, "serviceBranch");
        if (!serviceBranch.equals("civilian") && !serviceBranch.equals("military")) {
            throw new IllegalArgumentException("serviceBranch must be 'civilian' or 'military', got: " + serviceBranch);
        }
        role = role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
        if (!role.isEmpty()) {
            NpcRole parsedRole = NpcRole.byId(role).orElse(null);
            if (parsedRole == null) {
                throw new IllegalArgumentException("Unknown NPC role " + role);
            }
            boolean militaryRole = parsedRole == NpcRole.COMMANDER
                    || parsedRole == NpcRole.TROOPER;
            if (militaryRole != serviceBranch.equals("military")) {
                throw new IllegalArgumentException(
                        "NPC role " + role + " does not match " + serviceBranch + " branch");
            }
        }
        if (minimum < 0 || maximum < minimum || maximum > 32 || weight <= 0) {
            throw new IllegalArgumentException("invalid blueprint roster entry for " + entityTypeId);
        }
    }

    public BlueprintRosterEntry(
            String entityTypeId,
            int minimum,
            int maximum,
            int weight,
            String serviceBranch
    ) {
        this(entityTypeId, minimum, maximum, weight, serviceBranch, "");
    }

    public Optional<NpcRole> explicitRole() {
        return NpcRole.byId(role);
    }

    private static String normalize(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank");
        }
        return normalized;
    }
}
