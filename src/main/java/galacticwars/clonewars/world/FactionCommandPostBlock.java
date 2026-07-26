package galacticwars.clonewars.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class FactionCommandPostBlock extends BaseEntityBlock {
    public static final MapCodec<FactionCommandPostBlock> CODEC = simpleCodec(FactionCommandPostBlock::new);

    public FactionCommandPostBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FactionCommandPostBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof FactionCommandPostBlockEntity commandPost) {
            if (!commandPost.configured()) {
                player.sendSystemMessage(Component.translatable(
                        "message.galacticwars.faction_command_post.unconfigured"));
                return InteractionResult.SUCCESS;
            }
            FactionOutpostSavedData data = FactionOutpostSavedData.get((ServerLevel) level);
            FactionOutpostRecord outpost = data.outpost(commandPost.siteId()).orElse(null);
            if (outpost == null) {
                player.sendSystemMessage(Component.translatable(
                        "message.galacticwars.faction_command_post.unavailable",
                        commandPost.factionId()));
                return InteractionResult.SUCCESS;
            }
            int residents = outpost.militaryNpcIds().size() + outpost.civilianNpcIds().size();
            String stateLabel = data.siteGenerated(outpost.id()) ? "online" : "initializing";
            player.sendSystemMessage(Component.translatable(
                    "message.galacticwars.faction_command_post.status",
                    outpost.factionId(),
                    outpost.siteKind().id(),
                    residents,
                    stateLabel));
        }
        return InteractionResult.SUCCESS;
    }
}
