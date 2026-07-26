package galacticwars.clonewars.fabrication;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record FabricationInput(List<ItemStack> items) implements RecipeInput {
    public FabricationInput {
        items = List.copyOf(items);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public int size() {
        return items.size();
    }
}
