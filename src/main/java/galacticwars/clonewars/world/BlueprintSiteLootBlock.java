package galacticwars.clonewars.world;

import com.mojang.serialization.MapCodec;
import galacticwars.clonewars.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlueprintSiteLootBlock extends BaseEntityBlock {
    public static final MapCodec<BlueprintSiteLootBlock> CODEC = simpleCodec(BlueprintSiteLootBlock::new);

    public BlueprintSiteLootBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlueprintSiteLootBlockEntity(pos, state);
    }
}
