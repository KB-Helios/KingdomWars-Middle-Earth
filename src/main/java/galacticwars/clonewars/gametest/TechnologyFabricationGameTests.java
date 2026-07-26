package galacticwars.clonewars.gametest;

import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.fabrication.FabricationRecipe;
import galacticwars.clonewars.fabrication.FabricationService;
import galacticwars.clonewars.fabrication.FabricatorBlockEntity;
import galacticwars.clonewars.force.ForceRitualRecipe;
import galacticwars.clonewars.kingdom.KingdomMemberRole;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.menu.FabricatorMenu;
import galacticwars.clonewars.network.FabricationRequestPayload;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import galacticwars.clonewars.registry.ModBlocks;
import galacticwars.clonewars.registry.ModEntityTypes;
import galacticwars.clonewars.registry.ModItems;
import galacticwars.clonewars.settlement.CommandCenterBlockEntity;
import galacticwars.clonewars.technology.KingdomResearchService;
import galacticwars.clonewars.technology.KingdomTechnologyState;
import galacticwars.clonewars.technology.ResearchResult;
import galacticwars.clonewars.workforce.WorkerProfession;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/** Runtime coverage for datapack recipes, shared research, and atomic fabrication. */
public final class TechnologyFabricationGameTests {
    private static final List<String> BASELINE_RECIPES = List.of(
            "command_center",
            "credit_chip",
            "energy_cell",
            "claim_transponder",
            "command_marker",
            "republic_identity_chip",
            "separatist_identity_chip",
            "mandalorian_identity_chip",
            "hutt_cartel_identity_chip",
            "nightsister_identity_chip",
            "beskar_ore",
            "duracrete_stonecutting",
            "nightsister_weave_planks",
            "nightsister_weave_sapling",
            "jedi_meditation_shrine",
            "sith_holocron_pedestal",
            "nightsister_spirit_altar",
            "fabricator");
    private static final List<String> RITUAL_RECIPES = List.of(
            "blue_lightsaber",
            "green_lightsaber",
            "yellow_lightsaber",
            "purple_lightsaber",
            "white_lightsaber",
            "red_lightsaber");

    private TechnologyFabricationGameTests() {
    }

    public static void loadedRecipeCatalog(GameTestHelper helper) {
        var manager = helper.getLevel().getServer().getRecipeManager();
        if (GameplayDataManager.snapshot().technology().nodes().size() != 24
                || GameplayDataManager.snapshot().technology().recipes().size() != 67) {
            helper.fail("Reloaded technology catalog did not contain 24 nodes and 67 fabrication recipes");
            return;
        }
        for (String recipeId : GameplayDataManager.snapshot().technology().recipes()) {
            var holder = manager.byKey(recipeKey(recipeId)).orElse(null);
            if (holder == null || !(holder.value() instanceof FabricationRecipe)) {
                helper.fail("Technology recipe was not loaded as fabrication: " + recipeId);
                return;
            }
        }
        for (String path : BASELINE_RECIPES) {
            var holder = manager.byKey(recipeKey("galacticwars:" + path)).orElse(null);
            if (holder == null || holder.value() instanceof FabricationRecipe
                    || holder.value() instanceof ForceRitualRecipe) {
                helper.fail("Baseline recipe was missing or incorrectly gated: " + path);
                return;
            }
        }
        for (String path : RITUAL_RECIPES) {
            var holder = manager.byKey(recipeKey("galacticwars:" + path)).orElse(null);
            if (holder == null || !(holder.value() instanceof ForceRitualRecipe)) {
                helper.fail("Lightsaber recipe was not loaded as a Force ritual: " + path);
                return;
            }
        }
        helper.succeed();
    }

