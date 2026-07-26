package galacticwars.clonewars.progression;

import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.faction.FactionAlignmentSavedData;
import galacticwars.clonewars.faction.FactionId;
import galacticwars.clonewars.kingdom.KingdomDiplomacy;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomRelation;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.technology.KingdomTechnologyState;
import galacticwars.clonewars.technology.TechnologyCatalog;
import java.util.Objects;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** The one server authority for campaign, technology, reputation, diplomacy and building gates. */
public final class GameplayAccessResolver {
    public static final Set<String> EXPORTABLE_RECIPES = Set.of(
            "galacticwars:dc15_blaster",
            "galacticwars:e5_blaster",
            "galacticwars:westar_blaster",
            "galacticwars:scatter_blaster",
            "galacticwars:vibroblade",
            "galacticwars:nightsister_bow");

    private GameplayAccessResolver() {
    }

    public static AccessDecision fabrication(
            ServerPlayer player,
            String recipeId,
            String recipeFaction,
            String requiredTechnology,
            boolean exportable,
            boolean buildingPresent,
            boolean serverPolicyAllows
    ) {
        if (!(player.level() instanceof ServerLevel level)) {
            return AccessDecision.denied("server_only");
        }
        KingdomSavedData kingdoms = KingdomSavedData.get(level);
        KingdomRecord kingdom = kingdoms.kingdomForPlayer(player.getUUID()).orElse(null);
        if (kingdom == null) {
            return AccessDecision.denied("no_kingdom");
        }
        KingdomTechnologyState technology = kingdoms.technologyStateOrDefault(kingdom.id());
        int reputation = FactionAlignmentSavedData.get(level).alignment(player.getUUID())
                .score(FactionId.of(recipeFaction));
        int friendlyThreshold = GameplayDataManager.snapshot().npcAiProfile(FactionId.of(recipeFaction))
                .map(profile -> profile.friendlyThreshold()).orElse(10);
        boolean blockedDiplomacy = kingdoms.kingdoms().stream()
                .filter(other -> !other.id().equals(kingdom.id()) && other.factionId().equals(recipeFaction))
                .map(other -> kingdoms.relation(kingdom.id(), other.id()))
                .anyMatch(relation -> hostileOrEmbargoed(relation, level.getGameTime()));
        return fabrication(new FabricationAccessContext(
                recipeId,
                recipeFaction,
                requiredTechnology,
                kingdom.factionId(),
                technology.completedNodes().stream().collect(java.util.stream.Collectors.toUnmodifiableSet()),
                ProgressionSavedData.get(level).state(player.getUUID()).unlocks(),
                reputation,
                friendlyThreshold,
                exportable,
                blockedDiplomacy,
                buildingPresent,
                serverPolicyAllows));
    }

    public static AccessDecision fabrication(FabricationAccessContext context) {
        Objects.requireNonNull(context, "context");
        if (!context.serverPolicyAllows()) {
            return AccessDecision.denied("server_policy");
        }
        if (!context.buildingPresent()) {
            return AccessDecision.denied("fabricator_required");
        }
        boolean ownTree = context.recipeFaction().equals(TechnologyCatalog.UNIVERSAL)
                || context.recipeFaction().equals(context.kingdomFaction());
        if (ownTree) {
            return context.completedTechnology().contains(context.requiredTechnology())
                    ? AccessDecision.technology()
                    : AccessDecision.denied("technology_locked");
        }
        if (!context.exportable() || !EXPORTABLE_RECIPES.contains(context.recipeId())) {
            return AccessDecision.denied("not_exportable");
        }
        if (context.diplomacyBlocked()) {
            return AccessDecision.denied("hostility_or_embargo");
        }
        if (context.reputation() < context.friendlyThreshold()) {
            return AccessDecision.denied("friendly_reputation_required");
        }
        if (!context.campaignUnlocks().contains("advanced_trading")) {
            return AccessDecision.denied("advanced_trading_required");
        }
        return AccessDecision.license();
    }

    private static boolean hostileOrEmbargoed(KingdomDiplomacy diplomacy, long gameTime) {
        return diplomacy.embargo() || diplomacy.effectiveRelation(gameTime) == KingdomRelation.ENEMY;
    }

    public record FabricationAccessContext(
            String recipeId,
            String recipeFaction,
            String requiredTechnology,
            String kingdomFaction,
            Set<String> completedTechnology,
            Set<String> campaignUnlocks,
            int reputation,
            int friendlyThreshold,
            boolean exportable,
            boolean diplomacyBlocked,
            boolean buildingPresent,
            boolean serverPolicyAllows
    ) {
        public FabricationAccessContext {
            Objects.requireNonNull(recipeId, "recipeId");
            Objects.requireNonNull(recipeFaction, "recipeFaction");
            Objects.requireNonNull(requiredTechnology, "requiredTechnology");
            Objects.requireNonNull(kingdomFaction, "kingdomFaction");
            completedTechnology = Set.copyOf(completedTechnology);
            campaignUnlocks = Set.copyOf(campaignUnlocks);
        }
    }

    public record AccessDecision(boolean allowed, String source, String reason) {
        public static AccessDecision technology() {
            return new AccessDecision(true, "technology", "");
        }

        public static AccessDecision license() {
            return new AccessDecision(true, "reputation_license", "");
        }

        public static AccessDecision denied(String reason) {
            return new AccessDecision(false, "", reason);
        }
    }
}
