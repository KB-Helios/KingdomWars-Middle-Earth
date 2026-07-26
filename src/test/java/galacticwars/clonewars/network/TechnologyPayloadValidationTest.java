package galacticwars.clonewars.network;

import java.util.Optional;
import java.util.UUID;

public final class TechnologyPayloadValidationTest {
    private TechnologyPayloadValidationTest() {
    }

    public static void main(String[] args) {
        acceptsBoundedFabricationAndResearchRequests();
        rejectsMalformedFabricationRequests();
        rejectsMalformedResearchRequests();
        System.out.println("TechnologyPayloadValidationTest passed");
    }

    private static void acceptsBoundedFabricationAndResearchRequests() {
        UUID replay = UUID.randomUUID();
        FabricationRequestPayload fabrication = new FabricationRequestPayload(
                replay, 7, "galacticwars:dc15_blaster", 4L, 3);
        if (!fabrication.replayId().equals(replay) || fabrication.containerId() != 7) {
            throw new AssertionError("valid fabrication request changed");
        }
        ResearchActionPayload research = new ResearchActionPayload(
                replay,
                8,
                ResearchActionPayload.ASSIGN_TECHNICIAN,
                "",
                Optional.of(UUID.randomUUID()),
                4L,
                3);
        if (research.action() != ResearchActionPayload.ASSIGN_TECHNICIAN) {
            throw new AssertionError("valid research request changed");
        }
    }

    private static void rejectsMalformedFabricationRequests() {
        expectFailure(() -> new FabricationRequestPayload(
                UUID.randomUUID(), -1, "galacticwars:dc15_blaster", 0L, 0),
                "negative container");
        expectFailure(() -> new FabricationRequestPayload(
                UUID.randomUUID(), 1, "", 0L, 0),
                "blank recipe");
        expectFailure(() -> new FabricationRequestPayload(
                UUID.randomUUID(),
                1,
                "x".repeat(FabricationRequestPayload.MAX_RECIPE_ID_LENGTH + 1),
                0L,
                0),
                "oversized recipe");
        expectFailure(() -> new FabricationRequestPayload(
                UUID.randomUUID(), 1, "galacticwars:dc15_blaster", -1L, 0),
                "negative generation");
        expectFailure(() -> new FabricationRequestPayload(
                UUID.randomUUID(), 1, "galacticwars:dc15_blaster", 0L, -1),
                "negative technology revision");
    }

    private static void rejectsMalformedResearchRequests() {
        expectFailure(() -> new ResearchActionPayload(
                UUID.randomUUID(), 1, -1, "", Optional.empty(), 0L, 0),
                "unknown action");
        expectFailure(() -> new ResearchActionPayload(
                UUID.randomUUID(),
                1,
                ResearchActionPayload.START,
                "",
                Optional.empty(),
                0L,
                0),
                "blank start node");
        expectFailure(() -> new ResearchActionPayload(
                UUID.randomUUID(),
                1,
                ResearchActionPayload.ASSIGN_TECHNICIAN,
                "",
                Optional.empty(),
                0L,
                0),
                "missing technician");
        expectFailure(() -> new ResearchActionPayload(
                UUID.randomUUID(),
                1,
                ResearchActionPayload.START,
                "x".repeat(129),
                Optional.empty(),
                0L,
                0),
                "oversized technology node");
        expectFailure(() -> new ResearchActionPayload(
                UUID.randomUUID(),
                1,
                ResearchActionPayload.CANCEL,
                "",
                Optional.empty(),
                -1L,
                0),
                "negative catalog generation");
    }

    private static void expectFailure(Runnable action, String label) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(label + " was accepted");
    }
}
