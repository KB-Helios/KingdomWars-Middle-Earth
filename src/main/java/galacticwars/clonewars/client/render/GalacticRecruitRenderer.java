package galacticwars.clonewars.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityType;

public class GalacticRecruitRenderer<R extends EntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<GalacticRecruitEntity, R> {
    public GalacticRecruitRenderer(
            EntityRendererProvider.Context context,
            EntityType<GalacticRecruitEntity> entityType
    ) {
        super(context, new GalacticRecruitGeoModel(entityType));
        this.withRenderLayer(new ItemInHandGeoLayer<>(context, this));
        this.withScale(1.0F);
    }

    @Override
    public void adjustModelBonesForRender(
            RenderPassInfo<R> renderPassInfo,
            BoneSnapshots snapshots
    ) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        GalacticRecruitGeoModel.BlasterStance stance = renderPassInfo.getOrDefaultGeckolibData(
                GalacticRecruitGeoModel.BLASTER_STANCE,
                GalacticRecruitGeoModel.BlasterStance.NONE);
        switch (stance) {
            case RIFLE -> applyRifleStance(snapshots);
            case PISTOL -> applyPistolStance(snapshots);
            case NONE -> {
            }
        }
    }

    private static void applyRifleStance(BoneSnapshots snapshots) {
        snapshots.ifPresent("right_arm", arm -> arm.setRotation(
                radians(-76.0F),
                radians(-7.0F),
                radians(-11.0F)));
        snapshots.ifPresent("left_arm", arm -> arm.setRotation(
                radians(-68.0F),
                radians(23.0F),
                radians(20.0F)));
    }

    private static void applyPistolStance(BoneSnapshots snapshots) {
        snapshots.ifPresent("right_arm", arm -> arm.setRotation(
                radians(-84.0F),
                radians(-4.0F),
                radians(-6.0F)));
        snapshots.ifPresent("left_arm", arm -> arm.setRotation(
                radians(-8.0F),
                0.0F,
                radians(7.0F)));
    }

    private static float radians(float degrees) {
        return (float) Math.toRadians(degrees);
    }
}
