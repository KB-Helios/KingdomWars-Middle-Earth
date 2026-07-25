package galacticwars.clonewars.world;

import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.ai.NpcRole;
import galacticwars.clonewars.recruitment.NpcServiceBranch;
import galacticwars.clonewars.registry.ModBlockEntityTypes;
import galacticwars.clonewars.settlement.BlueprintRosterEntry;
import galacticwars.clonewars.settlement.KingdomBaseBlueprint;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;

/** Persistent, invisible handoff from worldgen workers to the authoritative server tick. */
public final class BlueprintSiteAnchorBlockEntity extends BlockEntity {
    private String blueprintId = "";
    private int rotationSteps;
    private String contentHash = "";
    private boolean initialized;
    private boolean invalid;

    public BlueprintSiteAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.BLUEPRINT_SITE_ANCHOR.get(), pos, state);
    }

    public void configure(String blueprintId, int rotationSteps) {
        KingdomBaseBlueprint blueprint = GameplayDataManager.snapshot().blueprint(blueprintId).orElse(null);
        configure(blueprintId, rotationSteps, blueprint == null ? "" : blueprint.contentHash());
    }

    public void configure(String blueprintId, int rotationSteps, String contentHash) {
        this.blueprintId = KingdomBaseBlueprint.canonicalId(blueprintId);
        this.rotationSteps = Math.floorMod(rotationSteps, 4);
        this.contentHash = contentHash == null ? "" : contentHash;
        this.initialized = false;
        this.invalid = false;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlueprintSiteAnchorBlockEntity anchor) {
        if (!(level instanceof ServerLevel serverLevel) || !anchor.shouldInitialize()) {
            return;
        }
        anchor.initialize(serverLevel, pos);
    }

    private boolean shouldInitialize() {
        return !initialized && !invalid && !blueprintId.isBlank() && GameplayDataManager.isReady();
    }

    private void initialize(ServerLevel level, BlockPos pos) {
        KingdomBaseBlueprint blueprint = GameplayDataManager.snapshot().blueprint(blueprintId).orElse(null);
        if (blueprint == null || blueprint.worldgen().isEmpty()) {
            markInvalid();
            return;
        }
        if (!contentHash.isEmpty() && !contentHash.equals(blueprint.contentHash())) {
            markInvalid();
            return;
        }
        var profile = blueprint.worldgen().orElseThrow();
        UUID siteId = computeSiteId(level, pos);
        FactionOutpostSavedData data = FactionOutpostSavedData.get(level);
        if (data.outpost(siteId).isPresent()) {
            markInitialized();
            return;
        }

        RandomSource random = RandomSource.create(siteId.getMostSignificantBits() ^ siteId.getLeastSignificantBits());
        ResidentPlan plan = buildResidentPlan(siteId, profile.roster(), random);

        // Seal the one-shot transaction before exposing containers or residents.
        data.registerGeneratedSite(siteId, profile.factionId(), level.dimension().identifier().toString(),
                pos, profile.siteRadius(), plan.military(), plan.civilians(), level.getGameTime());
        markInitialized();
        initializeLoot(level, pos, profile.factionId(), siteId);
        spawnResidents(level, pos, profile.siteRadius(), siteId, plan.residents());
    }

    private UUID computeSiteId(ServerLevel level, BlockPos pos) {
        String identity = level.dimension().identifier() + ":" + pos.asLong() + ":" + blueprintId;
        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
    }

    private static ResidentPlan buildResidentPlan(
            UUID siteId, List<BlueprintRosterEntry> roster, RandomSource random
    ) {
        ArrayList<PendingResident> residents = new ArrayList<>();
        ArrayList<UUID> military = new ArrayList<>();
        ArrayList<UUID> civilians = new ArrayList<>();
        int ordinal = 0;
        for (BlueprintRosterEntry entry : roster) {
            int count = entry.minimum() + random.nextInt(entry.maximum() - entry.minimum() + 1);
            NpcServiceBranch branch = entry.serviceBranch().equals("civilian")
                    ? NpcServiceBranch.CIVILIAN : NpcServiceBranch.MILITARY;
            for (int index = 0; index < count; index++) {
                UUID npcId = UUID.nameUUIDFromBytes((siteId + ":resident:" + ordinal++).getBytes(StandardCharsets.UTF_8));
                NpcRole role = entry.explicitRole().orElse(
                        branch == NpcServiceBranch.MILITARY
                                ? NpcRole.TROOPER : NpcRole.CIVILIAN);
                residents.add(new PendingResident(npcId, entry.entityTypeId(), branch, role));
                (branch == NpcServiceBranch.MILITARY ? military : civilians).add(npcId);
            }
        }
        return new ResidentPlan(List.copyOf(residents), List.copyOf(military), List.copyOf(civilians));
    }

    private void markInitialized() {
        initialized = true;
        setChanged();
    }

    private void markInvalid() {
        invalid = true;
        setChanged();
    }

    public boolean isInitializationInvalid() {
        return invalid;
    }

    private static void initializeLoot(ServerLevel level, BlockPos center, String factionId, UUID siteId) {
        String factionPath = factionId.substring(factionId.indexOf(':') + 1);
        ResourceKey<LootTable> loot = ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath("galacticwars", "chests/faction_site/" + factionPath));
        for (BlockPos target : BlockPos.betweenClosed(center.offset(-12, -2, -12), center.offset(12, 6, 12))) {
            if (level.getBlockState(target).is(galacticwars.clonewars.registry.ModBlocks.BLUEPRINT_SITE_LOOT.get())) {
                level.setBlock(target, Blocks.CHEST.defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof RandomizableContainerBlockEntity container) {
                    if (container.getLootTable() == null) {
                        container.setLootTable(loot);
                        container.setLootTableSeed(siteId.getLeastSignificantBits() ^ target.asLong());
                        container.setChanged();
                    }
                }
            }
        }
    }

    private static void spawnResidents(
            ServerLevel level, BlockPos center, int radius, UUID siteId, List<PendingResident> residents
    ) {
        int index = 0;
        for (PendingResident pending : residents) {
            var type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(pending.entityTypeId()));
            Entity entity = type == null ? null : type.create(level, EntitySpawnReason.STRUCTURE);
            if (!(entity instanceof GalacticRecruitEntity recruit)) {
                continue;
            }
            int dx = (index % 5) - 2;
            int dz = (index / 5) - 2;
            index++;
            recruit.setUUID(pending.id());
            recruit.snapTo(center.getX() + dx + 0.5D, center.getY() + 1.0D, center.getZ() + dz + 0.5D, 0.0F, 0.0F);
            recruit.initializeBlueprintSiteResident(
                    siteId, pending.branch(), pending.role(), center, radius);
            recruit.setPersistenceRequired();
            level.addFreshEntity(recruit);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        blueprintId = input.getStringOr("blueprint_id", "");
        rotationSteps = input.getIntOr("rotation_steps", 0);
        contentHash = input.getStringOr("content_hash", "");
        initialized = input.getBooleanOr("initialized", false);
        invalid = input.getBooleanOr("invalid", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("blueprint_id", blueprintId);
        output.putInt("rotation_steps", rotationSteps);
        output.putString("content_hash", contentHash);
        output.putBoolean("initialized", initialized);
        output.putBoolean("invalid", invalid);
    }

    private record PendingResident(
            UUID id,
            String entityTypeId,
            NpcServiceBranch branch,
            NpcRole role
    ) {
    }

    private record ResidentPlan(
            List<PendingResident> residents,
            List<UUID> military,
            List<UUID> civilians
    ) {
    }
}
