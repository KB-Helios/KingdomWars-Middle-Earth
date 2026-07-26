package galacticwars.clonewars.force;

import galacticwars.clonewars.fabrication.FabricationInput;
import galacticwars.clonewars.progression.ForceRuntimeState;
import galacticwars.clonewars.progression.ForceSavedData;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class ForceRitualService {
    private ForceRitualService() {
    }

    public static synchronized RitualResult perform(
            ServerPlayer player,
            BlockPos shrinePos,
            String recipeId
    ) {
        if (!(player.level() instanceof ServerLevel level)
                || player.distanceToSqr(
                        shrinePos.getX() + 0.5D,
                        shrinePos.getY() + 0.5D,
                        shrinePos.getZ() + 0.5D) > 64.0D
                || !(level.getBlockState(shrinePos).getBlock() instanceof ForceShrineBlock shrine)) {
            return reject(player, "force_shrine_required");
        }
        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key;
        try {
            key = ResourceKey.create(Registries.RECIPE, Identifier.parse(recipeId));
        } catch (RuntimeException exception) {
            return reject(player, "invalid_ritual");
        }
        var holder = level.getServer().getRecipeManager().byKey(key).orElse(null);
        if (holder == null || !(holder.value() instanceof ForceRitualRecipe recipe)
                || !recipe.shrine().equals(shrine.traditionId())
                || !recipe.path().equals(shrine.traditionId())) {
            return reject(player, "wrong_force_shrine");
        }
        ForceRuntimeState state = ForceSavedData.get(level).state(player.getUUID());
        if (!state.initiated() || !state.traditionId().equals(recipe.path())
                || state.rank() < recipe.requiredRank()) {
            return reject(player, "force_rank_required");
        }
        Inventory inventory = player.getInventory();
        ArrayList<ItemStack> stacks = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            stacks.add(inventory.getItem(slot).copy());
        }
        FabricationInput input = new FabricationInput(stacks);
        int[] removals = recipe.removals(input);
        ItemStack output = recipe.assemble(input);
        if (removals.length != inventory.getContainerSize() || output.isEmpty()) {
            return reject(player, "missing_ritual_ingredients");
        }
        if (!canFitOutput(inventory, removals, output)) {
            return reject(player, "ritual_inventory_full");
        }
        for (int slot = 0; slot < removals.length; slot++) {
            if (removals[slot] > 0) {
                inventory.getItem(slot).shrink(removals[slot]);
            }
        }
        if (!inventory.add(output.copy())) {
            throw new IllegalStateException("Validated ritual output could not be inserted");
        }
        player.sendSystemMessage(Component.translatable(
                "message.galacticwars.ritual.success", output.getHoverName()));
        return new RitualResult(true, "", output);
    }

    private static boolean canFitOutput(Inventory inventory, int[] removals, ItemStack output) {
        int remaining = output.getCount();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            int resultingCount = stack.getCount() - removals[slot];
            if (resultingCount <= 0) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(stack, output)) {
                remaining -= Math.max(0, stack.getMaxStackSize() - resultingCount);
                if (remaining <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static RitualResult reject(ServerPlayer player, String reason) {
        player.sendSystemMessage(Component.translatable(
                "message.galacticwars.ritual.rejected",
                Component.translatable("message.galacticwars.ritual.reason." + reason)));
        return new RitualResult(false, reason, ItemStack.EMPTY);
    }

    public record RitualResult(boolean accepted, String reason, ItemStack output) {
    }
}
