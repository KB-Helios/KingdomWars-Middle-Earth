package galacticwars.clonewars.economy;

import galacticwars.clonewars.conquest.ConquestSavedData;
import galacticwars.clonewars.data.LaunchContentDefinitions;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.faction.FactionBalanceService;
import galacticwars.clonewars.faction.ai.FactionReputationEvent;
import galacticwars.clonewars.faction.ai.FactionReputationService;
import galacticwars.clonewars.faction.ai.NpcDisposition;
import galacticwars.clonewars.faction.ai.NpcReactionDecision;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.progression.GalacticProgressionCoordinator;
import galacticwars.clonewars.progression.LaunchContentCatalog;
import galacticwars.clonewars.progression.ProgressionDecision;
import galacticwars.clonewars.progression.ProgressionEvent;
import galacticwars.clonewars.progression.ProgressionEventType;
import galacticwars.clonewars.progression.ProgressionSavedData;
import galacticwars.clonewars.progression.ProgressionState;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative physical trades with side-effect-free quote previews. */
public final class PhysicalTradeService {
    private PhysicalTradeService() {
    }

    public static TradePreview preview(ServerPlayer player, String tradeId) {
        return preview(player, tradeId, null);
    }

    /**
     * Evaluates the exact gates used by a purchase without changing inventory,
     * progression, diplomacy, conquest, or merchant state.
     */
    public static TradePreview preview(
            ServerPlayer player, String tradeId, GalacticRecruitEntity merchant
    ) {
        Objects.requireNonNull(player, "player");
        if (tradeId == null || tradeId.isBlank()) {
            return TradePreview.unknown("unknown_trade");
        }
        LaunchContentDefinitions.TradeDefinition trade = LaunchContentCatalog.trades().get(tradeId);
        if (trade == null) {
            return TradePreview.unknown("unknown_trade");
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return TradePreview.rejected(trade, "server_only");
        }
        if (merchant != null && (merchant.level() != level || !merchant.isAlive()
                || !merchant.isMerchant() || player.distanceToSqr(merchant) > 64.0D)) {
            return TradePreview.rejected(trade, "merchant_unavailable");
        }

        ProgressionState state = ProgressionSavedData.get(level).state(player.getUUID());
        String tradeFaction = "galacticwars:" + trade.factionId();
        NpcReactionDecision reaction = merchant == null ? null : merchant.npcReactionTo(player);
        if (merchant != null && !merchant.factionIdForGameplay().equals(tradeFaction)) {
            return TradePreview.rejected(trade, "hostile_merchant", reaction);
        }
        if (reaction != null && !reaction.tradeAllowed()) {
            return TradePreview.rejected(
                    trade,
                    reaction.disposition() == NpcDisposition.HOSTILE
                            ? "hostile_merchant" : "wary_merchant",
                    reaction);
        }
        if (merchant == null && !state.factionId().equals(tradeFaction)) {
            return TradePreview.rejected(trade, "hostile_merchant", null);
        }

        KingdomSavedData kingdoms = KingdomSavedData.get(level);
        var playerKingdom = kingdoms.kingdomForPlayer(player.getUUID()).orElse(null);
        var merchantKingdom = merchant == null
                ? kingdoms.kingdoms().stream()
                        .filter(kingdom -> kingdom.factionId().equals(tradeFaction))
                        .findFirst().orElse(null)
                : kingdoms.kingdomForRecruit(merchant.getUUID()).orElse(null);
        if (playerKingdom != null && merchantKingdom != null
                && !playerKingdom.id().equals(merchantKingdom.id())
                && kingdoms.relation(playerKingdom.id(), merchantKingdom.id()).embargo()) {
            return TradePreview.rejected(trade, "trade_embargoed", reaction);
        }
        if (trade.stockTier() > 1 && !state.unlocks().contains("veteran_trades")) {
            return TradePreview.rejected(trade, "veteran_trade_locked", reaction);
        }
        if (!state.unlocks().contains(trade.requiredUnlock())) {
            return TradePreview.rejected(trade, "trade_locked", reaction);
        }
        if (!trade.regionalPrerequisite().isEmpty()) {
            var control = ConquestSavedData.get(level).state(trade.regionalPrerequisite()).orElse(null);
            if (control == null || !control.controllingFaction().equals(tradeFaction)) {
                return TradePreview.rejected(trade, "regional_control_required", reaction);
            }
        }
        if (registeredItem(trade.itemId()) == null) {
            return TradePreview.rejected(trade, "unknown_trade_item", reaction);
        }
        Item tradeItem = registeredItem(trade.itemId());
        if (merchant != null
                && merchant.isMarketAvailable()
                && (tradeItem == null
                        || !merchant.hasMerchantStock(tradeItem, trade.itemCount()))) {
            return TradePreview.rejected(trade, "merchant_out_of_stock", reaction);
        }
        int creditPrice = adjustedCreditPrice(
                trade, reaction == null ? 100 : reaction.tradePricePercent());
        if (!player.hasInfiniteMaterials()
                && CreditTransactionService.playerBalance(player) < creditPrice) {
            return TradePreview.rejected(trade, "insufficient_credits", reaction);
        }
        return TradePreview.available(trade, reaction);
    }

