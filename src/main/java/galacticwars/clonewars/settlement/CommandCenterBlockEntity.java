package galacticwars.clonewars.settlement;

import galacticwars.clonewars.economy.CreditTransactionService;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import galacticwars.clonewars.data.GameplayDataManager;
import galacticwars.clonewars.faction.FactionBalanceService;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomActionId;
import galacticwars.clonewars.kingdom.KingdomGameplayAction;
import galacticwars.clonewars.kingdom.KingdomGameplayResult;
import galacticwars.clonewars.kingdom.KingdomGameplayRuntimeService;
import galacticwars.clonewars.kingdom.KingdomGameplayTransactionService;
import galacticwars.clonewars.kingdom.KingdomPermission;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.registry.ModBlockEntityTypes;
import galacticwars.clonewars.progression.ProgressionEventType;
import galacticwars.clonewars.progression.ProgressionSavedData;
import galacticwars.clonewars.workforce.ResourceInventory;
import galacticwars.clonewars.technology.KingdomResearchService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class CommandCenterBlockEntity extends BaseContainerBlockEntity {
    public static final int CONTAINER_SIZE = 54;
    private static final List<String> FALLBACK_FACTIONS = List.of(
            "galacticwars:republic",
            "galacticwars:mandalorian",
            "galacticwars:separatist",
            "galacticwars:hutt_cartel",
            "galacticwars:nightsister");

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private @Nullable UUID ownerId;
    private String factionId = FALLBACK_FACTIONS.getFirst();
    private long lastUpkeepGameTime;
    private long pendingUpkeepCredits;
    private boolean upkeepClockInitialized;
    private boolean upkeepPaid = true;
    private boolean removalPrepared;

    public CommandCenterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.COMMAND_CENTER.get(), pos, state);
    }

    public static void serverTick(
            net.minecraft.world.level.Level level,
            BlockPos pos,
            BlockState state,
            CommandCenterBlockEntity commandCenter
    ) {
        if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % 20L == 0L) {
            KingdomResearchService.tick(serverLevel, commandCenter);
        }
    }

    public @Nullable UUID ownerId() {
        return ownerId;
    }

    public boolean isOwner(Player player) {
        return ownerId != null && ownerId.equals(player.getUUID());
    }

    public boolean canUse(Player player, KingdomPermission permission) {
        if (this.isOwner(player)) {
            return true;
        }
        return this.level instanceof ServerLevel serverLevel
                && this.ownerId != null
                && KingdomSavedData.get(serverLevel).kingdomForOwner(this.ownerId)
                        .map(kingdom -> kingdom.allows(player.getUUID(), permission))
                        .orElse(false);
    }

    public boolean claim(Player player) {
        if (ownerId != null && !ownerId.equals(player.getUUID())) {
            return false;
        }
        if (ownerId == null) {
            if (!(this.level instanceof ServerLevel serverLevel)) {
                return false;
            }
            KingdomGameplayAction action = new KingdomGameplayAction(
                    KingdomActionId.of("command_center_claim", player.getUUID(),
                            serverLevel.dimension().identifier(), this.worldPosition.asLong()),
                    player.getUUID(), ProgressionEventType.BUILDING_COMPLETED,
                    "command_center", 1);
            ProgressionSavedData progression = ProgressionSavedData.get(serverLevel);
            KingdomGameplayResult evaluation = KingdomGameplayTransactionService.evaluate(
                    progression.state(player.getUUID()), action);
            if (!evaluation.accepted()) {
                return false;
            }
            long previousUpkeepTime = this.lastUpkeepGameTime;
            boolean previousClockState = this.upkeepClockInitialized;
            ownerId = player.getUUID();
            this.lastUpkeepGameTime = serverLevel.getGameTime();
            this.upkeepClockInitialized = true;
            if (evaluation.changed()) {
                KingdomGameplayResult committed = KingdomGameplayRuntimeService.applyProgression(
                        progression, action);
                if (!committed.accepted() || !committed.changed()) {
                    ownerId = null;
                    this.lastUpkeepGameTime = previousUpkeepTime;
                    this.upkeepClockInitialized = previousClockState;
                    return false;
                }
            }
            this.setChangedAndSync();
        }
        return true;
    }

    public String factionId() {
        return factionId;
    }

    public String cycleFaction() {
        List<String> factions = supportedFactions();
        int next = (Math.max(0, factions.indexOf(factionId)) + 1) % factions.size();
        return this.setFaction(factions.get(next));
    }

    public String setFaction(String factionId) {
        if (!supportedFactions().contains(factionId)) {
            throw new IllegalArgumentException("unsupported faction " + factionId);
        }
        this.factionId = factionId;
        this.setChangedAndSync();
        return this.factionId;
    }

    public int treasuryCredits() {
        return CreditTransactionService.containerBalance(this);
    }

    /** Atomically seals the exact starter-camp reservation into physical Hall storage. */
    public boolean depositStarterSupplies(ResourceInventory reservation) {
        Objects.requireNonNull(reservation, "reservation");
        NonNullList<ItemStack> simulated = NonNullList.withSize(this.items.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < this.items.size(); slot++) {
            simulated.set(slot, this.items.get(slot).copy());
        }
        for (var entry : reservation.resources().entrySet()) {
            var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.getKey()));
            if (item == null || item == Items.AIR
                    || !insertAll(simulated, new ItemStack(item, entry.getValue()))) {
                return false;
            }
        }
        this.items = simulated;
        this.setChangedAndSync();
        return true;
    }

    public Map<String, Integer> availableResearchContribution(Map<String, Integer> remaining) {
        LinkedHashMap<String, Integer> available = new LinkedHashMap<>();
        remaining.forEach((itemId, needed) -> {
            int present = this.items.stream()
                    .filter(stack -> itemId.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()))
                    .mapToInt(ItemStack::getCount)
                    .sum();
            int count = Math.min(Math.max(0, needed), present);
            if (count > 0) {
                available.put(itemId, count);
            }
        });
        return Map.copyOf(available);
    }

    public boolean consumeResearchContribution(Map<String, Integer> contribution) {
        if (!availableResearchContribution(contribution).equals(contribution)) {
            return false;
        }
        contribution.forEach((itemId, count) -> {
            int remaining = count;
            for (ItemStack stack : this.items) {
                if (remaining > 0
                        && itemId.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                    int removed = Math.min(remaining, stack.getCount());
                    stack.shrink(removed);
                    remaining -= removed;
                }
            }
        });
        this.setChangedAndSync();
        return true;
    }

    public boolean refundResearchMaterials(Map<String, Integer> materials) {
        NonNullList<ItemStack> simulated = NonNullList.withSize(this.items.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < this.items.size(); slot++) {
            simulated.set(slot, this.items.get(slot).copy());
        }
        for (var entry : materials.entrySet()) {
            var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.getKey()));
            if (item == null || item == Items.AIR
                    || !insertAll(simulated, new ItemStack(item, entry.getValue()))) {
                return false;
            }
        }
        this.items = simulated;
        this.setChangedAndSync();
        return true;
    }

    private static boolean insertAll(NonNullList<ItemStack> inventory, ItemStack incoming) {
        ItemStack remaining = incoming.copy();
        for (ItemStack existing : inventory) {
            if (!remaining.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining)
                    && existing.getCount() < existing.getMaxStackSize()) {
                int moved = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                existing.grow(moved);
                remaining.shrink(moved);
            }
        }
        for (int slot = 0; slot < inventory.size() && !remaining.isEmpty(); slot++) {
            if (inventory.get(slot).isEmpty()) {
                int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                inventory.set(slot, remaining.copyWithCount(moved));
                remaining.shrink(moved);
            }
        }
        return remaining.isEmpty();
    }

    public boolean upkeepPaid() {
        return upkeepPaid;
    }

    public boolean chargeDailyUpkeep(long gameTime, int population) {
        if (this.level instanceof ServerLevel serverLevel) {
            this.settlePendingCampaignRefunds(serverLevel);
        }
        if (!this.upkeepClockInitialized) {
            this.lastUpkeepGameTime = gameTime;
            this.upkeepClockInitialized = true;
            this.setChangedAndSync();
            return this.upkeepPaid;
        }
        int upkeepPercent = FactionBalanceService.resolve(this.factionId).upkeepPercent();
        int dailyUpkeep = Math.max(1, FactionBalanceService.applyPercentCeil(
                Math.max(0, population), upkeepPercent));
        long elapsedTicks = Math.max(0L, gameTime - this.lastUpkeepGameTime);
        long elapsedDays = elapsedTicks / 24000L;
        boolean changed = false;
        if (elapsedDays > 0L) {
            this.pendingUpkeepCredits = saturatingAdd(
                    this.pendingUpkeepCredits,
                    saturatingMultiply(dailyUpkeep, elapsedDays));
            this.lastUpkeepGameTime = gameTime - elapsedTicks % 24000L;
            changed = true;
        }
        if (this.pendingUpkeepCredits > 0L) {
            boolean paid = this.pendingUpkeepCredits <= Integer.MAX_VALUE
                    && this.reserveCredits((int) this.pendingUpkeepCredits);
            if (paid) {
                this.pendingUpkeepCredits = 0L;
            }
            changed |= this.upkeepPaid != paid || paid;
            this.upkeepPaid = paid;
        }
        if (changed) {
            this.setChangedAndSync();
        }
        return this.upkeepPaid;
    }

    private static long saturatingAdd(long first, long second) {
        if (first < 0L || second < 0L) {
            throw new IllegalArgumentException("upkeep values cannot be negative");
        }
        return Long.MAX_VALUE - first < second ? Long.MAX_VALUE : first + second;
    }

    private static long saturatingMultiply(int value, long multiplier) {
        if (value < 0 || multiplier < 0L) {
            throw new IllegalArgumentException("upkeep values cannot be negative");
        }
        return value != 0 && multiplier > Long.MAX_VALUE / value
                ? Long.MAX_VALUE
                : value * multiplier;
    }

    public boolean reserveCredits(int amount) {
        return CreditTransactionService.withdrawContainer(this, amount);
    }

    public int refundCredits(int amount) {
        return CreditTransactionService.depositContainer(this, amount);
    }

    public int settlePendingCampaignRefunds(ServerLevel level) {
        if (this.ownerId == null) {
            return 0;
        }
        KingdomSavedData data = KingdomSavedData.get(level);
        boolean isAuthoritativeHall = data.isHallActive(this.ownerId)
                && data.kingdomForOwner(this.ownerId)
                .map(KingdomRecord::settlement)
                .filter(settlement -> settlement.dimensionId().equals(level.dimension().identifier().toString()))
                .filter(settlement -> settlement.hallX() == this.worldPosition.getX()
                        && settlement.hallY() == this.worldPosition.getY()
                        && settlement.hallZ() == this.worldPosition.getZ())
                .isPresent();
        return isAuthoritativeHall
                ? data.applyPendingCampaignRefunds(this.ownerId, this::refundCredits)
                : 0;
    }

    public boolean prepareForOwnerRemoval(ServerLevel level) {
        if (this.removalPrepared) {
            return false;
        }
        this.removalPrepared = true;
        return CommandCenterLifecycleService.prepareOwnerRemoval(level, this);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (this.level instanceof ServerLevel serverLevel) {
            this.settlePendingCampaignRefunds(serverLevel);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.galacticwars.command_center");
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = Objects.requireNonNull(items, "items");
    }

    @Override
    public boolean canOpen(Player player) {
        return super.canOpen(player) && this.canUse(player, KingdomPermission.USE_STORAGE);
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.sixRows(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.ownerId = input.read("CommandCenterOwner", UUIDUtil.CODEC).orElse(null);
        List<String> factions = supportedFactions();
        this.factionId = input.getStringOr("CommandCenterFaction", factions.getFirst());
        if (!factions.contains(this.factionId)) {
            this.factionId = factions.getFirst();
        }
        this.lastUpkeepGameTime = Math.max(0L, input.getLongOr("LastUpkeepGameTime", 0L));
        this.pendingUpkeepCredits = Math.max(0L, input.getLongOr("PendingUpkeepCredits", 0L));
        this.upkeepClockInitialized = input.getBooleanOr(
                "UpkeepClockInitialized",
                this.lastUpkeepGameTime > 0L);
        this.upkeepPaid = input.getBooleanOr("UpkeepPaid", true);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.storeNullable("CommandCenterOwner", UUIDUtil.CODEC, this.ownerId);
        output.putString("CommandCenterFaction", this.factionId);
        output.putLong("LastUpkeepGameTime", this.lastUpkeepGameTime);
        output.putLong("PendingUpkeepCredits", this.pendingUpkeepCredits);
        output.putBoolean("UpkeepClockInitialized", this.upkeepClockInitialized);
        output.putBoolean("UpkeepPaid", this.upkeepPaid);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    private void setChangedAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    private static List<String> supportedFactions() {
        List<String> loaded = GameplayDataManager.snapshot().selectableFactions().stream()
                .map(definition -> definition.id().toString())
                .toList();
        return loaded.isEmpty() ? FALLBACK_FACTIONS : loaded;
    }
}
