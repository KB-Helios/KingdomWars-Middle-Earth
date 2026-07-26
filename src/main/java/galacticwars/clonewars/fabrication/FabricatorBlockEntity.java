package galacticwars.clonewars.fabrication;

import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.menu.FabricatorMenu;
import galacticwars.clonewars.registry.ModBlockEntityTypes;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class FabricatorBlockEntity extends BaseContainerBlockEntity {
    public static final int INPUT_SLOTS = 27;
    public static final int OUTPUT_SLOT = INPUT_SLOTS;
    public static final int CONTAINER_SIZE = INPUT_SLOTS + 1;
    public static final int MAX_REPLAY_IDS = 128;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private UUID ownerKingdomId;
    private final LinkedHashSet<UUID> replayIds = new LinkedHashSet<>();

    public FabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.FABRICATOR.get(), pos, state);
    }

    public boolean bindOwner(Player placer) {
        if (!(placer.level() instanceof ServerLevel level)) {
            return false;
        }
        UUID kingdomId = KingdomSavedData.get(level).kingdomForPlayer(placer.getUUID())
                .map(kingdom -> kingdom.id()).orElse(null);
        if (kingdomId == null) {
            return false;
        }
        this.ownerKingdomId = kingdomId;
        this.setChanged();
        return true;
    }

    public Optional<UUID> ownerKingdomId() {
        return Optional.ofNullable(ownerKingdomId);
    }

    public boolean canUse(Player player) {
        return level instanceof ServerLevel serverLevel
                && ownerKingdomId != null
                && KingdomSavedData.get(serverLevel).kingdomForPlayer(player.getUUID())
                        .filter(kingdom -> kingdom.id().equals(ownerKingdomId)).isPresent();
    }

    public FabricationInput input() {
        ArrayList<ItemStack> input = new ArrayList<>(INPUT_SLOTS);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            input.add(items.get(slot).copy());
        }
        return new FabricationInput(input);
    }

    public boolean fabricate(UUID replayId, int[] removals, ItemStack output) {
        Objects.requireNonNull(replayId, "replayId");
        Objects.requireNonNull(removals, "removals");
        Objects.requireNonNull(output, "output");
        if (replayIds.contains(replayId) || removals.length != INPUT_SLOTS || output.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            if (removals[slot] < 0 || items.get(slot).getCount() < removals[slot]) {
                return false;
            }
        }
        ItemStack currentOutput = items.get(OUTPUT_SLOT);
        if (!currentOutput.isEmpty()
                && (!ItemStack.isSameItemSameComponents(currentOutput, output)
                        || currentOutput.getCount() + output.getCount() > currentOutput.getMaxStackSize())) {
            return false;
        }
        if (currentOutput.isEmpty() && output.getCount() > output.getMaxStackSize()) {
            return false;
        }
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            items.get(slot).shrink(removals[slot]);
        }
        if (currentOutput.isEmpty()) {
            items.set(OUTPUT_SLOT, output.copy());
        } else {
            currentOutput.grow(output.getCount());
        }
        replayIds.add(replayId);
        while (replayIds.size() > MAX_REPLAY_IDS) {
            replayIds.remove(replayIds.iterator().next());
        }
        this.setChanged();
        return true;
    }

    public boolean processed(UUID replayId) {
        return replayIds.contains(replayId);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.galacticwars.fabricator");
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
        return super.canOpen(player) && canUse(player);
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FabricatorMenu(containerId, inventory, this, worldPosition, List.of(), 0L, 0);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        this.ownerKingdomId = input.read("OwnerKingdom", UUIDUtil.CODEC).orElse(null);
        this.replayIds.clear();
        input.read("ReplayIds", UUIDUtil.CODEC.listOf(0, MAX_REPLAY_IDS))
                .orElse(List.of()).forEach(replayIds::add);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.storeNullable("OwnerKingdom", UUIDUtil.CODEC, ownerKingdomId);
        output.store("ReplayIds", UUIDUtil.CODEC.listOf(0, MAX_REPLAY_IDS), List.copyOf(replayIds));
    }
}
