package galacticwars.clonewars.network;

import galacticwars.clonewars.GalacticWars;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ResearchActionPayload(
        UUID replayId,
        int containerId,
        int action,
        String nodeId,
        Optional<UUID> technicianId,
        long catalogGeneration,
        int technologyRevision
) implements CustomPacketPayload {
    public static final int START = 0;
    public static final int CONTRIBUTE = 1;
    public static final int ASSIGN_TECHNICIAN = 2;
    public static final int CANCEL = 3;
    public static final Type<ResearchActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "research_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchActionPayload> STREAM_CODEC =
            StreamCodec.of((buffer, value) -> {
                buffer.writeUUID(value.replayId());
                buffer.writeVarInt(value.containerId());
                buffer.writeVarInt(value.action());
                buffer.writeUtf(value.nodeId(), 128);
                buffer.writeBoolean(value.technicianId().isPresent());
                value.technicianId().ifPresent(buffer::writeUUID);
                buffer.writeVarLong(value.catalogGeneration());
                buffer.writeVarInt(value.technologyRevision());
            }, buffer -> new ResearchActionPayload(
                    buffer.readUUID(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readUtf(128),
                    buffer.readBoolean() ? Optional.of(buffer.readUUID()) : Optional.empty(),
                    buffer.readVarLong(),
                    buffer.readVarInt()));

    public ResearchActionPayload {
        technicianId = technicianId == null ? Optional.empty() : technicianId;
        nodeId = nodeId == null ? "" : nodeId.trim();
        if (replayId == null || containerId < 0 || action < START || action > CANCEL
                || nodeId.length() > 128 || catalogGeneration < 0L || technologyRevision < 0
                || (action == START && nodeId.isBlank())
                || (action == ASSIGN_TECHNICIAN && technicianId.isEmpty())) {
            throw new IllegalArgumentException("Invalid research action");
        }
    }

    @Override
    public Type<ResearchActionPayload> type() {
        return TYPE;
    }
}