    public static TradeResult purchase(ServerPlayer player, UUID eventId, String tradeId) {
        return purchase(player, eventId, tradeId, null, null);
    }

    public static TradeResult purchase(
            ServerPlayer player, UUID eventId, String tradeId, GalacticRecruitEntity merchant
    ) {
        return purchase(player, eventId, tradeId, merchant, null);
    }

    /**
     * Commits a trade only if live server state still satisfies every preview
     * gate and, when supplied, the menu's server-issued item and price quote.
     */
    public static TradeResult purchase(
            ServerPlayer player,
            UUID eventId,
            String tradeId,
            GalacticRecruitEntity merchant,
            TradeQuote expectedQuote
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(eventId, "eventId");
        if (!(player.level() instanceof ServerLevel level)) {
            return TradeResult.rejected("server_only");
        }
        ProgressionSavedData progression = ProgressionSavedData.get(level);
        ProgressionState before = progression.state(player.getUUID());
        if (before.processed(eventId)) {
            return TradeResult.duplicate();
        }

        TradePreview preview = preview(player, tradeId, merchant);
        if (expectedQuote != null && !expectedQuote.matches(preview)) {
            return TradeResult.rejected("offer_changed");
        }
        if (!preview.eligible()) {
            return TradeResult.rejected(preview.reason());
        }
        Item resultItem = registeredItem(preview.itemId());
        if (resultItem == null) {
            return TradeResult.rejected("unknown_trade_item");
        }
        GalacticRecruitEntity physicalStockMerchant = merchant != null
                && merchant.isMarketAvailable()
                ? merchant
                : null;
        ItemStack reservedStock = physicalStockMerchant == null
                ? new ItemStack(resultItem, preview.itemCount())
                : physicalStockMerchant.takeMerchantStock(
                        resultItem, preview.itemCount());
        if (reservedStock.getCount() != preview.itemCount()) {
            return TradeResult.rejected("merchant_out_of_stock");
        }
        ProgressionEvent event = new ProgressionEvent(
                eventId, player.getUUID(), ProgressionEventType.TRADE_COMPLETED,
                preview.tradeId(), 1);
        ProgressionDecision evaluated = GalacticProgressionCoordinator.apply(before, event);
        if (!evaluated.accepted()) {
            return TradeResult.rejected(evaluated.reason());
        }
        if (!evaluated.changed()) {
            return TradeResult.duplicate();
        }
        if (!CreditTransactionService.withdrawPlayer(player, preview.creditPrice())) {
            restoreMerchantStock(level, physicalStockMerchant, reservedStock);
            return TradeResult.rejected("insufficient_credits");
        }

        ProgressionDecision completion;
        try {
            completion = progression.commitEvaluated(event, before, evaluated);
        } catch (RuntimeException failure) {
            CreditTransactionService.refundPlayer(player, preview.creditPrice());
            restoreMerchantStock(level, physicalStockMerchant, reservedStock);
            return TradeResult.rejected("transaction_failed");
        }
        if (!completion.accepted() || !completion.changed()) {
            CreditTransactionService.refundPlayer(player, preview.creditPrice());
            restoreMerchantStock(level, physicalStockMerchant, reservedStock);
            return completion.accepted()
                    ? TradeResult.duplicate()
                    : TradeResult.rejected(completion.reason());
        }

        ItemStack result = reservedStock;
        player.getInventory().add(result);
        if (!result.isEmpty()) {
            player.spawnAtLocation(level, result);
        }
        LaunchContentDefinitions.TradeDefinition completedTrade =
                LaunchContentCatalog.trades().get(preview.tradeId());
        if (completedTrade != null) {
            FactionReputationService.record(
                    level,
                    player.getUUID(),
                    eventId,
                    "galacticwars:" + completedTrade.factionId(),
                    FactionReputationEvent.TRADE_COMPLETED);
        }
        return new TradeResult(
                true, true, "accepted", preview.itemId(), preview.itemCount(), preview.creditPrice());
    }

    public static String reasonTranslationKey(String reason) {
        return switch (reason == null ? "" : reason) {
            case "available", "accepted", "unknown_trade", "server_only", "merchant_unavailable",
                    "hostile_merchant", "trade_embargoed", "trade_locked",
                    "wary_merchant",
                    "veteran_trade_locked", "regional_control_required",
                    "unknown_trade_item", "insufficient_credits",
                    "merchant_out_of_stock",
                    "offer_changed", "duplicate_event", "transaction_failed" ->
                    "reason.galacticwars.trade." + reason;
            default -> "reason.galacticwars.trade.unavailable";
        };
    }

