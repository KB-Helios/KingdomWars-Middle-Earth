package galacticwars.clonewars.network;

import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.menu.WorksiteConfigurationAction;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Replay-safe, revision-checked worksite intent without client-authored world positions. */
public record WorksiteActionPayload(
        UUID replayId,
        int containerId,
        int actionId,
        long expectedConfigurationRevision,
        int selectedIndex
) implements CustomPacketPayload {
    public static final Type<WorksiteActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "worksite_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorksiteActionPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, value) -> {
                        buffer.writeUUID(value.replayId());
                        buffer.writeVarInt(value.containerId());
                        buffer.writeVarInt(value.actionId());
                        buffer.writeVarLong(value.expectedConfigurationRevision());
                        buffer.writeVarInt(value.selectedIndex() + 1);
                    },
                    buffer -> new WorksiteActionPayload(
                            buffer.readUUID(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarLong(),
                            buffer.readVarInt() - 1));

    public WorksiteActionPayload {
        if (replayId == null || containerId < 0
                || WorksiteConfigurationAction.byId(actionId).isEmpty()
                || expectedConfigurationRevision < 0L
                || selectedIndex < -1 || selectedIndex >= 64) {
            throw new IllegalArgumentException("invalid worksite action payload");
        }
    }

    @Override
    public Type<WorksiteActionPayload> type() {
        return TYPE;
    }
}
