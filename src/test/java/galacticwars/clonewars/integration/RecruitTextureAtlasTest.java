package galacticwars.clonewars.integration;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public final class RecruitTextureAtlasTest {
    private static final int PROJECT_ATLAS_SIZE = 256;
    private static final int RECRUIT_TEXEL_DENSITY = 2;
    private static final int PROJECT_ARMOR_ATLAS_SIZE = 1024;
    private static final int ARMOR_TEXEL_DENSITY = 6;
    private static final Pattern TEXTURE_WIDTH = Pattern.compile(
            "\\\"texture_width\\\"\\s*:\\s*([0-9]+)");
    private static final Pattern TEXTURE_HEIGHT = Pattern.compile(
            "\\\"texture_height\\\"\\s*:\\s*([0-9]+)");
    private static final Pattern CUBE = Pattern.compile(
            "\\\"size\\\"\\s*:\\s*\\[\\s*([0-9.]+)\\s*,\\s*([0-9.]+)\\s*,\\s*([0-9.]+)\\s*]"
                    + "\\s*,\\s*\\\"uv\\\"\\s*:\\s*",
            Pattern.DOTALL);
    private static final Pattern BOX_UV = Pattern.compile(
            "\\[\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*]");
    private static final Pattern BONE_NAME = Pattern.compile(
            "\\\"name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern ANIMATED_BONE = Pattern.compile(
            "\\\"([^\\\"]+)\\\"\\s*:\\s*\\{\\s*\\\"(?:rotation|position|scale)\\\"",
            Pattern.DOTALL);
    private static final Path ASSET_ROOT = Path.of("src/main/resources/assets/galacticwars");
    private static final List<String> RECRUITS = List.of(
            "clone_trooper", "arc_trooper", "phase_i_clone_trooper", "phase_i_arc_trooper",
            "senate_commando", "republic_honor_guard", "jedi_knight", "sith_acolyte",
            "b1_battle_droid", "b1_security_droid", "b2_super_battle_droid", "commando_droid",
            "mandalorian_warrior", "mandalorian_marksman", "mandalorian_heavy",
            "hutt_enforcer", "bounty_hunter", "smuggler",
            "nightsister_acolyte", "nightsister_archer", "nightbrother_brute",
            "republic_civilian", "togruta_civilian", "separatist_technician", "mandalorian_clansperson",
            "hutt_civilian", "nightsister_civilian");
    private static final List<String> ARMOR_FAMILIES = List.of(
            "mandalorian_alloy", "nightsister_weave", "republic_plastoid", "phase_i_clone",
            "separatist_alloy", "beskar");
    private static final List<String> DATHOMIR_RECRUITS = List.of(
            "nightsister_acolyte", "nightsister_archer",
            "nightsister_civilian", "nightbrother_brute");
    private static final Set<String> LICENSED_128_RECRUITS = Set.of(
            "clone_trooper", "arc_trooper", "phase_i_clone_trooper", "phase_i_arc_trooper",
            "senate_commando", "republic_honor_guard");
    private static final Set<String> LICENSED_128_BY_64_RECRUITS = Set.of(
            "b1_battle_droid", "b1_security_droid", "separatist_technician");
    private static final Set<String> AUTHORIZED_SOURCE_RECRUITS = Set.of(
            "clone_trooper", "arc_trooper", "phase_i_clone_trooper", "phase_i_arc_trooper",
            "senate_commando", "republic_honor_guard", "b1_battle_droid", "b1_security_droid",
            "togruta_civilian", "hutt_enforcer", "smuggler", "hutt_civilian",
            "separatist_technician", "mandalorian_warrior", "mandalorian_marksman",
            "mandalorian_heavy", "mandalorian_clansperson");
    private static final Set<String> LICENSED_CLONE_ARMOR = Set.of(
            "phase_i_clone", "republic_plastoid");
    private static final List<String> HUMANOID_BONES = List.of(
            "head", "body", "right_arm", "left_arm", "right_leg", "left_leg",
            "RightHandItem", "LeftHandItem");
    private static final List<String> ARMOR_BONES = List.of(
            "armorHead", "armorBody", "armorRightArm", "armorLeftArm",
            "armorRightLeg", "armorLeftLeg", "armorRightBoot", "armorLeftBoot");

    private RecruitTextureAtlasTest() {
    }

    public static void main(String[] args) throws IOException {
        for (String recruit : RECRUITS) {
            validatesRecruitAssetSet(recruit);
        }
        for (String family : ARMOR_FAMILIES) {
            validatesArmorAssetSet(family);
        }
        validatesDistinctAssetCount(RECRUITS, "textures/entity/", ".png", 24, "recruit textures");
        validatesDistinctAssets(RECRUITS, "textures/item/", "_spawn_egg.png", "spawn eggs");
        validatesDistinctAssets(ARMOR_FAMILIES, "textures/armor/", ".png", "armor textures");
        validatesDistinctAssets(
                DATHOMIR_RECRUITS,
                "geckolib/models/entity/",
                ".geo.json",
                "Dathomir silhouettes");
        validatesDistinctAssets(
                DATHOMIR_RECRUITS,
                "textures/entity/",
                ".png",
                "Dathomir textures");
        validatesDistinctAssets(
                DATHOMIR_RECRUITS,
                "geckolib/animations/entity/",
                ".animation.json",
                "Dathomir animation sets");
        validatesAnimationFamiliesDiffer();
        validatesCommanderTextureVariants();
        validatesAuthorizedSourceAssets();
        validatesRendererAndProvenance();
        System.out.println("RecruitTextureAtlasTest passed");
    }

    private static void validatesRecruitAssetSet(String recruit) throws IOException {
        Path texturePath = ASSET_ROOT.resolve("textures/entity/" + recruit + ".png");
        Path modelPath = ASSET_ROOT.resolve("geckolib/models/entity/" + recruit + ".geo.json");
        int width = LICENSED_128_BY_64_RECRUITS.contains(recruit) ? 128
                : LICENSED_128_RECRUITS.contains(recruit) ? 128 : PROJECT_ATLAS_SIZE;
        int height = LICENSED_128_BY_64_RECRUITS.contains(recruit) ? 64 : width;
        boolean licensedSource = AUTHORIZED_SOURCE_RECRUITS.contains(recruit);
        BufferedImage image = readAtlas(texturePath, recruit, width, height);
        String geometry = geometry(modelPath, width, height);
        validatesGeometry(
                recruit,
                geometry,
                image,
                HUMANOID_BONES,
                minimumRecruitCubes(recruit),
                licensedSource ? 0 : RECRUIT_TEXEL_DENSITY
        );
        assertMinimumOpaqueColors(image, licensedSource ? 10 : 64,
                recruit + " material and wear variation");

        String animation = Files.readString(
                ASSET_ROOT.resolve("geckolib/animations/entity/" + recruit + ".animation.json"));
        validatesAnimationBonesExist(recruit, geometry, animation);
        for (String bone : HUMANOID_BONES.subList(0, 6)) {
            assertContains(animation, '"' + bone + '"', recruit + " animation bone " + bone);
        }
        assertContains(animation, "\"misc.idle\"", recruit + " idle animation");
        assertContains(animation, "\"move.walk\"", recruit + " walk animation");
        assertContains(animation, "\"move.run\"", recruit + " run animation");
        assertContains(animation, "\"attack.swing\"", recruit + " attack animation");
        validatesNamedSilhouette(recruit, geometry);
        validatesRigPivots(recruit, geometry);
        validatesIdleDuration(recruit, animation);

        BufferedImage egg = ImageIO.read(ASSET_ROOT.resolve(
                "textures/item/" + recruit + "_spawn_egg.png").toFile());
        assertNotNull(egg, recruit + " spawn egg decodes");
        assertEquals(16, egg.getWidth(), recruit + " spawn egg width");
        assertEquals(16, egg.getHeight(), recruit + " spawn egg height");
        assertRegularFile(ASSET_ROOT.resolve("items/" + recruit + "_spawn_egg.json"));
        assertRegularFile(ASSET_ROOT.resolve("models/item/" + recruit + "_spawn_egg.json"));
    }

    private static int minimumRecruitCubes(String recruit) {
        return switch (recruit) {
            case "phase_i_clone_trooper" -> 24;
            case "phase_i_arc_trooper" -> 30;
            case "clone_trooper" -> 27;
            case "arc_trooper" -> 33;
            case "b1_battle_droid" -> 23;
            case "b1_security_droid", "separatist_technician" -> 23;
            case "senate_commando" -> 22;
            case "republic_honor_guard" -> 21;
            case "togruta_civilian", "smuggler" -> 16;
            case "hutt_civilian" -> 17;
            case "hutt_enforcer" -> 15;
            case "mandalorian_warrior" -> 23;
            case "mandalorian_marksman" -> 16;
            case "mandalorian_heavy" -> 33;
            case "mandalorian_clansperson" -> 14;
            default -> 20;
        };
    }

    private static void validatesNamedSilhouette(String recruit, String geometry) {
        List<String> requiredParts;
        if (recruit.equals("clone_trooper") || recruit.equals("phase_i_clone_trooper")) {
            requiredParts = List.of(
                    "helmet", "chest_armor", "right_gauntlet", "left_gauntlet",
                    "right_boot", "left_boot");
        } else if (recruit.equals("arc_trooper") || recruit.equals("phase_i_arc_trooper")) {
            requiredParts = List.of(
                    "helmet", "chest_armor", "right_gauntlet", "left_gauntlet",
                    "right_boot", "left_boot", "rangefinder", "pauldron", "kama");
        } else if (Set.of("b1_battle_droid", "b1_security_droid", "separatist_technician",
                "b2_super_battle_droid", "commando_droid")
                .contains(recruit)) {
            requiredParts = List.of(
                    "neck", "right_forearm", "left_forearm", "right_shin", "left_shin");
            if (recruit.equals("b2_super_battle_droid")) {
                requiredParts = List.of("neck", "right_forearm", "left_forearm", "right_shin",
                        "left_shin", "forearm_cannon");
            } else if (recruit.equals("commando_droid")) {
                requiredParts = List.of("neck", "right_forearm", "left_forearm", "right_shin",
                        "left_shin", "commando_head_profile");
            }
        } else if (recruit.equals("togruta_civilian")) {
            requiredParts = List.of(
                    "right_montral", "left_montral", "center_lekku", "right_lekku", "left_lekku");
        } else if (recruit.equals("smuggler")) {
            requiredParts = List.of("duros_cranium", "duros_brow");
        } else if (recruit.equals("hutt_civilian")) {
            requiredParts = List.of("rodian_muzzle", "right_antenna", "left_antenna");
        } else if (recruit.equals("hutt_enforcer")) {
            requiredParts = List.of("trandoshan_muzzle", "right_brow_scale", "left_brow_scale");
        } else if (recruit.startsWith("mandalorian_")) {
            requiredParts = List.of("t_visor_brow", "t_visor_drop", "rangefinder", "jetpack");
        } else if (recruit.equals("nightsister_acolyte")) {
            requiredParts = List.of(
                    "robe_mantle", "robe_skirt", "cloth_panels",
                    "ritual_spires", "talisman_cords");
        } else if (recruit.equals("nightsister_archer")) {
            requiredParts = List.of(
                    "robe_mantle", "robe_skirt", "cloth_panels",
                    "quiver", "hair_locks", "archer_sash");
        } else if (recruit.equals("jedi_knight")) {
            requiredParts = List.of("robe_mantle", "robe_skirt", "cloth_panels");
        } else if (recruit.equals("nightbrother_brute")) {
            requiredParts = List.of(
                    "horns", "face_tattoo_vertical", "shoulder_harness", "waist_cloth");
        } else if (recruit.equals("nightsister_civilian")) {
            requiredParts = List.of("shawl", "woven_apron", "satchel");
        } else {
            return;
        }
        for (String part : requiredParts) {
            assertContains(geometry, "\"name\": \"" + part + "\"",
                    recruit + " named silhouette component " + part);
        }
    }

    private static void validatesAnimationBonesExist(
            String recruit,
            String geometry,
            String animation
    ) {
        Set<String> modelBones = new HashSet<>();
        Matcher modelBone = BONE_NAME.matcher(geometry);
        while (modelBone.find()) {
            modelBones.add(modelBone.group(1));
        }
        if (modelBones.size() <= HUMANOID_BONES.size()) {
            throw new AssertionError(recruit + " must use family-specific child bones; found " + modelBones);
        }
        Matcher animatedBone = ANIMATED_BONE.matcher(animation);
        int animatedCount = 0;
        while (animatedBone.find()) {
            String bone = animatedBone.group(1);
            if (!modelBones.contains(bone)) {
                throw new AssertionError(recruit + " animation references missing model bone " + bone);
            }
            animatedCount++;
        }
        if (animatedCount == 0) {
            throw new AssertionError(recruit + " animation contains no bone channels");
        }
    }

    private static void validatesAnimationFamiliesDiffer() throws IOException {
        List<String> representatives = List.of(
                "phase_i_clone_trooper",
                "clone_trooper",
                "b1_battle_droid",
                "commando_droid",
                "mandalorian_warrior",
                "jedi_knight",
                "nightsister_acolyte",
                "nightbrother_brute",
                "hutt_enforcer",
                "republic_civilian");
        validatesDistinctAssets(
                representatives,
                "geckolib/animations/entity/",
                ".animation.json",
                "humanoid, droid, brute, robed, civilian, and clone animation families");
    }

    private static void validatesRigPivots(String recruit, String geometry) {
        if (recruit.equals("arc_trooper") || recruit.equals("phase_i_arc_trooper")) {
            assertBonePivot(geometry, "kama", 0, 12, 0, recruit);
        }
        if (recruit.startsWith("mandalorian_")) {
            assertBonePivot(geometry, "helmet", 0, 24, 0, recruit);
        }
        if (recruit.equals("republic_honor_guard") || recruit.equals("togruta_civilian")) {
            assertBonePivot(geometry, "head", 0, 24, 0, recruit);
        }
        if (recruit.equals("hutt_enforcer")) {
            for (String bone : List.of("right_arm", "left_arm", "right_leg", "left_leg")) {
                assertContains(geometry, "\"name\": \"" + bone + "\"",
                        recruit + " canonical limb bone " + bone);
            }
            assertContains(geometry, "\"parent\": \"right_arm\"",
                    recruit + " right arm overlays and held-item anchor");
            assertContains(geometry, "\"parent\": \"left_arm\"",
                    recruit + " left arm overlays and held-item anchor");
            assertContains(geometry, "\"parent\": \"right_leg\"",
                    recruit + " right leg overlay");
            assertContains(geometry, "\"parent\": \"left_leg\"",
                    recruit + " left leg overlay");
        }
    }

    private static void validatesIdleDuration(String recruit, String animation) {
        int idleName = animation.indexOf("\"misc.idle\"");
        int idleStart = animation.indexOf('{', idleName);
        if (idleName < 0 || idleStart < 0) {
            throw new AssertionError(recruit + " idle animation object missing");
        }
        String idle = animation.substring(idleStart, matchingBrace(animation, idleStart) + 1);
        Pattern idleLength = Pattern.compile(
                "\\\"animation_length\\\"\\s*:\\s*([0-9.]+)");
        Matcher lengthMatcher = idleLength.matcher(idle);
        if (!lengthMatcher.find()) {
            throw new AssertionError(recruit + " idle animation length declaration missing");
        }
        double animationLength = Double.parseDouble(lengthMatcher.group(1));
        Matcher timestamps = Pattern.compile("\\\"([0-9]+(?:\\.[0-9]+)?)\\\"\\s*:").matcher(idle);
        double lastKeyframe = -1;
        while (timestamps.find()) {
            lastKeyframe = Math.max(lastKeyframe, Double.parseDouble(timestamps.group(1)));
        }
        assertDoubleEquals(animationLength, lastKeyframe,
                recruit + " idle duration must end with its final keyframes");
    }

    private static void assertBonePivot(
            String geometry,
            String bone,
            double expectedX,
            double expectedY,
            double expectedZ,
            String recruit
    ) {
        Pattern pivot = Pattern.compile(
                "\\\"name\\\"\\s*:\\s*\\\"" + Pattern.quote(bone)
                        + "\\\"\\s*,\\s*\\\"pivot\\\"\\s*:\\s*\\[\\s*"
                        + "(-?[0-9.]+)\\s*,\\s*(-?[0-9.]+)\\s*,\\s*(-?[0-9.]+)\\s*]",
                Pattern.DOTALL);
        Matcher matcher = pivot.matcher(geometry);
        if (!matcher.find()) {
            throw new AssertionError(recruit + " " + bone + " pivot declaration missing");
        }
        assertDoubleEquals(expectedX, Double.parseDouble(matcher.group(1)), recruit + " " + bone + " pivot x");
        assertDoubleEquals(expectedY, Double.parseDouble(matcher.group(2)), recruit + " " + bone + " pivot y");
        assertDoubleEquals(expectedZ, Double.parseDouble(matcher.group(3)), recruit + " " + bone + " pivot z");
    }

    private static void validatesArmorAssetSet(String family) throws IOException {
        Path texturePath = ASSET_ROOT.resolve("textures/armor/" + family + ".png");
        Path modelPath = ASSET_ROOT.resolve("geckolib/models/armor/" + family + ".geo.json");
        boolean licensedClone = LICENSED_CLONE_ARMOR.contains(family);
        int atlasSize = licensedClone ? 128 : PROJECT_ARMOR_ATLAS_SIZE;
        BufferedImage image = readAtlas(texturePath, family + " armor", atlasSize, atlasSize);
        String geometry = geometry(modelPath, atlasSize, atlasSize);
        validatesGeometry(
                family + " armor",
                geometry,
                image,
                ARMOR_BONES,
                family.equals("phase_i_clone") ? 24
                        : family.equals("republic_plastoid") ? 27
                        : family.equals("nightsister_weave") ? 33 : 50,
                licensedClone ? 0 : ARMOR_TEXEL_DENSITY
        );
        assertMinimumOpaqueColors(image, licensedClone ? 20 : 48,
                family + " high-detail material variation");
        assertArmorBoneParent(geometry, "armorHead", "bipedHead", family);
        assertArmorBoneParent(geometry, "armorBody", "bipedBody", family);
        assertArmorBoneParent(geometry, "armorRightArm", "bipedRightArm", family);
        assertArmorBoneParent(geometry, "armorLeftArm", "bipedLeftArm", family);
        assertArmorBoneParent(geometry, "armorRightLeg", "bipedRightLeg", family);
        assertArmorBoneParent(geometry, "armorRightBoot", "bipedRightLeg", family);
        assertArmorBoneParent(geometry, "armorLeftLeg", "bipedLeftLeg", family);
        assertArmorBoneParent(geometry, "armorLeftBoot", "bipedLeftLeg", family);
        validatesArmorIdentity(family, geometry);
        String animation = Files.readString(ASSET_ROOT.resolve(
                "geckolib/animations/armor/" + family + ".animation.json"));
        assertContains(animation, "\"armor.pose\"", family + " valid no-op armor animation");
        if (animation.matches("(?s).*\"animations\"\\s*:\\s*\\{\\s*}.*")) {
            throw new AssertionError(family + " armor animation table must not be empty");
        }
    }

    private static void validatesArmorIdentity(String family, String geometry) throws IOException {
        if (LICENSED_CLONE_ARMOR.contains(family)) {
            validatesLicensedCloneArmorIdentity(family, geometry);
            return;
        }
        String generator = Files.readString(Path.of("tools/generate_character_models.py"));
        String familySource = armorIdentitySource(generator, family);
        assertContains(geometry, "\"name\": \"armorHead\"",
                family + " equipped helmet bone");
        List<String> requiredParts = switch (family) {
            case "separatist_alloy" -> List.of(
                    "crown_reinforcement", "sensor_visor", "face_guard", "chin_filter",
                    "right_sensor_pod", "rear_power_node", "rear_reactor");
            case "mandalorian_alloy" -> List.of(
                    "visor_bar", "vertical_visor", "right_cheek_plate", "rangefinder_stem",
                    "rangefinder_sensor", "rear_filter", "compact_pack");
            case "nightsister_weave" -> List.of(
                    "mask_brow", "mask_visor", "mask_spine", "hood_crown",
                    "hood_back", "hood_right", "ritual_sash", "front_tabard");
            case "beskar" -> List.of(
                    "visor_bar", "vertical_visor", "right_cheek_plate", "rear_filter",
                    "heavy_collar", "right_heavy_pauldron");
            default -> throw new AssertionError("unmapped armor family " + family);
        };
        for (String part : requiredParts) {
            assertContains(familySource, "\"" + part + "\"",
                    family + " reproducible authored armor component " + part);
        }
    }

    private static void validatesLicensedCloneArmorIdentity(String family, String geometry)
            throws IOException {
        String importer = Files.readString(Path.of("tools/import_authorized_character_assets.py"));
        assertContains(geometry, "\"name\": \"helmet\"", family + " authored helmet group");
        assertContains(importer, "Clone Trooper.bbmodel", family + " pinned Blockbench source");
        assertContains(importer, "clone_group_policy(phase, False)", family + " phase selection");
        if (family.equals("phase_i_clone")) {
            assertContains(importer, "(\"phase_i_clone\", 1)", "Phase I armor selection");
        } else {
            assertContains(importer, "(\"republic_plastoid\", 2)", "Phase II armor selection");
            assertContains(importer, "forge_501st_blue", "permissioned 501st source palette");
        }
    }

    private static String armorIdentitySource(String generator, String family) {
        int functionStart = generator.indexOf("def add_family_armor_details(");
        if (functionStart < 0) {
            throw new AssertionError("armor family generator function missing");
        }
        String marker = (family.equals("republic_plastoid") ? "    if" : "    elif")
                + " family == \"" + family + "\":";
        int branchStart = generator.indexOf(marker, functionStart);
        if (branchStart < 0) {
            throw new AssertionError(family + " armor generator branch missing");
        }
        int nextBranch = generator.indexOf("\n    elif family ==", branchStart + marker.length());
        int elseBranch = generator.indexOf("\n    else:", branchStart + marker.length());
        int branchEnd = nextBranch < 0 ? elseBranch : Math.min(nextBranch, elseBranch);
        if (branchEnd < 0) {
            throw new AssertionError(family + " armor generator branch is unterminated");
        }

        String helper = switch (family) {
            case "republic_plastoid" -> "add_republic_helmet";
            case "phase_i_clone" -> "add_phase_i_clone_helmet";
            case "separatist_alloy" -> "add_separatist_helmet";
            case "mandalorian_alloy", "beskar" -> "add_mandalorian_helmet";
            case "nightsister_weave" -> "add_nightsister_helmet";
            default -> throw new AssertionError("unmapped armor family " + family);
        };
        return generator.substring(branchStart, branchEnd) + functionSource(generator, helper);
    }

    private static String functionSource(String source, String functionName) {
        int start = source.indexOf("def " + functionName + "(");
        if (start < 0) {
            throw new AssertionError(functionName + " generator helper missing");
        }
        int end = source.indexOf("\ndef ", start + 1);
        return end < 0 ? source.substring(start) : source.substring(start, end);
    }

    private static void assertArmorBoneParent(
            String geometry,
            String bone,
            String parent,
            String family
    ) {
        Pattern parentedBone = Pattern.compile(
                "\\\"name\\\"\\s*:\\s*\\\"" + Pattern.quote(bone)
                        + "\\\"(?s:.*?)\\\"parent\\\"\\s*:\\s*\\\""
                        + Pattern.quote(parent) + "\\\"");
        if (!parentedBone.matcher(geometry).find()) {
            throw new AssertionError(family + " " + bone + " must be parented to " + parent);
        }
    }

    private static BufferedImage readAtlas(
            Path path,
            String label,
            int expectedWidth,
            int expectedHeight
    ) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        assertNotNull(image, label + " texture decodes");
        assertEquals(expectedWidth, image.getWidth(), label + " width");
        assertEquals(expectedHeight, image.getHeight(), label + " height");
        boolean transparentPixel = false;
        boolean opaquePixel = false;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = image.getRGB(x, y) >>> 24;
                transparentPixel |= alpha == 0;
                opaquePixel |= alpha == 255;
            }
        }
        if (!transparentPixel || !opaquePixel) {
            throw new AssertionError(label + " atlas must contain painted faces and transparent unused space");
        }
        return image;
    }

    private static String geometry(Path path, int expectedWidth, int expectedHeight) throws IOException {
        String model = Files.readString(path);
        assertPatternValue(TEXTURE_WIDTH, model, expectedWidth, path + " texture width");
        assertPatternValue(TEXTURE_HEIGHT, model, expectedHeight, path + " texture height");
        return model;
    }

    private static void validatesGeometry(
            String label,
            String geometry,
            BufferedImage image,
            List<String> requiredBones,
            int minimumCubeCount,
            int texelDensity
    ) {
        Set<String> missing = new HashSet<>();
        for (String requiredBone : requiredBones) {
            if (!geometry.contains("\"name\": \"" + requiredBone + "\"")) {
                missing.add(requiredBone);
            }
        }
        if (!missing.isEmpty()) {
            throw new AssertionError(label + " missing required bones " + missing);
        }
        Matcher cubes = CUBE.matcher(geometry);
        int cubeCount = 0;
        while (cubes.find()) {
            double rawWidth = Double.parseDouble(cubes.group(1));
            double rawHeight = Double.parseDouble(cubes.group(2));
            double rawDepth = Double.parseDouble(cubes.group(3));
            if (rawWidth <= 0 || rawHeight <= 0 || rawDepth <= 0) {
                throw new AssertionError(label + " cube " + cubeCount + " is degenerate");
            }
            int width = (int) Math.ceil(rawWidth);
            int height = (int) Math.ceil(rawHeight);
            int depth = (int) Math.ceil(rawDepth);
            int uvStart = skipWhitespace(geometry, cubes.end());
            if (uvStart < geometry.length() && geometry.charAt(uvStart) == '[') {
                Matcher origin = BOX_UV.matcher(geometry);
                origin.region(uvStart, geometry.length());
                if (!origin.lookingAt()) {
                    throw new AssertionError(label + " cube " + cubeCount + " must use box UVs");
                }
                BoxUv box = new BoxUv(
                        Integer.parseInt(origin.group(1)), Integer.parseInt(origin.group(2)),
                        width, height, depth);
                if (box.u() < 0 || box.v() < 0
                        || box.u() + 2 * box.depth() + 2 * box.width() > image.getWidth()
                        || box.v() + box.depth() + box.height() > image.getHeight()) {
                    throw new AssertionError(label + " cube " + cubeCount + " extends outside its atlas");
                }
                assertOpaqueBoxFaces(image, box, label + " cube " + cubeCount);
            } else {
                if (uvStart >= geometry.length() || geometry.charAt(uvStart) != '{') {
                    throw new AssertionError(label + " cube " + cubeCount
                            + " must use high-density per-face UVs");
                }
                int uvEnd = matchingBrace(geometry, uvStart);
                String perFaceUvs = geometry.substring(uvStart, uvEnd + 1);
                if (texelDensity == 0) {
                    assertLicensedPerFaceUvs(
                            image,
                            perFaceUvs,
                            label + " cube " + cubeCount
                    );
                } else {
                    assertOpaquePerFaceUvs(
                            image,
                            perFaceUvs,
                            width,
                            height,
                            depth,
                            texelDensity,
                            label + " cube " + cubeCount
                    );
                }
            }
            cubeCount++;
        }
        if (cubeCount < minimumCubeCount) {
            throw new AssertionError(label + " has only " + cubeCount
                    + " cubes; expected at least " + minimumCubeCount + " for a detailed silhouette");
        }
    }

    private static void assertOpaquePerFaceUvs(
            BufferedImage image,
            String uv,
            int width,
            int height,
            int depth,
            int density,
            String label
    ) {
        assertOpaqueFace(image, uv, "north", width * density, height * density, label);
        assertOpaqueFace(image, uv, "south", width * density, height * density, label);
        assertOpaqueFace(image, uv, "east", depth * density, height * density, label);
        assertOpaqueFace(image, uv, "west", depth * density, height * density, label);
        assertOpaqueFace(image, uv, "up", width * density, depth * density, label);
        assertOpaqueFace(image, uv, "down", width * density, depth * density, label);
    }

    private static void assertLicensedPerFaceUvs(
            BufferedImage image,
            String uv,
            String label
    ) {
        int paintedFaces = 0;
        for (String faceName : List.of("north", "south", "east", "west", "up", "down")) {
            Pattern facePattern = Pattern.compile(
                    "\\\"" + Pattern.quote(faceName) + "\\\"\\s*:\\s*\\{\\s*"
                            + "\\\"uv\\\"\\s*:\\s*\\[\\s*(-?[0-9.]+)\\s*,\\s*(-?[0-9.]+)\\s*]\\s*,\\s*"
                            + "\\\"uv_size\\\"\\s*:\\s*\\[\\s*(-?[0-9.]+)\\s*,\\s*(-?[0-9.]+)\\s*]",
                    Pattern.DOTALL);
            Matcher face = facePattern.matcher(uv);
            if (!face.find()) {
                throw new AssertionError(label + " missing " + faceName + " UV mapping");
            }
            double x1 = Double.parseDouble(face.group(1));
            double y1 = Double.parseDouble(face.group(2));
            double x2 = x1 + Double.parseDouble(face.group(3));
            double y2 = y1 + Double.parseDouble(face.group(4));
            int left = (int) Math.floor(Math.min(x1, x2));
            int right = (int) Math.ceil(Math.max(x1, x2));
            int top = (int) Math.floor(Math.min(y1, y2));
            int bottom = (int) Math.ceil(Math.max(y1, y2));
            if (left < 0 || top < 0 || right > image.getWidth() || bottom > image.getHeight()
                    || left == right || top == bottom) {
                throw new AssertionError(label + " " + faceName + " extends outside its source atlas");
            }
            if (hasOpaquePixel(image, left, top, right - left, bottom - top)) {
                paintedFaces++;
            }
        }
        if (paintedFaces == 0) {
            throw new AssertionError(label + " maps only transparent source pixels");
        }
    }

    private static void assertOpaqueFace(
            BufferedImage image,
            String uv,
            String faceName,
            int expectedWidth,
            int expectedHeight,
            String label
    ) {
        Pattern facePattern = Pattern.compile(
                "\\\"" + Pattern.quote(faceName) + "\\\"\\s*:\\s*\\{\\s*"
                        + "\\\"uv\\\"\\s*:\\s*\\[\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*]\\s*,\\s*"
                        + "\\\"uv_size\\\"\\s*:\\s*\\[\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*]",
                Pattern.DOTALL);
        Matcher face = facePattern.matcher(uv);
        if (!face.find()) {
            throw new AssertionError(label + " missing " + faceName + " UV mapping");
        }
        int x = Integer.parseInt(face.group(1));
        int y = Integer.parseInt(face.group(2));
        int width = Integer.parseInt(face.group(3));
        int height = Integer.parseInt(face.group(4));
        assertEquals(expectedWidth, width, label + " " + faceName + " UV width");
        assertEquals(expectedHeight, height, label + " " + faceName + " UV height");
        if (x < 0 || y < 0 || x + width > image.getWidth() || y + height > image.getHeight()) {
            throw new AssertionError(label + " " + faceName + " extends outside its atlas");
        }
        assertOpaque(image, x, y, width, height, label + " " + faceName);
    }

    private static int skipWhitespace(String value, int index) {
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }

    private static int matchingBrace(String value, int openingBrace) {
        int depth = 0;
        boolean insideString = false;
        boolean escaped = false;
        for (int index = openingBrace; index < value.length(); index++) {
            char character = value.charAt(index);
            if (insideString) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '"') {
                    insideString = false;
                }
                continue;
            }
            if (character == '"') {
                insideString = true;
            } else if (character == '{') {
                depth++;
            } else if (character == '}' && --depth == 0) {
                return index;
            }
        }
        throw new AssertionError("Unclosed per-face UV object at character " + openingBrace);
    }

    private static void assertMinimumOpaqueColors(BufferedImage image, int minimum, String label) {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getRGB(x, y);
                if ((color >>> 24) == 255) {
                    colors.add(color & 0x00ffffff);
                }
            }
        }
        if (colors.size() < minimum) {
            throw new AssertionError(label + " expected at least " + minimum
                    + " opaque colors but found " + colors.size());
        }
    }

    private static void validatesRendererAndProvenance() throws IOException {
        String recruitRenderer = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/client/render/GalacticRecruitRenderer.java"));
        String armorItem = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/item/GalacticArmorItem.java"));
        String items = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/registry/ModItems.java"));
        String entities = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/registry/ModEntityTypes.java"));
        String provenance = Files.readString(Path.of("docs/galacticwars-asset-provenance.md"));
        String turnaroundRenderer = Files.readString(Path.of("tools/render_asset_turnarounds.py"));
        String turnaroundRendererTest = Files.readString(Path.of(
                "tools/test_render_asset_turnarounds.py"));
        assertContains(recruitRenderer, "ItemInHandGeoLayer", "held item render layer");
        assertContains(armorItem, "implements GeoItem", "custom equipped armor item");
        assertContains(armorItem, "getGeoArmorRenderer", "custom equipped armor provider");
        assertContains(items, "new GalacticArmorItem", "armor registry uses custom geometry");
        assertNotContains(entities, "renderAlias", "civilian renderer aliases");
        assertContains(provenance, "generate_character_models.py", "character model provenance");
        assertContains(provenance, "import_authorized_character_assets.py",
                "authorized character importer provenance");
        assertContains(provenance, "b91b4cc1a827eeb7c2ae16f0b703affd78c1c206",
                "Galaxies pinned source commit");
        assertContains(provenance, "c9555aa4966e9e63c22a59f488d4b05bc614569e",
                "Forge pinned source commit");
        assertContains(turnaroundRenderer, "all_27_recruits.png", "27-recruit turnaround output");
        assertContains(turnaroundRenderer, "face_texture(", "turnaround UV pixel sampling");
        assertContains(turnaroundRenderer, "Image.Transform.AFFINE",
                "turnaround projected UV mapping");
        assertNotContains(turnaroundRenderer, "face_color(", "averaged turnaround face colors");
        assertContains(turnaroundRendererTest,
                "assert_rotated_cube_renders_actual_texture_pixels",
                "rotated cuboid texture-sampling QA");
        assertContains(turnaroundRendererTest, "len(RECRUIT_ASSETS) != 27",
                "27-recruit turnaround catalog QA");
        for (String recruit : RECRUITS) {
            assertContains(
                    turnaroundRenderer,
                    '"' + recruit + '"',
                    recruit + " turnaround entry");
        }
        String visualProfiles = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/client/render/RecruitVisualProfileCatalog.java"));
        assertContains(visualProfiles, "Map<EntityType<?>, String>",
                "commander visual profiles keyed by registered entity type");
        assertContains(visualProfiles, "Identifier textureResource(",
                "visual profile returns a texture identifier");
        for (String commander : List.of("CLONE_TROOPER", "ARC_TROOPER", "B1_BATTLE_DROID")) {
            assertContains(visualProfiles, "ModEntityTypes." + commander + ".get()",
                    commander + " commander texture override");
        }
    }

    private static void validatesAuthorizedSourceAssets() throws IOException {
        Path sourceRoot = Path.of("tools/source_art/authorized_upstream");
        Path galaxies = sourceRoot.resolve("galaxies_pswg");
        Path forge = sourceRoot.resolve("forge_star_wars_clone_wars");
        for (Path source : List.of(
                galaxies.resolve("Clone Trooper.bbmodel"),
                galaxies.resolve("Clone Trooper.png"),
                galaxies.resolve("PSWG_Mandalorian.bbmodel"),
                galaxies.resolve("PSWG_Mandalorian.png"),
                galaxies.resolve("HonorGuard.bbmodel"),
                galaxies.resolve("SenateCommando.bbmodel"),
                galaxies.resolve("HumanoidBody.bbmodel"),
                galaxies.resolve("Togruta.bbmodel"),
                galaxies.resolve("Duros.bbmodel"),
                galaxies.resolve("Rodian.bbmodel"),
                galaxies.resolve("Trandoshan.bbmodel"),
                forge.resolve("Droid.bbmodel"),
                forge.resolve("droid.png"),
                forge.resolve("droid_security.png"),
                forge.resolve("droid_pilot.png"),
                forge.resolve("droid_commander.png"),
                forge.resolve("clone_armor_armor_501st_layer_helmet.png"))) {
            assertRegularFile(source);
        }
        assertImagesEqual(
                galaxies.resolve("Clone Trooper.png"),
                ASSET_ROOT.resolve("textures/entity/phase_i_clone_trooper.png"),
                "Phase I clone source texture");
        assertImageDimensionsEqual(
                forge.resolve("droid.png"),
                ASSET_ROOT.resolve("textures/entity/b1_battle_droid.png"),
                "B1 derived texture");
        assertImageDimensionsEqual(
                forge.resolve("droid_security.png"),
                ASSET_ROOT.resolve("textures/entity/b1_security_droid.png"),
                "B1 security derived texture");
        assertImageDimensionsEqual(
                forge.resolve("droid_pilot.png"),
                ASSET_ROOT.resolve("textures/entity/separatist_technician.png"),
                "B1 pilot derived texture");
    }

    private static void assertImagesEqual(Path expectedPath, Path actualPath, String label)
            throws IOException {
        BufferedImage expected = ImageIO.read(expectedPath.toFile());
        BufferedImage actual = ImageIO.read(actualPath.toFile());
        assertNotNull(expected, label + " source decodes");
        assertNotNull(actual, label + " output decodes");
        assertEquals(expected.getWidth(), actual.getWidth(), label + " width");
        assertEquals(expected.getHeight(), actual.getHeight(), label + " height");
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                if (expected.getRGB(x, y) != actual.getRGB(x, y)) {
                    throw new AssertionError(label + " differs at " + x + "," + y);
                }
            }
        }
    }

    private static void validatesCommanderTextureVariants() throws IOException {
        for (String recruit : List.of("clone_trooper", "arc_trooper", "b1_battle_droid")) {
            int width = recruit.equals("b1_battle_droid") ? 128 : 128;
            int height = recruit.equals("b1_battle_droid") ? 64 : 128;
            BufferedImage texture = readAtlas(
                    ASSET_ROOT.resolve("textures/entity/" + recruit + "_commander.png"),
                    recruit + " commander",
                    width,
                    height
            );
            String model = geometry(
                    ASSET_ROOT.resolve("geckolib/models/entity/" + recruit + ".geo.json"),
                    width,
                    height
            );
            validatesGeometry(
                    recruit + " commander",
                    model,
                    texture,
                    HUMANOID_BONES,
                    minimumRecruitCubes(recruit),
                    0
            );
            if (java.util.Arrays.equals(
                    Files.readAllBytes(ASSET_ROOT.resolve("textures/entity/" + recruit + ".png")),
                    Files.readAllBytes(ASSET_ROOT.resolve(
                            "textures/entity/" + recruit + "_commander.png")))) {
                throw new AssertionError(recruit + " commander texture must differ from soldier texture");
            }
            if (!recruit.equals("b1_battle_droid")) {
                BufferedImage soldier = ImageIO.read(ASSET_ROOT.resolve(
                        "textures/entity/" + recruit + ".png").toFile());
                for (int y = 0; y < texture.getHeight(); y++) {
                    for (int x = 0; x < texture.getWidth(); x++) {
                        int soldierAlpha = soldier.getRGB(x, y) >>> 24;
                        int commanderAlpha = texture.getRGB(x, y) >>> 24;
                        if (soldierAlpha != commanderAlpha) {
                            throw new AssertionError(recruit
                                    + " commander must preserve soldier alpha mask at " + x + "," + y);
                        }
                    }
                }
            }
        }
    }

    private static void assertImageDimensionsEqual(Path expectedPath, Path actualPath, String label)
            throws IOException {
        BufferedImage expected = ImageIO.read(expectedPath.toFile());
        BufferedImage actual = ImageIO.read(actualPath.toFile());
        assertNotNull(expected, label + " source decodes");
        assertNotNull(actual, label + " output decodes");
        assertEquals(expected.getWidth(), actual.getWidth(), label + " width");
        assertEquals(expected.getHeight(), actual.getHeight(), label + " height");
    }

    private static void validatesDistinctAssets(
            List<String> ids,
            String prefix,
            String suffix,
            String label
    ) throws IOException {
        Set<Integer> hashes = new HashSet<>();
        for (String id : ids) {
            hashes.add(java.util.Arrays.hashCode(Files.readAllBytes(ASSET_ROOT.resolve(prefix + id + suffix))));
        }
        assertEquals(ids.size(), hashes.size(), label + " distinct hashes");
    }

    private static void validatesDistinctAssetCount(
            List<String> ids,
            String prefix,
            String suffix,
            int minimum,
            String label
    ) throws IOException {
        Set<Integer> hashes = new HashSet<>();
        for (String id : ids) {
            hashes.add(java.util.Arrays.hashCode(Files.readAllBytes(ASSET_ROOT.resolve(prefix + id + suffix))));
        }
        if (hashes.size() < minimum) {
            throw new AssertionError(label + " expected at least " + minimum
                    + " distinct hashes but found " + hashes.size());
        }
    }

    private static void assertOpaqueBoxFaces(BufferedImage image, BoxUv cube, String label) {
        assertOpaque(image, cube.u() + cube.depth(), cube.v(), cube.width(), cube.depth(), label + " top");
        assertOpaque(image, cube.u() + cube.depth() + cube.width(), cube.v(), cube.width(), cube.depth(), label + " bottom");
        assertOpaque(image, cube.u(), cube.v() + cube.depth(), cube.depth(), cube.height(), label + " west");
        assertOpaque(image, cube.u() + cube.depth(), cube.v() + cube.depth(), cube.width(), cube.height(), label + " front");
        assertOpaque(image, cube.u() + cube.depth() + cube.width(), cube.v() + cube.depth(), cube.depth(), cube.height(), label + " east");
        assertOpaque(image, cube.u() + cube.depth() * 2 + cube.width(), cube.v() + cube.depth(), cube.width(), cube.height(), label + " back");
    }

    private static void assertOpaque(BufferedImage image, int x, int y, int width, int height, String label) {
        for (int iy = y; iy < y + height; iy++) {
            for (int ix = x; ix < x + width; ix++) {
                if ((image.getRGB(ix, iy) >>> 24) != 255) {
                    throw new AssertionError(label + " has transparent pixel at " + ix + "," + iy);
                }
            }
        }
    }

    private static boolean hasOpaquePixel(BufferedImage image, int x, int y, int width, int height) {
        for (int iy = y; iy < y + height; iy++) {
            for (int ix = x; ix < x + width; ix++) {
                if ((image.getRGB(ix, iy) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void assertContains(String value, String expected, String label) {
        if (!value.contains(expected)) {
            throw new AssertionError(label + " missing <" + expected + ">");
        }
    }

    private static void assertNotContains(String value, String unexpected, String label) {
        if (value.contains(unexpected)) {
            throw new AssertionError(label + " still contains <" + unexpected + ">");
        }
    }

    private static void assertRegularFile(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new AssertionError("missing asset <" + path + ">");
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertDoubleEquals(double expected, double actual, String label) {
        if (Math.abs(expected - actual) > 0.00001) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertPatternValue(
            Pattern pattern,
            String value,
            int expected,
            String label
    ) {
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            throw new AssertionError(label + " declaration missing");
        }
        assertEquals(expected, Integer.parseInt(matcher.group(1)), label);
    }

    private static void assertNotNull(Object value, String label) {
        if (value == null) {
            throw new AssertionError(label + " was null");
        }
    }

    private record BoxUv(int u, int v, int width, int height, int depth) {
    }
}
