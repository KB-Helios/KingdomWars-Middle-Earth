package galacticwars.clonewars.world;

import galacticwars.clonewars.registry.ModBlockEntityTypes;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlueprintSiteLootBlockEntity extends BlockEntity {
    private String marker = "";

    public BlueprintSiteLootBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.BLUEPRINT_SITE_LOOT.get(), pos, state);
    }

    public void configure(String marker) {
        String normalized = marker == null ? "" : marker.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("loot marker cannot be blank");
        }
        this.marker = normalized;
        setChanged();
    }

    public String marker() {
        return marker;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        marker = input.getStringOr("marker", "");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("marker", marker);
    }
}
