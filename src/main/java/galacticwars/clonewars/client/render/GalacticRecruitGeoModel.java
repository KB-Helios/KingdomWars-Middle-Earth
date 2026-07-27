package galacticwars.clonewars.client.render;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import galacticwars.clonewars.combat.BlasterItem;
import galacticwars.clonewars.entity.GalacticRecruitEntity;
import galacticwars.clonewars.recruitment.RecruitDuty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

/** Default recruit model with a synchronized, duty-sensitive texture selection. */
public final class GalacticRecruitGeoModel extends DefaultedEntityGeoModel<GalacticRecruitEntity> {
    public static final DataTicket<RecruitDuty> RECRUIT_DUTY = DataTickets.create(
            "galacticwars:recruit_duty", RecruitDuty.class);
    public static final DataTicket<BlasterStance> BLASTER_STANCE = DataTickets.create(
            "galacticwars:recruit_blaster_stance", BlasterStance.class);

    private final EntityType<GalacticRecruitEntity> entityType;

    public GalacticRecruitGeoModel(EntityType<GalacticRecruitEntity> entityType) {
        super(entityType);
        this.entityType = entityType;
    }

    @Override
    public void addAdditionalStateData(
            GalacticRecruitEntity animatable,
            Object relatedObject,
            GeoRenderState renderState
    ) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        RecruitDuty duty = animatable == null ? RecruitDuty.SOLDIER : animatable.getRecruitDuty();
        renderState.addGeckolibData(RECRUIT_DUTY, duty);
        renderState.addGeckolibData(BLASTER_STANCE, blasterStance(animatable));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        RecruitDuty duty = renderState.getOrDefaultGeckolibData(RECRUIT_DUTY, RecruitDuty.SOLDIER);
        return RecruitVisualProfileCatalog.textureResource(this.entityType, duty);
    }

    private static BlasterStance blasterStance(GalacticRecruitEntity recruit) {
        if (recruit == null || !(recruit.getMainHandItem().getItem() instanceof BlasterItem blaster)) {
            return BlasterStance.NONE;
        }
        return "westar_blaster".equals(blaster.visualId())
                ? BlasterStance.PISTOL
                : BlasterStance.RIFLE;
    }

    public enum BlasterStance {
        NONE,
        RIFLE,
        PISTOL
    }
}
