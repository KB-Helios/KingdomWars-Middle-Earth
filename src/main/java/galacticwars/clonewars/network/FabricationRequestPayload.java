package galacticwars.clonewars.network;

import galacticwars.clonewars.GalacticWars;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FabricationRequestPayload(
        UUID replayId,
        int containerId,
        String recipeId,
        long catalogGeneration,
        int technologyRevision
) implements CustomPacketPayload {
    public static final int MAX_RECIPE_ID_LENGTH = 128;
    public static final Type<FabricationRequestPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "fabricate"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FabricationRequestPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeUUID(payload.replayId());
                        buffer.writeVarInt(payload.containerId());
                        buffer.writeUtf(payload.recipeId(), MAX_RECIPE_ID_LENGTH);
                        buffer.writeVarLong(payload.catalogGeneration());
                        buffer.writeVarInt(payload.technologyRevision());
                    },
                    buffer -> new FabricationRequestPayload(
                            buffer.readUUID(),
                            buffer.readVarInt(),
                            buffer.readUtf(MAX_RECIPE_ID_LENGTH),
                            buffer.readVarLong(),
                            buffer.readVarInt()));

    public FabricationRequestPayload {
        Objects.requireNonNull(replayId, "replayId");
        recipeId = Objects.requireNonNull(recipeId, "recipeId").trim();
        if (containerId < 0 || recipeId.isBlank() || recipeId.length() > MAX_RECIPE_ID_LENGTH
                || catalogGeneration < 0L || technologyRevision < 0) {
            throw new IllegalArgumentException("Invalid fabrication request");
        }
    }

    @Override
    public Type<FabricationRequestPayload> type() {
        return TYPE;
    }
}