    public static void sharedKingdomResearch(GameTestHelper helper) {
        BlockPos hallPos = helper.absolutePos(new BlockPos(5, 1, 5)).offset(0, 0, 1_000_000);
        helper.getLevel().getChunkAt(hallPos);
        helper.getLevel().setBlock(
                hallPos, ModBlocks.COMMAND_CENTER.get().defaultBlockState(), 3);
        if (!(helper.getLevel().getBlockEntity(hallPos) instanceof CommandCenterBlockEntity hall)) {
            helper.fail("Could not place research Command Center");
            return;
        }
        ServerPlayer owner = mockPlayer(helper, GameType.SURVIVAL);
        ServerPlayer member = mockPlayer(helper, GameType.SURVIVAL);
        owner.setPos(hallPos.getX() + 0.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        member.setPos(hallPos.getX() + 1.5D, hallPos.getY(), hallPos.getZ() + 0.5D);
        if (!hall.claim(owner)) {
            helper.fail("Research owner could not claim the Command Center");
            return;
        }

        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(),
                "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(),
                hallPos);
        if (!data.addMember(
                owner.getUUID(), member.getUUID(), KingdomMemberRole.MEMBER, "")) {
            helper.fail("Could not add ordinary research contributor");
            return;
        }

        UUID startReplay = UUID.randomUUID();
        ResearchResult started = KingdomResearchService.start(
                owner, hall, "galacticwars:field_fabrication", startReplay, 0);
        if (!started.accepted() || !started.changed()) {
            helper.fail("Owner could not start physical research: " + started);
            return;
        }
        hall.setItem(0, new ItemStack(Items.IRON_INGOT, 16));
        hall.setItem(1, new ItemStack(Items.REDSTONE, 8));
        UUID contributionReplay = UUID.randomUUID();
        ResearchResult contributed = KingdomResearchService.contribute(
                member, hall, contributionReplay, started.revision());
        if (!contributed.accepted() || !contributed.changed()
                || !hall.getItem(0).isEmpty() || !hall.getItem(1).isEmpty()) {
            helper.fail("Ordinary member contribution was not committed atomically: " + contributed);
            return;
        }
        ResearchResult duplicateContribution = KingdomResearchService.contribute(
                member, hall, contributionReplay, contributed.revision());
        if (!duplicateContribution.accepted() || duplicateContribution.changed()
                || !"replay".equals(duplicateContribution.reason())) {
            helper.fail("Duplicate research contribution was not replay-safe: "
                    + duplicateContribution);
            return;
        }

        GalacticRecruitEntity technician = ModEntityTypes.CLONE_TROOPER.get().create(
                helper.getLevel(), EntitySpawnReason.EVENT);
        if (technician == null) {
            helper.fail("Could not create research technician");
            return;
        }
        technician.tame(owner);
        technician.setPos(
                hallPos.getX() + 1.5D,
                hallPos.getY(),
                hallPos.getZ() + 0.5D);
        if (!helper.getLevel().addFreshEntity(technician)) {
            helper.fail("Could not add research technician to the server level");
            return;
        }
        technician.setWorkerProfession(WorkerProfession.TECHNICIAN);
        if (!data.registerRecruit(owner.getUUID(), technician.getUUID(), NpcServiceBranch.CIVILIAN)) {
            helper.fail("Could not register research technician");
            return;
        }
        helper.runAfterDelay(1, () -> finishSharedKingdomResearch(
                helper,
                owner,
                member,
                hall,
                data,
                kingdom,
                technician,
                started,
                contributed));
    }

    private static void finishSharedKingdomResearch(
            GameTestHelper helper,
            ServerPlayer owner,
            ServerPlayer member,
            CommandCenterBlockEntity hall,
            KingdomSavedData data,
            KingdomRecord kingdom,
            GalacticRecruitEntity technician,
            ResearchResult started,
            ResearchResult contributed
    ) {
        ResearchResult staleAssignment = KingdomResearchService.assignTechnician(
                owner,
                hall,
                technician.getUUID(),
                UUID.randomUUID(),
                started.revision());
        if (staleAssignment.accepted() || !"stale_revision".equals(staleAssignment.reason())) {
            helper.fail("Stale technician assignment was accepted: " + staleAssignment);
            return;
        }
        ResearchResult assigned = KingdomResearchService.assignTechnician(
                owner,
                hall,
                technician.getUUID(),
                UUID.randomUUID(),
                contributed.revision());
        if (!assigned.accepted() || !assigned.changed()) {
            helper.fail("Valid technician assignment failed after entity registration: " + assigned);
            return;
        }

        for (int tick = 0; tick < 60; tick++) {
            if (!KingdomResearchService.tick(helper.getLevel(), hall)) {
                helper.fail("Technician research stalled at work tick " + tick);
                return;
            }
        }
        KingdomTechnologyState completed = data.technologyStateOrDefault(kingdom.id());
        if (!completed.completed("galacticwars:field_fabrication")
                || completed.activeProject().isPresent()
                || data.kingdomForPlayer(member.getUUID())
                        .filter(candidate -> candidate.id().equals(kingdom.id())).isEmpty()) {
            helper.fail("Research did not complete once for the whole kingdom: " + completed);
            return;
        }

        var encoded = KingdomSavedData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        KingdomSavedData restored = KingdomSavedData.CODEC.parse(
                NbtOps.INSTANCE, encoded).getOrThrow();
        if (!restored.technologyStateOrDefault(kingdom.id())
                .completed("galacticwars:field_fabrication")) {
            helper.fail("Completed research did not survive SavedData reload");
            return;
        }
        helper.succeed();
    }

