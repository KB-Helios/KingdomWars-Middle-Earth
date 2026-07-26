package galacticwars.clonewars.settlement;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomPermission;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.progression.ProgressionSavedData;
import galacticwars.clonewars.menu.CommandCenterNavigationMenuProvider;
import galacticwars.clonewars.menu.FactionSelectionMenuProvider;
import galacticwars.clonewars.menu.CommandCenterOperationsMenuProvider;
import galacticwars.clonewars.menu.CommandCenterOperationsMenu;
import galacticwars.clonewars.menu.StarterCampSetupMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import galacticwars.clonewars.registry.ModBlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class CommandCenterBlock extends BaseEntityBlock {
    public static final MapCodec<CommandCenterBlock> CODEC = simpleCodec(CommandCenterBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CommandCenterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
        return new CommandCenterBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide() ? null : createTickerHelper(
                type,
                ModBlockEntityTypes.COMMAND_CENTER.get(),
                CommandCenterBlockEntity::serverTick);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(placer instanceof ServerPlayer player)
                || !(level instanceof ServerLevel serverLevel)
                || !(level.getBlockEntity(pos) instanceof CommandCenterBlockEntity hall)) {
            return;
        }
        if (!hall.claim(player)) {
            return;
        }
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        Optional<KingdomRecord> existing = data.kingdomForOwner(player.getUUID());
        if (existing.isPresent()) {
            Optional<KingdomRecord> activated = data.activateHall(
                    player.getUUID(), existing.orElseThrow().factionId(),
                    serverLevel.dimension().identifier().toString(), pos);
            if (activated.isEmpty()) {
                player.sendSystemMessage(Component.translatable(
                        "message.galacticwars.command_center.duplicate"));
                serverLevel.destroyBlock(pos, true, player);
                return;
            }
            hall.setFaction(activated.orElseThrow().factionId());
        }
        if (existing.isEmpty()
                || ProgressionSavedData.get(serverLevel).state(player.getUUID()).factionId().isEmpty()) {
            MenuRegistry.openExtendedMenu(player, new FactionSelectionMenuProvider(pos));
        } else if (data.starterCampDeployment(existing.orElseThrow().id())
                .map(deployment -> !deployment.terminal()).orElse(true)) {
            MenuRegistry.openExtendedMenu(player, new StarterCampSetupMenuProvider(pos));
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(level.getBlockEntity(pos) instanceof CommandCenterBlockEntity hall)) {
            return InteractionResult.SUCCESS;
        }
        if (hall.ownerId() == null && !hall.claim(player)) {
            player.sendSystemMessage(Component.translatable("message.galacticwars.command_center.not_owner"));
            return InteractionResult.FAIL;
        }

        UUID authorityOwner = hall.ownerId();
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        boolean memberAccess = authorityOwner != null
                && hall.canUse(player, KingdomPermission.USE_STORAGE);
        boolean inviteAccess = authorityOwner != null && CommandCenterOperationsMenu.hasPendingInvite(
                data, hall, player.getUUID(), serverLevel.getGameTime());
        if (!memberAccess && !inviteAccess) {
            player.sendSystemMessage(Component.translatable("message.galacticwars.command_center.not_owner"));
            return InteractionResult.FAIL;
        }
        if (!memberAccess && player instanceof ServerPlayer serverPlayer) {
            MenuRegistry.openExtendedMenu(serverPlayer, new CommandCenterOperationsMenuProvider(pos));
            return InteractionResult.SUCCESS;
        }

        Optional<KingdomRecord> existing = data.kingdomForOwner(authorityOwner);
        if (existing.isEmpty()) {
            if (player instanceof ServerPlayer serverPlayer) {
                MenuRegistry.openExtendedMenu(serverPlayer, new FactionSelectionMenuProvider(pos));
            }
            return InteractionResult.SUCCESS;
        }
        Optional<KingdomRecord> activated = hall.isOwner(player)
                ? data.activateHall(authorityOwner, existing.orElseThrow().factionId(),
                        serverLevel.dimension().identifier().toString(), pos)
                : existing;
        if (activated.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.galacticwars.command_center.duplicate"));
            return InteractionResult.FAIL;
        }
        if (!hall.factionId().equals(activated.orElseThrow().factionId())) {
            hall.setFaction(activated.orElseThrow().factionId());
        }
        if (hall.isOwner(player) && player instanceof ServerPlayer owner) {
            boolean starterCampPending = data.starterCampDeployment(activated.orElseThrow().id())
                    .map(deployment -> !deployment.terminal())
                    .orElse(true);
            if (starterCampPending) {
                MenuRegistry.openExtendedMenu(owner, new StarterCampSetupMenuProvider(pos));
                return InteractionResult.SUCCESS;
            }
        }
        if (player.isShiftKeyDown()) {
            if (!hall.canUse(player, KingdomPermission.TRAVEL)) {
                player.sendSystemMessage(Component.translatable("message.galacticwars.command_center.command_denied"));
                return InteractionResult.FAIL;
            }
            if (!ProgressionSavedData.get(serverLevel).state(player.getUUID()).factionId().isEmpty()
                    && player instanceof ServerPlayer serverPlayer) {
                MenuRegistry.openExtendedMenu(
                        serverPlayer, new CommandCenterNavigationMenuProvider(serverPlayer));
                return InteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                MenuRegistry.openExtendedMenu(serverPlayer, new FactionSelectionMenuProvider(pos));
            }
            return InteractionResult.SUCCESS;
        }

        hall.settlePendingCampaignRefunds(serverLevel);
        if (player instanceof ServerPlayer serverPlayer) {
            MenuRegistry.openExtendedMenu(serverPlayer, new CommandCenterOperationsMenuProvider(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof CommandCenterBlockEntity hall
                && (hall.ownerId() == null || hall.isOwner(player))) {
            hall.prepareForOwnerRemoval(serverLevel);
            dropHallContents(serverLevel, pos, hall);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool
    ) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    @Override
    protected void onExplosionHit(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            Explosion explosion,
            BiConsumer<ItemStack, BlockPos> dropConsumer
    ) {
        // Hall removal is owner-authorized so treasury and campaign reservations remain conserved.
    }

    private static void dropHallContents(ServerLevel level, BlockPos pos, CommandCenterBlockEntity hall) {
        for (int slot = 0; slot < hall.getContainerSize(); slot++) {
            ItemStack stored = hall.removeItemNoUpdate(slot);
            if (!stored.isEmpty()) {
                popResource(level, pos, stored);
            }
        }
        hall.clearContent();
    }
}
