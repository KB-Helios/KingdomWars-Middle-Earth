package galacticwars.clonewars.network;

import galacticwars.clonewars.GalacticWars;
import galacticwars.clonewars.menu.WorksiteConfigurationSnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Authoritative worksite snapshot sent after open and every accepted/rejected edit. */
public record WorksiteStatePayload(
        int containerId,
        WorksiteConfigurationSnapshot snapshot
) implements CustomPacketPayload {
    public static final Type<WorksiteStatePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GalacticWars.MODID, "worksite_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorksiteStatePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, value) -> {
                        buffer.writeVarInt(value.containerId());
                        WorksiteConfigurationSnapshot.write(buffer, value.snapshot());
                    },
                    buffer -> new WorksiteStatePayload(
                            buffer.readVarInt(),
                            WorksiteConfigurationSnapshot.read(buffer)));

    public WorksiteStatePayload {
        if (containerId < 0 || snapshot == null) {
            throw new IllegalArgumentException("invalid worksite state payload");
        }
    }

    @Override
    public Type<WorksiteStatePayload> type() {
        return TYPE;
    }
}
