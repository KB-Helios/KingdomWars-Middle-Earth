package galacticwars.clonewars.menu;

import galacticwars.clonewars.combat.FactionRangedWeaponService;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.registry.ModMenuTypes;
import galacticwars.clonewars.workforce.WorkerDutyLoadoutPolicy;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.network.FriendlyByteBuf;

/** Server-authoritative recruit equipment and shared physical cargo inventory. */
public final class RecruitLoadoutMenu extends AbstractContainerMenu {
    public static final int EQUIPMENT_SLOT_COUNT = 7;
    public static final int CARGO_SLOT_COUNT = 9;
    public static final int RECRUIT_SLOT_COUNT = EQUIPMENT_SLOT_COUNT + CARGO_SLOT_COUNT;
    public static final int PLAYER_SLOT_COUNT = 36;
    public static final int PLAYER_INVENTORY_START = RECRUIT_SLOT_COUNT;
    public static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + PLAYER_SLOT_COUNT;

    private static final int CARGO_SLOT_START = EQUIPMENT_SLOT_COUNT;
    private static final int CARGO_SLOT_END = RECRUIT_SLOT_COUNT;
    private static final int PLAYER_EXTENDED_END = PLAYER_INVENTORY_START + 27;
    private static final RecruitEquipmentTarget[] EQUIPMENT_TARGETS = {
            RecruitEquipmentTarget.MILITARY_WEAPON,
            RecruitEquipmentTarget.WORKER_TOOL,
            RecruitEquipmentTarget.OFFHAND,
            RecruitEquipmentTarget.HEAD,
            RecruitEquipmentTarget.CHEST,
            RecruitEquipmentTarget.LEGS,
            RecruitEquipmentTarget.FEET
    };

    private final Level level;
    private final int recruitEntityId;
    private final Container equipment;
    private final Container cargo;
    private final GalacticRecruitEntity serverRecruit;

