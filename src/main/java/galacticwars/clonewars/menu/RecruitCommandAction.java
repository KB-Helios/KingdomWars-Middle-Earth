package galacticwars.clonewars.menu;

import galacticwars.clonewars.workforce.WorkerProfession;
import galacticwars.clonewars.workforce.WorkerProfessionCatalog;

import java.util.Optional;

/** Server-authoritative command actions accepted by the recruit command menu. */
public enum RecruitCommandAction {
    HIRE,
    FOLLOW,
    HOLD,
    MOVE,
    PROTECT,
    ATTACK,
    CLEAR,
    SET_WORKSITE,
    RETURN_WORKSITE,
    CLEAR_WORKSITE,
    SET_STORAGE,
    BUILD_STARTER_KEEP,
    WORK_RADIUS_DECREASE,
    WORK_RADIUS_INCREASE,
    PROMOTE_COMMANDER,
    TOGGLE_AUTO_RECRUITMENT,
    START_RECRUITMENT,
    NEXT_BLUEPRINT,
    RETURN_TO_SOLDIER,
    CANCEL_BUILD,
    ASSIGN_WORKER_PROFESSION,
    CYCLE_FORMATION,
    ROTATE_BLUEPRINT,
    PATROL,
    OPEN_LOADOUT,
    OPEN_WORKSITE_CONFIGURATION;

    public static Optional<RecruitCommandAction> fromButtonId(int buttonId) {
        if (WorkerProfessionCatalog.professionForButton(buttonId).isPresent()) {
            return Optional.of(ASSIGN_WORKER_PROFESSION);
        }
        return Optional.ofNullable(switch (buttonId) {
            case RecruitCommandMenu.BUTTON_HIRE -> HIRE;
            case RecruitCommandMenu.BUTTON_FOLLOW -> FOLLOW;
            case RecruitCommandMenu.BUTTON_HOLD -> HOLD;
            case RecruitCommandMenu.BUTTON_MOVE -> MOVE;
            case RecruitCommandMenu.BUTTON_PROTECT -> PROTECT;
            case RecruitCommandMenu.BUTTON_ATTACK -> ATTACK;
            case RecruitCommandMenu.BUTTON_CLEAR -> CLEAR;
            case RecruitCommandMenu.BUTTON_SET_WORKSITE -> SET_WORKSITE;
            case RecruitCommandMenu.BUTTON_RETURN_WORKSITE -> RETURN_WORKSITE;
            case RecruitCommandMenu.BUTTON_CLEAR_WORKSITE -> CLEAR_WORKSITE;
            case RecruitCommandMenu.BUTTON_SET_STORAGE -> SET_STORAGE;
            case RecruitCommandMenu.BUTTON_BUILD_STARTER_KEEP -> BUILD_STARTER_KEEP;
            case RecruitCommandMenu.BUTTON_WORK_RADIUS_DECREASE -> WORK_RADIUS_DECREASE;
            case RecruitCommandMenu.BUTTON_WORK_RADIUS_INCREASE -> WORK_RADIUS_INCREASE;
            case RecruitCommandMenu.BUTTON_PROMOTE_COMMANDER -> PROMOTE_COMMANDER;
            case RecruitCommandMenu.BUTTON_TOGGLE_AUTO_RECRUITMENT -> TOGGLE_AUTO_RECRUITMENT;
            case RecruitCommandMenu.BUTTON_START_RECRUITMENT -> START_RECRUITMENT;
            case RecruitCommandMenu.BUTTON_NEXT_BLUEPRINT -> NEXT_BLUEPRINT;
            case RecruitCommandMenu.BUTTON_RETURN_TO_SOLDIER -> RETURN_TO_SOLDIER;
            case RecruitCommandMenu.BUTTON_CANCEL_BUILD -> CANCEL_BUILD;
            case RecruitCommandMenu.BUTTON_CYCLE_FORMATION -> CYCLE_FORMATION;
            case RecruitCommandMenu.BUTTON_ROTATE_BLUEPRINT -> ROTATE_BLUEPRINT;
            case RecruitCommandMenu.BUTTON_PATROL -> PATROL;
            case RecruitCommandMenu.BUTTON_OPEN_LOADOUT -> OPEN_LOADOUT;
            case RecruitCommandMenu.BUTTON_OPEN_WORKSITE_CONFIGURATION ->
                    OPEN_WORKSITE_CONFIGURATION;
            default -> null;
        });
    }

    public static Optional<WorkerProfession> workerProfession(int buttonId) {
        return WorkerProfessionCatalog.professionForButton(buttonId)
                .filter(WorkerProfessionCatalog::isEnabled);
    }
}
