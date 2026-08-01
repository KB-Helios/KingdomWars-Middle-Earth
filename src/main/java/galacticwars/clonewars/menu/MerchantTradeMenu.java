package galacticwars.clonewars.menu;

import galacticwars.clonewars.economy.PhysicalTradeService;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.registry.ModMenuTypes;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Bounded merchant offers; all costs and rewards are resolved again on the server. */
public final class MerchantTradeMenu extends AbstractContainerMenu {
    public static final int MAX_OFFERS = MerchantTradeOffer.MAX_OFFERS;
    private final Level level;
    private final int merchantEntityId;
    private final List<MerchantTradeOffer> offers;
    private final LinkedHashSet<UUID> processedActionIds = new LinkedHashSet<>();

    public MerchantTradeMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readVarInt(), readOffers(buffer));
    }

    MerchantTradeMenu(
            int containerId,
            Inventory inventory,
            int merchantEntityId,
            List<MerchantTradeOffer> offers
    ) {
        super(ModMenuTypes.MERCHANT_TRADE.get(), containerId);
        this.level = inventory.player.level();
        this.merchantEntityId = merchantEntityId;
        Objects.requireNonNull(offers, "offers");
        if (offers.size() > MAX_OFFERS) {
            throw new IllegalArgumentException("merchant offer count exceeds " + MAX_OFFERS);
        }
        this.offers = List.copyOf(offers);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return false;
    }

    public boolean handleReplayAction(ServerPlayer serverPlayer, UUID requestId, int buttonId) {
        if (requestId == null || buttonId < 0 || buttonId >= offers.size()
                || !stillValid(serverPlayer) || !processedActionIds.add(requestId)) {
            return false;
        }
        while (processedActionIds.size() > 64) {
            processedActionIds.remove(processedActionIds.iterator().next());
        }
        MerchantTradeOffer offer = offers.get(buttonId);
        PhysicalTradeService.TradeResult result = PhysicalTradeService.purchase(
                serverPlayer,
                requestId,
                offer.tradeId(),
                level.getEntity(merchantEntityId) instanceof GalacticRecruitEntity merchant
                        ? merchant : null,
                offer.quote());
        serverPlayer.sendSystemMessage(Component.translatable(
                result.accepted() ? "message.galacticwars.trade.accepted" : "message.galacticwars.trade.rejected",
                Component.translatable(PhysicalTradeService.reasonTranslationKey(result.reason()))));
        return result.accepted();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        Entity entity = level.getEntity(merchantEntityId);
        return entity instanceof GalacticRecruitEntity recruit
                && recruit.isMerchant()
                && recruit.isMarketAvailable()
                && entity.isAlive()
                && player.distanceToSqr(entity) <= 64.0D;
    }

    public List<String> tradeIds() {
        return offers.stream().map(MerchantTradeOffer::tradeId).toList();
    }

    public List<MerchantTradeOffer> offers() {
        return offers;
    }

    private static List<MerchantTradeOffer> readOffers(FriendlyByteBuf buffer) {
        return MerchantTradeOffer.readOffers(buffer);
    }
}