    /** Client constructor. Slot contents are populated by vanilla menu synchronization. */
    public RecruitLoadoutMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, extraData.readVarInt());
    }

    private RecruitLoadoutMenu(int containerId, Inventory inventory, int recruitEntityId) {
        this(
                containerId,
                inventory,
                recruitEntityId,
                new SimpleContainer(EQUIPMENT_SLOT_COUNT),
                new SimpleContainer(CARGO_SLOT_COUNT),
                null);
    }

    /** Server constructor. Both container views mutate the live recruit state. */
    public RecruitLoadoutMenu(
            int containerId,
            Inventory inventory,
            GalacticRecruitEntity recruit
    ) {
        this(
                containerId,
                inventory,
                recruit.getId(),
                new RecruitEquipmentContainer(recruit),
                recruit.createCargoContainer(),
                recruit);
    }

    private RecruitLoadoutMenu(
            int containerId,
            Inventory inventory,
            int recruitEntityId,
            Container equipment,
            Container cargo,
            GalacticRecruitEntity serverRecruit
    ) {
        super(ModMenuTypes.RECRUIT_LOADOUT.get(), containerId);
        checkContainerSize(equipment, EQUIPMENT_SLOT_COUNT);
        checkContainerSize(cargo, CARGO_SLOT_COUNT);
        this.level = inventory.player.level();
        this.recruitEntityId = recruitEntityId;
        this.equipment = equipment;
        this.cargo = cargo;
        this.serverRecruit = serverRecruit;

        for (int slot = 0; slot < EQUIPMENT_SLOT_COUNT; slot++) {
            this.addSlot(new RecruitEquipmentSlot(
                    equipment,
                    slot,
                    8 + slot * 18,
                    20,
                    EQUIPMENT_TARGETS[slot],
                    serverRecruit));
        }
        for (int slot = 0; slot < CARGO_SLOT_COUNT; slot++) {
            this.addSlot(new Slot(cargo, slot, 8 + slot * 18, 64));
        }
        this.addStandardInventorySlots(inventory, 8, 112);

        if (serverRecruit != null) {
            equipment.startOpen(inventory.player);
            cargo.startOpen(inventory.player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot sourceSlot = this.slots.get(slotIndex);
        if (!sourceSlot.hasItem() || !sourceSlot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack original = sourceStack.copy();
        int startingCount = sourceStack.getCount();

        if (slotIndex < RECRUIT_SLOT_COUNT) {
            if (!this.moveItemStackTo(
                    sourceStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int equipmentTarget = this.equipmentTarget(sourceStack);
            if (equipmentTarget >= 0) {
                this.moveItemStackTo(sourceStack, equipmentTarget, equipmentTarget + 1, false);
            }
            if (!sourceStack.isEmpty()) {
                this.moveItemStackTo(sourceStack, CARGO_SLOT_START, CARGO_SLOT_END, false);
            }
            if (sourceStack.getCount() == startingCount) {
                boolean movedWithinPlayerInventory;
                if (slotIndex < PLAYER_EXTENDED_END) {
                    movedWithinPlayerInventory = this.moveItemStackTo(
                            sourceStack, PLAYER_EXTENDED_END, PLAYER_INVENTORY_END, false);
                } else {
                    movedWithinPlayerInventory = this.moveItemStackTo(
                            sourceStack, PLAYER_INVENTORY_START, PLAYER_EXTENDED_END, false);
                }
                if (!movedWithinPlayerInventory) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY, original);
        } else {
            sourceSlot.setChanged();
        }
        if (sourceStack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        sourceSlot.onTake(player, sourceStack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        Entity entity = this.level.getEntity(this.recruitEntityId);
        return player.level() == this.level
                && entity instanceof GalacticRecruitEntity recruit
                && (this.serverRecruit == null || recruit == this.serverRecruit)
                && recruit.isAlive()
                && player.distanceToSqr(recruit) <= 64.0D
                && (this.serverRecruit == null || recruit.canPlayerManageLogistics(player));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.serverRecruit != null) {
            this.equipment.stopOpen(player);
            this.cargo.stopOpen(player);
        }
    }

    public int recruitEntityId() {
        return recruitEntityId;
    }

    private int equipmentTarget(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            for (int slot = 0; slot < EQUIPMENT_SLOT_COUNT; slot++) {
                if (EQUIPMENT_TARGETS[slot].equipmentSlot == equippable.slot()
                        && this.slots.get(slot).mayPlace(stack)) {
                    return slot;
                }
            }
        }
        if (WorkerDutyLoadoutPolicy.isRecognizedTool(stack)
                && this.slots.get(RecruitEquipmentTarget.WORKER_TOOL.ordinal()).mayPlace(stack)) {
            return RecruitEquipmentTarget.WORKER_TOOL.ordinal();
        }
        if (FactionRangedWeaponService.supportsRecruitRangedCombat(stack)
                || stack.is(Items.BOW)
                || stack.is(Items.CROSSBOW)
                || stack.get(DataComponents.WEAPON) != null) {
            return RecruitEquipmentTarget.MILITARY_WEAPON.ordinal();
        }
        return -1;
    }

    private static final class RecruitEquipmentSlot extends Slot {
        private final RecruitEquipmentTarget target;
        private final GalacticRecruitEntity owner;

        private RecruitEquipmentSlot(
                Container container,
                int containerSlot,
                int x,
                int y,
                RecruitEquipmentTarget target,
                GalacticRecruitEntity owner
        ) {
            super(container, containerSlot, x, y);
            this.target = target;
            this.owner = owner;
        }

        @Override
        public void setByPlayer(ItemStack stack, ItemStack previous) {
            EquipmentSlot activeSlot = this.target.activeEquipmentSlot(this.owner);
            if (this.owner != null && activeSlot != null) {
                this.owner.onEquipItem(activeSlot, previous, stack);
            }
            super.setByPlayer(stack, previous);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (this.target == RecruitEquipmentTarget.MILITARY_WEAPON) {
                return !WorkerDutyLoadoutPolicy.isRecognizedTool(stack)
                        || FactionRangedWeaponService.supportsRecruitRangedCombat(stack)
                        || stack.get(DataComponents.WEAPON) != null;
            }
            if (this.target == RecruitEquipmentTarget.WORKER_TOOL) {
                return WorkerDutyLoadoutPolicy.isRecognizedTool(stack)
                        && (this.owner == null
                        || this.owner.getWorkerProfession()
                                .map(profession -> WorkerDutyLoadoutPolicy.isCompatible(profession, stack))
                                .orElse(true));
            }
            if (this.target == RecruitEquipmentTarget.OFFHAND) {
                return true;
            }
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            return equippable != null
                    && equippable.slot() == this.target.equipmentSlot
                    && (this.owner == null
                    || equippable.canBeEquippedBy(this.owner.typeHolder()));
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack stack = this.getItem();
            if (this.target.equipmentSlot != null
                    && this.target.equipmentSlot.isArmor()
                    && !stack.isEmpty()
                    && !player.isCreative()
                    && EnchantmentHelper.has(
                    stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                return false;
            }
            return super.mayPickup(player);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }

        @Override
        public Identifier getNoItemIcon() {
            return switch (this.target) {
                case OFFHAND -> InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
                case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
                case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
                case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
                case FEET -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
                default -> null;
            };
        }
    }

    private static final class RecruitEquipmentContainer implements Container {
        private final GalacticRecruitEntity recruit;

        private RecruitEquipmentContainer(GalacticRecruitEntity recruit) {
            this.recruit = recruit;
        }

        @Override
        public int getContainerSize() {
            return EQUIPMENT_SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            for (int slot = 0; slot < EQUIPMENT_SLOT_COUNT; slot++) {
                if (!this.getItem(slot).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return validSlot(slot)
                    ? EQUIPMENT_TARGETS[slot].get(this.recruit)
                    : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack existing = this.getItem(slot);
            if (existing.isEmpty() || amount <= 0) {
                return ItemStack.EMPTY;
            }
            int removedCount = Math.min(amount, existing.getCount());
            ItemStack removed = existing.copyWithCount(removedCount);
            ItemStack remainder = existing.copy();
            remainder.shrink(removedCount);
            this.setItem(slot, remainder);
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack existing = this.getItem(slot);
            if (!existing.isEmpty()) {
                EQUIPMENT_TARGETS[slot].set(this.recruit, ItemStack.EMPTY);
            }
            return existing;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (!validSlot(slot)) {
                throw new IndexOutOfBoundsException("equipment slot " + slot);
            }
            ItemStack stored = stack.isEmpty()
                    ? ItemStack.EMPTY
                    : stack.copyWithCount(1);
            EQUIPMENT_TARGETS[slot].set(this.recruit, stored);
        }

        @Override
        public void setChanged() {
            this.recruit.markLoadoutChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return player.level() == this.recruit.level()
                    && this.recruit.isAlive()
                    && player.distanceToSqr(this.recruit) <= 64.0D
                    && this.recruit.canPlayerManageLogistics(player);
        }

        @Override
        public void clearContent() {
            for (RecruitEquipmentTarget target : EQUIPMENT_TARGETS) {
                target.set(this.recruit, ItemStack.EMPTY);
            }
        }

        private static boolean validSlot(int slot) {
            return slot >= 0 && slot < EQUIPMENT_SLOT_COUNT;
        }
    }

    private enum RecruitEquipmentTarget {
        MILITARY_WEAPON(null),
        WORKER_TOOL(null),
        OFFHAND(EquipmentSlot.OFFHAND),
        HEAD(EquipmentSlot.HEAD),
        CHEST(EquipmentSlot.CHEST),
        LEGS(EquipmentSlot.LEGS),
        FEET(EquipmentSlot.FEET);

        private final EquipmentSlot equipmentSlot;

        RecruitEquipmentTarget(EquipmentSlot equipmentSlot) {
            this.equipmentSlot = equipmentSlot;
        }

        private ItemStack get(GalacticRecruitEntity recruit) {
            return switch (this) {
                case MILITARY_WEAPON -> recruit.getMilitaryMainHandItem();
                case WORKER_TOOL -> recruit.getWorkerMainHandItem();
                default -> recruit.getItemBySlot(this.equipmentSlot);
            };
        }

        private void set(GalacticRecruitEntity recruit, ItemStack stack) {
            switch (this) {
                case MILITARY_WEAPON -> recruit.setMilitaryMainHandItem(stack);
                case WORKER_TOOL -> recruit.setWorkerMainHandItem(stack);
                default -> recruit.setItemSlot(this.equipmentSlot, stack);
            }
        }

        private EquipmentSlot activeEquipmentSlot(GalacticRecruitEntity recruit) {
            if (this == MILITARY_WEAPON) {
                return recruit != null && recruit.isMilitaryDutyActive()
                        ? EquipmentSlot.MAINHAND : null;
            }
            if (this == WORKER_TOOL) {
                return recruit != null && !recruit.isMilitaryDutyActive()
                        ? EquipmentSlot.MAINHAND : null;
            }
            return this.equipmentSlot;
        }
    }
}
