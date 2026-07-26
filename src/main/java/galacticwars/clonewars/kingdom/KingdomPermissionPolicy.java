package galacticwars.clonewars.kingdom;

import java.util.EnumSet;
import java.util.Set;

public final class KingdomPermissionPolicy {
    private static final Set<KingdomPermission> OWNER = Set.copyOf(EnumSet.allOf(KingdomPermission.class));
    private static final Set<KingdomPermission> OFFICER = Set.copyOf(EnumSet.of(
            KingdomPermission.MANAGE_MEMBERS,
            KingdomPermission.MANAGE_DIPLOMACY,
            KingdomPermission.MANAGE_CLAIMS,
            KingdomPermission.COMMAND_ARMY,
            KingdomPermission.BUILD,
            KingdomPermission.TRAVEL,
            KingdomPermission.USE_STORAGE,
            KingdomPermission.MANAGE_LOGISTICS,
            KingdomPermission.MANAGE_WORKSITES,
            KingdomPermission.MANAGE_TECHNOLOGY,
            KingdomPermission.RECRUIT));
    private static final Set<KingdomPermission> BUILDER = Set.copyOf(EnumSet.of(
            KingdomPermission.BUILD,
            KingdomPermission.TRAVEL,
            KingdomPermission.USE_STORAGE,
            KingdomPermission.MANAGE_WORKSITES));
    private static final Set<KingdomPermission> QUARTERMASTER = Set.copyOf(EnumSet.of(
            KingdomPermission.USE_STORAGE,
            KingdomPermission.MANAGE_LOGISTICS,
            KingdomPermission.TRAVEL,
            KingdomPermission.MANAGE_WORKSITES,
            KingdomPermission.RECRUIT));
    private static final Set<KingdomPermission> MEMBER = Set.copyOf(EnumSet.of(
            KingdomPermission.USE_STORAGE, KingdomPermission.TRAVEL));

    private KingdomPermissionPolicy() {
    }

    public static boolean allows(KingdomMemberRole role, KingdomPermission permission) {
        return permissions(role).contains(permission);
    }

    public static Set<KingdomPermission> permissions(KingdomMemberRole role) {
        return switch (role) {
            case OWNER -> OWNER;
            case OFFICER -> OFFICER;
            case BUILDER -> BUILDER;
            case QUARTERMASTER -> QUARTERMASTER;
            case MEMBER -> MEMBER;
        };
    }
}
