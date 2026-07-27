package galacticwars.clonewars.integration;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public final class BlasterAssetIntegrationTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/galacticwars");
    private static final List<String> WEAPONS =
            List.of("dc15_blaster", "e5_blaster", "westar_blaster", "scatter_blaster");
    private static final Map<String, List<String>> IDENTITY_DETAILS = Map.of(
            "dc15_blaster", List.of(
                    "dc15_scope_body", "dc15_foregrip", "dc15_cooling_ring_"),
            "e5_blaster", List.of(
                    "e5_round_receiver", "e5_stock_top", "e5_cooling_ring_",
                    "e5_forward_handle"),
            "westar_blaster", List.of(
                    "westar_grip_rib_", "westar_fork_left", "westar_fork_right"),
            "scatter_blaster", List.of(
                    "scatter_upper_barrel", "scatter_lower_barrel",
                    "scatter_upper_emitter", "scatter_lower_emitter",
                    "scatter_forward_grip"));

    private BlasterAssetIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        Set<String> geometryDigests = new HashSet<>();
        Set<String> textureDigests = new HashSet<>();
        String generator = Files.readString(Path.of("tools/generate_blaster_models.py"));
        for (String weapon : WEAPONS) {
            String definition = json(ASSETS.resolve("items/" + weapon + ".json"));
            require(definition.contains("\"type\": \"minecraft:special\"")
                            && definition.contains("\"type\": \"geckolib:geckolib\"")
                            && definition.contains("galacticwars:item/" + weapon),
                    weapon + " must resolve through GeckoLib's special item renderer");

            String display = json(ASSETS.resolve("models/item/" + weapon + ".json"));
            require(display.contains("\"parent\": \"builtin/entity\"")
                            && display.contains("\"thirdperson_righthand\"")
                            && display.contains("\"firstperson_righthand\"")
                            && display.contains("\"gui\"")
                            && display.contains("\"fixed\""),
                    weapon + " must define consistent GeckoLib transforms for all item contexts");
            require(!display.contains("minecraft:item/generated")
                            && !display.contains("minecraft:item/handheld"),
                    weapon + " must not fall back to a paper-thin vanilla model");
            require(!Pattern.compile("-?\\d+\\.\\d{10,}").matcher(display).find(),
                    weapon + " display transforms must use clean decimal values");

            Path geometryPath = ASSETS.resolve("geckolib/models/item/blaster/" + weapon + ".geo.json");
            String geometry = json(geometryPath);
            require(geometry.contains("geometry.galacticwars.item.blaster." + weapon),
                    weapon + " stable visual geometry identifier");
            require(geometry.contains("\"name\": \"receiver\"")
                            && geometry.contains("\"name\": \"grip\"")
                            && geometry.contains("\"name\": \"barrel\"")
                            && geometry.contains("\"name\": \"foregrip\"")
                            && geometry.contains("\"name\": \"power_cell\"")
                            && geometry.contains("\"name\": \"muzzle\""),
                    weapon + " must expose firearm landmark bones");
            int minimumCubes = weapon.equals("westar_blaster") ? 18 : 20;
            require(occurrences(geometry, "\"origin\"") >= minimumCubes,
                    weapon + " must be a detailed volumetric model");
            for (String identityDetail : IDENTITY_DETAILS.get(weapon)) {
                require(generator.contains(identityDetail),
                        weapon + " must preserve its " + identityDetail + " identity detail");
            }
            geometryDigests.add(digest(geometryPath));

            Path texture = ASSETS.resolve("textures/item/blaster/" + weapon + ".png");
            Path glowmask = ASSETS.resolve("textures/item/blaster/" + weapon + "_glowmask.png");
            assertAtlas(texture, true, true);
            assertAtlas(glowmask, true, false);
            textureDigests.add(digest(texture));
            require(json(ASSETS.resolve("geckolib/animations/item/blaster/" + weapon + ".animation.json"))
                            .contains("animation.blaster.idle"),
                    weapon + " GeckoLib animation contract");
        }
        require(geometryDigests.size() == WEAPONS.size(), "each blaster must own distinct geometry");
        require(textureDigests.size() == WEAPONS.size(), "each blaster must own a distinct UV-safe atlas");

        String itemClass = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/combat/BlasterItem.java"));
        require(itemClass.contains("implements GeoItem")
                        && itemClass.contains("visualId")
                        && itemClass.contains("createGeoRenderer")
                        && itemClass.contains("animation.blaster.idle"),
                "BlasterItem must expose the shared GeckoLib visual contract");
        String renderer = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/client/render/GalacticBlasterRenderer.java"));
        require(renderer.contains("extends GeoItemRenderer")
                        && renderer.contains("AutoGlowingGeoLayer")
                        && renderer.contains("item.visualId()"),
                "blasters must use weapon-specific geometry and emissive materials");
        assertHeldWeaponPose();
        assertRecruitBlasterPose();
        System.out.println("BlasterAssetIntegrationTest passed");
    }

    private static void assertHeldWeaponPose() throws Exception {
        String extension = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/client/render/BlasterClientExtensions.java"));
        require(extension.contains("HumanoidModel.ArmPose.CROSSBOW_HOLD")
                        && extension.contains("applyForgeHandTransform")
                        && extension.contains("recoil"),
                "NeoForge blasters must retain their shouldered/recoil pose");
        String fabricClient = Files.readString(Path.of(
                "fabric/src/main/kotlin/galacticwars/clonewars/fabric/GalacticWarsFabricClient.kt"));
        require(fabricClient.contains("GalacticWarsClient.init()"),
                "Fabric must initialize the common GeckoLib item-rendering path");
    }

    private static void assertRecruitBlasterPose() throws Exception {
        String model = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/client/render/GalacticRecruitGeoModel.java"));
        require(model.contains("BLASTER_STANCE")
                        && model.contains("BlasterStance.RIFLE")
                        && model.contains("BlasterStance.PISTOL")
                        && model.contains("\"westar_blaster\""),
                "recruit render state must distinguish rifles from the compact WESTAR sidearm");
        String renderer = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/client/render/GalacticRecruitRenderer.java"));
        require(renderer.contains("adjustModelBonesForRender")
                        && renderer.contains("applyRifleStance")
                        && renderer.contains("applyPistolStance")
                        && renderer.contains("radians(-76.0F)")
                        && renderer.contains("radians(-68.0F)")
                        && renderer.contains("radians(-84.0F)")
                        && renderer.contains("\"right_arm\"")
                        && renderer.contains("\"left_arm\""),
                "recruits must apply forward-facing runtime rifle and pistol arm poses"
                        + " without replacing NPC geometry");
    }

    private static void assertAtlas(
            Path path,
            boolean requireTransparency,
            boolean requireMaterialDetail
    ) throws Exception {
        require(Files.isRegularFile(path), "missing texture " + path);
        BufferedImage image = ImageIO.read(path.toFile());
        require(image != null && image.getWidth() == 256 && image.getHeight() == 256,
                path + " must be a 256x256 geometry-bound atlas");
        boolean visible = false;
        boolean transparent = false;
        int visiblePixels = 0;
        Set<Integer> opaqueColors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int alpha = pixel >>> 24;
                visible |= alpha != 0;
                transparent |= alpha == 0;
                if (alpha != 0) {
                    visiblePixels++;
                    opaqueColors.add(pixel & 0x00FFFFFF);
                }
            }
        }
        require(visible, path + " must contain visible mapped pixels");
        require(!requireTransparency || transparent, path + " must retain unused transparent space");
        if (requireMaterialDetail) {
            require(visiblePixels >= 512, path + " must provide substantial authored UV coverage");
            require(opaqueColors.size() >= 24, path + " must retain layered material variation");
        }
    }

    private static String digest(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }

    private static String json(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing JSON asset " + path);
        String content = Files.readString(path).trim();
        require(content.startsWith("{") && content.endsWith("}"), "invalid JSON envelope " + path);
        return content;
    }

    private static int occurrences(String content, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = content.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
