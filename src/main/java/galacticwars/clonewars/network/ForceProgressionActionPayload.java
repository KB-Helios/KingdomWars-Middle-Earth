package galacticwars.clonewars.network;

import galacticwars.clonewars.GalacticWars;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ForceProgressionActionPayload(
        UUID replayId,
        BlockPos shrinePos,
        int action,
        String subjectId,
        int slot
) implements CustomPacketPayload {
    public static final int LEARN = 0;
    public static final int EQUIP = 1;
    public static final int RESPEC = 2;
    public static final int RITUAL = 3;

    public ForceProgressionActionPayload {
        Objects.requireNonNull(replayId, "replayId");
        Objects.requireNonNull(shrinePos, "shrinePos");
        Objects.requireNonNull(subjectId, "subjectId");
        if (action < LEARN || action > RITUAL || subjectId.length() > 128 || slot < -1 || slot > 2) {
            throw new IllegalArgumentException("Invalid Force progression action");
        }
    }

    public static final Type<ForceProgressionActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "force_progression_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ForceProgressionActionPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {
                buffer.writeUUID(payload.replayId());
                buffer.writeLong(payload.shrinePos().asLong());
                buffer.writeVarInt(payload.action());
                buffer.writeUtf(payload.subjectId(), 128);
                buffer.writeVarInt(payload.slot());
            }, buffer -> new ForceProgressionActionPayload(
                    buffer.readUUID(), BlockPos.of(buffer.readLong()), buffer.readVarInt(),
                    buffer.readUtf(128), buffer.readVarInt()));

    @Override
    public Type<ForceProgressionActionPayload> type() {
        return TYPE;
    }
}
