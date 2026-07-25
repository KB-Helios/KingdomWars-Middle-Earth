package galacticwars.clonewars.menu;

import galacticwars.clonewars.data.LaunchContentDefinitions;
import galacticwars.clonewars.economy.PhysicalTradeService;
import io.netty.handler.codec.DecoderException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;

/** Immutable, bounded offer terms authored by the server for one open merchant menu. */
public record MerchantTradeOffer(
        String tradeId,
        String itemId,
        int itemCount,
        int creditPrice,
        boolean eligible,
        String reasonTranslationKey,
        String disposition
) {
    public static final int MAX_OFFERS = 32;
    public static final int MAX_TEXT_BYTES = LaunchContentDefinitions.MAX_SERIALIZED_TRADE_TEXT_BYTES;

    public MerchantTradeOffer {
        tradeId = boundedText(tradeId, "tradeId");
        itemId = boundedText(itemId, "itemId");
        reasonTranslationKey = boundedText(reasonTranslationKey, "reasonTranslationKey");
        disposition = boundedText(disposition, "disposition");
        if (itemCount <= 0 || itemCount > LaunchContentDefinitions.MAX_TRADE_ITEM_COUNT) {
            throw new IllegalArgumentException("itemCount is outside the merchant offer limit");
        }
        if (creditPrice <= 0 || creditPrice > LaunchContentDefinitions.MAX_TRADE_CREDIT_PRICE) {
            throw new IllegalArgumentException("creditPrice is outside the merchant offer limit");
        }
    }

    public MerchantTradeOffer(
            String tradeId,
            String itemId,
            int itemCount,
            int creditPrice,
            boolean eligible,
            String reasonTranslationKey
    ) {
        this(tradeId, itemId, itemCount, creditPrice, eligible, reasonTranslationKey, "neutral");
    }

    public static MerchantTradeOffer fromPreview(PhysicalTradeService.TradePreview preview) {
        Objects.requireNonNull(preview, "preview");
        if (preview.tradeId().isBlank() || preview.itemId().isBlank()
                || preview.itemCount() <= 0 || preview.creditPrice() <= 0) {
            throw new IllegalArgumentException("Cannot publish an unknown trade as a merchant offer");
        }
        return new MerchantTradeOffer(
                preview.tradeId(), preview.itemId(), preview.itemCount(), preview.creditPrice(),
                preview.eligible(), PhysicalTradeService.reasonTranslationKey(preview.reason()),
                preview.disposition());
    }

    public PhysicalTradeService.TradeQuote quote() {
        return new PhysicalTradeService.TradeQuote(
                tradeId, itemId, itemCount, creditPrice, disposition);
    }

    public static void writeOffers(FriendlyByteBuf buffer, List<MerchantTradeOffer> offers) {
        Objects.requireNonNull(buffer, "buffer");
        Objects.requireNonNull(offers, "offers");
        if (offers.size() > MAX_OFFERS) {
            throw new IllegalArgumentException("merchant offer count exceeds " + MAX_OFFERS);
        }
        buffer.writeVarInt(offers.size());
        for (MerchantTradeOffer offer : offers) {
            Objects.requireNonNull(offer, "offer").write(buffer);
        }
    }

    public static List<MerchantTradeOffer> readOffers(FriendlyByteBuf buffer) {
        Objects.requireNonNull(buffer, "buffer");
        int count = buffer.readVarInt();
        if (count < 0 || count > MAX_OFFERS) {
            throw new DecoderException("merchant offer count exceeds " + MAX_OFFERS);
        }
        ArrayList<MerchantTradeOffer> offers = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            offers.add(read(buffer));
        }
        return List.copyOf(offers);
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(tradeId, MAX_TEXT_BYTES);
        buffer.writeUtf(itemId, MAX_TEXT_BYTES);
        buffer.writeVarInt(itemCount);
        buffer.writeVarInt(creditPrice);
        buffer.writeBoolean(eligible);
        buffer.writeUtf(reasonTranslationKey, MAX_TEXT_BYTES);
        buffer.writeUtf(disposition, MAX_TEXT_BYTES);
    }

    private static MerchantTradeOffer read(FriendlyByteBuf buffer) {
        try {
            return new MerchantTradeOffer(
                    buffer.readUtf(MAX_TEXT_BYTES),
                    buffer.readUtf(MAX_TEXT_BYTES),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readBoolean(),
                    buffer.readUtf(MAX_TEXT_BYTES),
                    buffer.readUtf(MAX_TEXT_BYTES));
        } catch (IllegalArgumentException exception) {
            throw new DecoderException("Invalid merchant offer", exception);
        }
    }

    private static String boundedText(String value, String label) {
        Objects.requireNonNull(value, label);
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " cannot be blank");
        }
        if (value.length() > MAX_TEXT_BYTES
                || value.getBytes(StandardCharsets.UTF_8).length > MAX_TEXT_BYTES) {
            throw new IllegalArgumentException(label + " exceeds " + MAX_TEXT_BYTES + " UTF-8 bytes");
        }
        return value;
    }
}