    public static void atomicFabricationAuthority(GameTestHelper helper) {
        BlockPos fabricatorPos = helper.absolutePos(new BlockPos(5, 1, 5))
                .offset(0, 0, 1_020_000);
        helper.getLevel().getChunkAt(fabricatorPos);
        ServerPlayer owner = mockPlayer(helper, GameType.SURVIVAL);
        owner.setPos(
                fabricatorPos.getX() + 0.5D,
                fabricatorPos.getY(),
                fabricatorPos.getZ() + 0.5D);
        KingdomSavedData data = KingdomSavedData.get(helper.getLevel());
        KingdomRecord kingdom = data.foundKingdom(
                owner.getUUID(),
                "galacticwars:republic",
                helper.getLevel().dimension().identifier().toString(),
                fabricatorPos.offset(0, 0, 64));
        helper.getLevel().setBlock(
                fabricatorPos, ModBlocks.FABRICATOR.get().defaultBlockState(), 3);
        if (!(helper.getLevel().getBlockEntity(fabricatorPos)
                instanceof FabricatorBlockEntity fabricator)
                || !fabricator.bindOwner(owner)) {
            helper.fail("Could not place and bind kingdom Fabricator");
            return;
        }
        loadDc15Inputs(fabricator);

        FabricatorMenu lockedMenu = new FabricatorMenu(
                7,
                owner.getInventory(),
                fabricator,
                fabricatorPos,
                List.of("galacticwars:dc15_blaster"),
                GameplayDataManager.generation(),
                0);
        owner.containerMenu = lockedMenu;
        FabricationRequestPayload lockedRequest = new FabricationRequestPayload(
                UUID.randomUUID(),
                lockedMenu.containerId,
                "galacticwars:dc15_blaster",
                GameplayDataManager.generation(),
                0);
        var locked = FabricationService.fabricate(owner, lockedMenu, lockedRequest);
        if (locked.accepted() || !"technology_locked".equals(locked.reason())
                || fabricator.getItem(0).getCount() != 3
                || !fabricator.getItem(FabricatorBlockEntity.OUTPUT_SLOT).isEmpty()) {
            helper.fail("Locked fabrication changed physical inventory: " + locked);
            return;
        }

        KingdomTechnologyState unlocked = KingdomTechnologyState.empty(
                        kingdom.id(), kingdom.factionId())
                .grantMigrationNodes(Set.of(
                        "galacticwars:plastoid_processing",
                        "galacticwars:clone_field_arms"));
        if (!data.storeTechnologyState(unlocked, 0)) {
            helper.fail("Could not install completed kingdom technology");
            return;
        }
        FabricatorMenu unlockedMenu = new FabricatorMenu(
                8,
                owner.getInventory(),
                fabricator,
                fabricatorPos,
                List.of("galacticwars:dc15_blaster"),
                GameplayDataManager.generation(),
                unlocked.revision());
        owner.containerMenu = unlockedMenu;
        UUID fabricationReplay = UUID.randomUUID();
        FabricationRequestPayload request = new FabricationRequestPayload(
                fabricationReplay,
                unlockedMenu.containerId,
                "galacticwars:dc15_blaster",
                GameplayDataManager.generation(),
                unlocked.revision());
        var fabricated = FabricationService.fabricate(owner, unlockedMenu, request);
        if (!fabricated.accepted()
                || !fabricator.getItem(0).isEmpty()
                || !fabricator.getItem(1).isEmpty()
                || !fabricator.getItem(2).isEmpty()
                || !fabricator.getItem(3).isEmpty()
                || !fabricator.getItem(FabricatorBlockEntity.OUTPUT_SLOT)
                        .is(ModItems.DC15_BLASTER.get())) {
            helper.fail("Authorized fabrication was not atomic: " + fabricated);
            return;
        }
        var replay = FabricationService.fabricate(owner, unlockedMenu, request);
        if (replay.accepted() || !"replay".equals(replay.reason())
                || fabricator.getItem(FabricatorBlockEntity.OUTPUT_SLOT).getCount() != 1) {
            helper.fail("Duplicate fabrication request duplicated output: " + replay);
            return;
        }
        FabricationRequestPayload stale = new FabricationRequestPayload(
                UUID.randomUUID(),
                unlockedMenu.containerId,
                "galacticwars:dc15_blaster",
                GameplayDataManager.generation(),
                0);
        var staleResult = FabricationService.fabricate(owner, unlockedMenu, stale);
        if (staleResult.accepted() || !"stale_technology".equals(staleResult.reason())
                || fabricator.getItem(FabricatorBlockEntity.OUTPUT_SLOT).getCount() != 1) {
            helper.fail("Stale fabrication request changed output: " + staleResult);
            return;
        }
        helper.succeed();
    }

    private static void loadDc15Inputs(FabricatorBlockEntity fabricator) {
        fabricator.setItem(0, new ItemStack(Items.IRON_INGOT, 3));
        fabricator.setItem(1, new ItemStack(Items.REDSTONE, 1));
        fabricator.setItem(2, new ItemStack(Items.COPPER_INGOT, 1));
        fabricator.setItem(3, new ItemStack(ModItems.REPUBLIC_PLASTOID_INGOT.get(), 1));
    }

    @SuppressWarnings("removal")
    private static ServerPlayer mockPlayer(GameTestHelper helper, GameType gameType) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        gameType.updatePlayerAbilities(player.getAbilities());
        return player;
    }

    private static ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipeKey(String id) {
        return ResourceKey.create(Registries.RECIPE, Identifier.parse(id));
    }
}