    private static Item registeredItem(String itemId) {
        Identifier resultId;
        try {
            resultId = Identifier.parse(itemId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
        Item resultItem = BuiltInRegistries.ITEM.getValue(resultId);
        return resultItem != null && resultId.equals(BuiltInRegistries.ITEM.getKey(resultItem))
                ? resultItem : null;
    }

    private static void restoreMerchantStock(
            ServerLevel level,
            GalacticRecruitEntity merchant,
            ItemStack stock
    ) {
        if (merchant == null || stock.isEmpty()) {
            return;
        }
        if (!merchant.restoreMerchantStock(stock)) {
            merchant.spawnAtLocation(level, stock.copy());
        }
    }

    private static int adjustedCreditPrice(
            LaunchContentDefinitions.TradeDefinition trade,
            int dispositionPercent
    ) {
        int factionPrice = FactionBalanceService.tradeCreditPrice(
                "galacticwars:" + trade.factionId(), trade.price());
        return Math.max(1, Math.min(
                LaunchContentDefinitions.MAX_TRADE_CREDIT_PRICE,
                FactionBalanceService.applyPercentCeil(
                        factionPrice, dispositionPercent)));
    }

    public record TradePreview(
            String tradeId,
            String itemId,
            int itemCount,
            int creditPrice,
            boolean eligible,
            String reason,
            String disposition
    ) {
        public TradePreview {
            Objects.requireNonNull(tradeId, "tradeId");
            Objects.requireNonNull(itemId, "itemId");
            Objects.requireNonNull(reason, "reason");
            Objects.requireNonNull(disposition, "disposition");
            if (itemCount < 0 || creditPrice < 0) {
                throw new IllegalArgumentException("Trade preview amounts cannot be negative");
            }
            if (eligible && (tradeId.isBlank() || itemId.isBlank()
                    || itemCount == 0 || creditPrice == 0 || !reason.equals("available"))) {
                throw new IllegalArgumentException("Eligible trade preview is incomplete");
            }
        }

        public TradePreview(
                String tradeId,
                String itemId,
                int itemCount,
                int creditPrice,
                boolean eligible,
                String reason
        ) {
            this(tradeId, itemId, itemCount, creditPrice, eligible, reason, "neutral");
        }

        private static TradePreview available(
                LaunchContentDefinitions.TradeDefinition trade,
                NpcReactionDecision reaction
        ) {
            return of(trade, true, "available", reaction);
        }

        private static TradePreview rejected(
                LaunchContentDefinitions.TradeDefinition trade, String reason
        ) {
            return of(trade, false, reason, null);
        }

        private static TradePreview rejected(
                LaunchContentDefinitions.TradeDefinition trade,
                String reason,
                NpcReactionDecision reaction
        ) {
            return of(trade, false, reason, reaction);
        }

        private static TradePreview unknown(String reason) {
            return new TradePreview("", "", 0, 0, false, reason, "neutral");
        }

        private static TradePreview of(
                LaunchContentDefinitions.TradeDefinition trade,
                boolean eligible,
                String reason,
                NpcReactionDecision reaction
        ) {
            int dispositionPercent = reaction == null ? 100 : reaction.tradePricePercent();
            return new TradePreview(
                    trade.id(), trade.itemId(), trade.itemCount(),
                    adjustedCreditPrice(trade, dispositionPercent),
                    eligible, reason,
                    reaction == null
                            ? "neutral"
                            : reaction.disposition().name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    public record TradeQuote(
            String tradeId,
            String itemId,
            int itemCount,
            int creditPrice,
            String disposition
    ) {
        public TradeQuote {
            Objects.requireNonNull(tradeId, "tradeId");
            Objects.requireNonNull(itemId, "itemId");
            Objects.requireNonNull(disposition, "disposition");
            if (tradeId.isBlank() || itemId.isBlank() || itemCount <= 0 || creditPrice <= 0) {
                throw new IllegalArgumentException("Trade quote is incomplete");
            }
        }

        public TradeQuote(String tradeId, String itemId, int itemCount, int creditPrice) {
            this(tradeId, itemId, itemCount, creditPrice, "neutral");
        }

        public boolean matches(TradePreview preview) {
            return preview != null
                    && tradeId.equals(preview.tradeId())
                    && itemId.equals(preview.itemId())
                    && itemCount == preview.itemCount()
                    && creditPrice == preview.creditPrice()
                    && disposition.equals(preview.disposition());
        }
    }

    public record TradeResult(
            boolean accepted,
            boolean changed,
            String reason,
            String itemId,
            int itemCount,
            int creditsCharged
    ) {
        public TradeResult {
            Objects.requireNonNull(reason, "reason");
            Objects.requireNonNull(itemId, "itemId");
        }

        static TradeResult rejected(String reason) {
            return new TradeResult(false, false, reason, "", 0, 0);
        }

        static TradeResult duplicate() {
            return new TradeResult(true, false, "duplicate_event", "", 0, 0);
        }
    }
}
