package galacticwars.clonewars.progression;

import java.util.Set;

public final class GameplayAccessResolverTest {
    private GameplayAccessResolverTest() {
    }

    public static void main(String[] args) {
        serverPolicyAndBuildingTakePrecedence();
        domesticTechnologyAuthorizesFabrication();
        foreignLicenseRequiresEveryGate();
        nonExportableContentNeverReceivesLicense();
        System.out.println("GameplayAccessResolverTest passed");
    }

    private static void serverPolicyAndBuildingTakePrecedence() {
        assertDenied(context(
                "galacticwars:dc15_blaster", true, false, true, 100, true, false),
                "server_policy");
        assertDenied(context(
                "galacticwars:dc15_blaster", true, false, false, 100, true, true),
                "fabricator_required");
    }

    private static void domesticTechnologyAuthorizesFabrication() {
        GameplayAccessResolver.FabricationAccessContext context =
                new GameplayAccessResolver.FabricationAccessContext(
                        "galacticwars:phase_i_clone_helmet",
                        "galacticwars:republic",
                        "galacticwars:clone_armor",
                        "galacticwars:republic",
                        Set.of("galacticwars:clone_armor"),
                        Set.of(),
                        -100,
                        10,
                        false,
                        true,
                        true,
                        true);
        var decision = GameplayAccessResolver.fabrication(context);
        assertTrue(decision.allowed(), "domestic completed technology authorizes recipe");
        assertEquals("technology", decision.source(), "technology decision source");

        var locked = GameplayAccessResolver.fabrication(
                new GameplayAccessResolver.FabricationAccessContext(
                        "galacticwars:dc15_blaster",
                        "galacticwars:republic",
                        "galacticwars:clone_field_arms",
                        "galacticwars:republic",
                        Set.of(),
                        Set.of("advanced_trading"),
                        100,
                        10,
                        true,
                        false,
                        true,
                        true));
        assertTrue(!locked.allowed(), "domestic reputation cannot bypass technology");
        assertEquals("technology_locked", locked.reason(), "domestic technology lock");
    }

    private static void foreignLicenseRequiresEveryGate() {
        var allowed = GameplayAccessResolver.fabrication(context(
                "galacticwars:dc15_blaster", true, false, true, 10, true, true));
        assertTrue(allowed.allowed(), "friendly export license");
        assertEquals("reputation_license", allowed.source(), "license decision source");

        assertDenied(context(
                "galacticwars:dc15_blaster", true, false, true, 9, true, true),
                "friendly_reputation_required");
        assertDenied(context(
                "galacticwars:dc15_blaster", true, true, true, 100, true, true),
                "hostility_or_embargo");
        assertDenied(context(
                "galacticwars:dc15_blaster", true, false, true, 100, false, true),
                "advanced_trading_required");
    }

    private static void nonExportableContentNeverReceivesLicense() {
        assertDenied(context(
                "galacticwars:phase_i_clone_helmet", true, false, true, 100, true, true),
                "not_exportable");
        assertDenied(context(
                "galacticwars:dc15_blaster", false, false, true, 100, true, true),
                "not_exportable");
        assertEquals(Set.of(
                "galacticwars:dc15_blaster",
                "galacticwars:e5_blaster",
                "galacticwars:westar_blaster",
                "galacticwars:scatter_blaster",
                "galacticwars:vibroblade",
                "galacticwars:nightsister_bow"),
                GameplayAccessResolver.EXPORTABLE_RECIPES,
                "closed export allowlist");
    }

    private static GameplayAccessResolver.FabricationAccessContext context(
            String recipeId,
            boolean exportable,
            boolean diplomacyBlocked,
            boolean buildingPresent,
            int reputation,
            boolean advancedTrading,
            boolean serverPolicy
    ) {
        return new GameplayAccessResolver.FabricationAccessContext(
                recipeId,
                "galacticwars:republic",
                "galacticwars:clone_field_arms",
                "galacticwars:separatist",
                Set.of(),
                advancedTrading ? Set.of("advanced_trading") : Set.of(),
                reputation,
                10,
                exportable,
                diplomacyBlocked,
                buildingPresent,
                serverPolicy);
    }

    private static void assertDenied(
            GameplayAccessResolver.FabricationAccessContext context,
            String reason
    ) {
        var decision = GameplayAccessResolver.fabrication(context);
        assertTrue(!decision.allowed(), reason + " is denied");
        assertEquals(reason, decision.reason(), reason + " lock reason");
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }
}
