package galacticwars.clonewars.fabrication;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class RecipeIngredientPlanner {
    private RecipeIngredientPlanner() {
    }

    public static int[] removals(List<CountedIngredient> requirements, FabricationInput input) {
        int[] removals = new int[input.size()];
        for (CountedIngredient requirement : requirements) {
            int remaining = requirement.count();
            for (int slot = 0; slot < input.size() && remaining > 0; slot++) {
                ItemStack stack = input.getItem(slot);
                int available = stack.getCount() - removals[slot];
                if (available > 0 && requirement.ingredient().test(stack)) {
                    int used = Math.min(available, remaining);
                    removals[slot] += used;
                    remaining -= used;
                }
            }
            if (remaining > 0) {
                return new int[0];
            }
        }
        return removals;
    }
}
