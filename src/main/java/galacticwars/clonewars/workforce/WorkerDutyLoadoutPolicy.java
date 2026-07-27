package galacticwars.clonewars.workforce;

import galacticwars.clonewars.registry.ModItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Physical main-hand requirements for each worker profession. */
public final class WorkerDutyLoadoutPolicy {
    private WorkerDutyLoadoutPolicy() {
    }

    public static ItemStack defaultTool(WorkerProfession profession) {
        return switch (profession) {
            case FARMER -> new ItemStack(Items.IRON_HOE);
            case LUMBERJACK -> new ItemStack(Items.IRON_AXE);
            case FISHERMAN -> new ItemStack(Items.FISHING_ROD);
            case ANIMAL_FARMER -> new ItemStack(Items.WHEAT);
            case MINER -> new ItemStack(Items.IRON_PICKAXE);
            case BUILDER -> new ItemStack(Items.BRICKS);
            case COOK -> new ItemStack(Items.BREAD);
            case MERCHANT -> new ItemStack(ModItems.CREDIT_CHIP.get());
            case COURIER -> new ItemStack(Items.CHEST);
            case TECHNICIAN -> new ItemStack(Items.REDSTONE);
        };
    }

    public static boolean isCompatible(WorkerProfession profession, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        return switch (profession) {
            case FARMER -> stack.is(ItemTags.HOES);
            case LUMBERJACK -> stack.is(ItemTags.AXES);
            case FISHERMAN -> stack.is(Items.FISHING_ROD);
            case MINER -> stack.is(ItemTags.PICKAXES);
            case ANIMAL_FARMER, BUILDER, COOK, MERCHANT, COURIER, TECHNICIAN ->
                    ItemStack.isSameItemSameComponents(stack, defaultTool(profession));
        };
    }

    public static boolean isRecognizedTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.is(ItemTags.HOES)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(Items.FISHING_ROD)
                || stack.is(Items.WHEAT)
                || stack.is(Items.BRICKS)
                || stack.is(Items.BREAD)
                || stack.is(ModItems.CREDIT_CHIP.get())
                || stack.is(Items.CHEST)
                || stack.is(Items.REDSTONE);
    }
}
