package galacticwars.clonewars.menu;

import galacticwars.clonewars.fabrication.FabricatorBlockEntity;
import galacticwars.clonewars.registry.ModBlocks;
import galacticwars.clonewars.registry.ModMenuTypes;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class FabricatorMenu extends AbstractContainerMenu {
    public static final int MAX_RECIPE_IDS = 128;
    private final Container container;
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;
    private final List<String> recipeIds;
    private final long catalogGeneration;
    private final int technologyRevision;

    public FabricatorMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(FabricatorBlockEntity.CONTAINER_SIZE),
                buffer.readBlockPos(), readRecipeIds(buffer), buffer.readVarLong(), buffer.readVarInt());
    }

    public FabricatorMenu(
            int id,
            Inventory inventory,
            Container container,
            BlockPos blockPos,
            List<String> recipeIds,
            long catalogGeneration,
            int technologyRevision
    ) {
        super(ModMenuTypes.FABRICATOR.get(), id);
        this.container = container;
        this.blockPos = blockPos.immutable();
        this.recipeIds = List.copyOf(recipeIds);
        this.catalogGeneration = catalogGeneration;
        this.technologyRevision = technologyRevision;
        this.access = ContainerLevelAccess.create(inventory.player.level(), blockPos);
        container.startOpen(inventory.player);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(container, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }
        addSlot(new Slot(container, FabricatorBlockEntity.OUTPUT_SLOT, 170, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 85 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 143));
        }
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public List<String> recipeIds() {
        return recipeIds;
    }

    public long catalogGeneration() {
        return catalogGeneration;
    }

    public int technologyRevision() {
        return technologyRevision;
    }

    public Container container() {
        return container;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            moved = stack.copy();
            if (index < FabricatorBlockEntity.CONTAINER_SIZE) {
                if (!moveItemStackTo(stack, FabricatorBlockEntity.CONTAINER_SIZE, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, FabricatorBlockEntity.INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, ModBlocks.FABRICATOR.get())
                && (!(container instanceof FabricatorBlockEntity fabricator) || fabricator.canUse(player));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    private static List<String> readRecipeIds(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > MAX_RECIPE_IDS) {
            throw new IllegalArgumentException("Invalid fabrication recipe list size " + count);
        }
        java.util.ArrayList<String> ids = new java.util.ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            ids.add(buffer.readUtf(128));
        }
        return List.copyOf(ids);
    }
}
