package galacticwars.clonewars.world;

import galacticwars.clonewars.registry.ModBlockEntityTypes;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class FactionCommandPostBlockEntity extends BlockEntity {
    private @Nullable UUID siteId;
    private String factionId = "";

    public FactionCommandPostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.FACTION_COMMAND_POST.get(), pos, state);
    }

    public void configure(UUID siteId, String factionId) {
        this.siteId = java.util.Objects.requireNonNull(siteId, "siteId");
        String normalized = java.util.Objects.requireNonNull(factionId, "factionId")
                .trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("factionId cannot be blank");
        }
        this.factionId = normalized;
        setChanged();
    }

    public boolean configured() {
        return siteId != null && !factionId.isBlank();
    }

    public @Nullable UUID siteId() {
        return siteId;
    }

    public String factionId() {
        return factionId;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        siteId = input.read("site_id", UUIDUtil.CODEC).orElse(null);
        factionId = input.getStringOr("faction_id", "");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("site_id", UUIDUtil.CODEC, siteId);
        output.putString("faction_id", factionId);
    }
}
