package galacticwars.clonewars.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class RecruitGeckoLibIntegrationTest {
    private static final List<String> RECRUITS = List.of(
            "clone_trooper", "arc_trooper", "phase_i_clone_trooper", "phase_i_arc_trooper",
            "senate_commando", "republic_honor_guard", "jedi_knight", "sith_acolyte",
            "b1_battle_droid", "b1_security_droid", "b2_super_battle_droid", "commando_droid",
            "mandalorian_warrior", "mandalorian_marksman", "mandalorian_heavy",
            "hutt_enforcer", "bounty_hunter", "smuggler",
            "nightsister_acolyte", "nightsister_archer", "nightbrother_brute",
            "republic_civilian", "togruta_civilian", "separatist_technician", "mandalorian_clansperson",
            "hutt_civilian", "nightsister_civilian");

    private RecruitGeckoLibIntegrationTest() {
    }

    public static void main(String[] args) throws IOException {
        recruitEntityOwnsGeckoLibControllers();
        clientRegistersGeckoRenderersForEveryRecruit();
        geckoAssetsExistForEveryRecruit();

        System.out.println("RecruitGeckoLibIntegrationTest passed");
    }

    private static void recruitEntityOwnsGeckoLibControllers() throws IOException {
        String entity = read("src/main/java/galacticwars/clonewars/entity/GalacticRecruitEntity.java");

        assertContains(entity, "implements GeoEntity", "GeoEntity implementation");
        assertContains(entity, "GeckoLibUtil.createInstanceCache(this)", "GeckoLib instance cache");
        assertContains(entity, "registerControllers", "controller registration method");
        assertContains(entity, "new AnimationController<>(\"recruit_gameplay\"", "gameplay state controller");
        assertContains(entity, "RecruitVisualAction.byId", "authoritative gameplay animation state");
        assertContains(entity, "VISUAL_WALK", "safe walk fallback");
        assertContains(entity, "VISUAL_IDLE", "safe idle fallback");
        assertContains(entity, "DefaultAnimations.genericAttackAnimation(DefaultAnimations.ATTACK_SWING)",
                "attack animation controller");
    }

    private static void clientRegistersGeckoRenderersForEveryRecruit() throws IOException {
        String client = read("src/main/java/galacticwars/clonewars/GalacticWarsClient.java");
        String entities = read("src/main/java/galacticwars/clonewars/registry/ModEntityTypes.java");

        for (String recruit : RECRUITS) {
            assertContains(entities, '"' + recruit + '"', "registered entity " + recruit);
        }
        assertContains(client, "ModEntityTypes.recruits()", "data-driven renderer registration");
    }

    private static void geckoAssetsExistForEveryRecruit() throws IOException {
        for (String recruit : RECRUITS) {
            assertRegularFile("src/main/resources/assets/galacticwars/geckolib/models/entity/"
                    + recruit + ".geo.json");
            assertRegularFile("src/main/resources/assets/galacticwars/geckolib/animations/entity/"
                    + recruit + ".animation.json");
            assertRegularFile("src/main/resources/assets/galacticwars/textures/entity/"
                    + recruit + ".png");
            String animation = read("src/main/resources/assets/galacticwars/geckolib/animations/entity/"
                    + recruit + ".animation.json");
            assertContains(animation, "\"misc.idle\"", "idle animation " + recruit);
            assertContains(animation, "\"move.walk\"", "walk animation " + recruit);
            assertContains(animation, "\"attack.swing\"", "attack animation " + recruit);
        }
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }

    private static void assertContains(String haystack, String needle, String label) {
        if (!haystack.contains(needle)) {
            throw new AssertionError(label + " missing <" + needle + ">");
        }
    }

    private static void assertRegularFile(String relativePath) {
        if (!Files.isRegularFile(Path.of(relativePath))) {
            throw new AssertionError("missing GeckoLib asset <" + relativePath + ">");
        }
    }
}
