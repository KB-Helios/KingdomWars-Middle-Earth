package galacticwars.clonewars.workforce;

import galacticwars.clonewars.kingdom.KingdomPermission;
import galacticwars.clonewars.kingdom.KingdomRecord;
import galacticwars.clonewars.kingdom.KingdomSavedData;
import galacticwars.clonewars.kingdom.WorksiteRecord;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-authorized worksite boundary projection. Using targeted particles keeps
 * the overlay loader-neutral and prevents clients from rendering worksites they
 * are not permitted to inspect.
 */
public final class WorksiteOverlayService {
    private static final int INTERVAL_TICKS = 10;
    private static final double MAX_DISTANCE_SQUARED = 96.0D * 96.0D;

    private WorksiteOverlayService() {
    }

    public static void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        KingdomSavedData data = KingdomSavedData.get(server.overworld());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            KingdomRecord kingdom = data.kingdomForPlayer(player.getUUID()).orElse(null);
            if (kingdom == null
                    || !data.isHallActive(kingdom.ownerId())
                    || !kingdom.allows(
                            player.getUUID(), KingdomPermission.MANAGE_WORKSITES)) {
                continue;
            }
            String dimensionId =
                    player.level().dimension().identifier().toString();
            for (var settlement : kingdom.settlements()) {
                for (WorksiteRecord worksite : settlement.worksites()) {
                    if (!worksite.dimensionId().equals(dimensionId)
                            || !worksite.configuration().overlayVisible()
                            || !worksite.configuration().kingdomAccess()
                                    && !kingdom.ownerId().equals(player.getUUID())
                            || player.distanceToSqr(
                                    worksite.x() + 0.5D,
                                    worksite.y() + 0.5D,
                                    worksite.z() + 0.5D)
                                    > MAX_DISTANCE_SQUARED) {
                        continue;
                    }
                    renderBounds((ServerLevel) player.level(), player, worksite);
                }
            }
        }
    }

    private static void renderBounds(
            ServerLevel level,
            ServerPlayer player,
            WorksiteRecord worksite
    ) {
        WorkAreaBounds bounds = worksite.configuration().bounds();
        double minX = worksite.x() - (bounds.width() - 1) / 2.0D - 0.5D;
        double maxX = worksite.x() + bounds.width() / 2.0D + 0.5D;
        double minY = worksite.y() - (bounds.height() - 1) / 2.0D - 0.5D;
        double maxY = worksite.y() + bounds.height() / 2.0D + 0.5D;
        double minZ = worksite.z() - (bounds.depth() - 1) / 2.0D - 0.5D;
        double maxZ = worksite.z() + bounds.depth() / 2.0D + 0.5D;
        double midX = (minX + maxX) * 0.5D;
        double midY = (minY + maxY) * 0.5D;
        double midZ = (minZ + maxZ) * 0.5D;
        double[][] markers = {
                {minX, minY, minZ}, {minX, minY, maxZ},
                {maxX, minY, minZ}, {maxX, minY, maxZ},
                {minX, maxY, minZ}, {minX, maxY, maxZ},
                {maxX, maxY, minZ}, {maxX, maxY, maxZ},
                {midX, minY, minZ}, {midX, minY, maxZ},
                {minX, minY, midZ}, {maxX, minY, midZ},
                {minX, midY, minZ}, {minX, midY, maxZ},
                {maxX, midY, minZ}, {maxX, midY, maxZ}
        };
        for (double[] marker : markers) {
            level.sendParticles(
                    player,
                    ParticleTypes.ELECTRIC_SPARK,
                    false,
                    false,
                    marker[0],
                    marker[1],
                    marker[2],
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }
}
