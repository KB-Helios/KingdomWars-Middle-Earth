package galacticwars.clonewars.network;

import java.nio.file.Files;
import java.nio.file.Path;

public final class NetworkThreadDispatchTest {
    private NetworkThreadDispatchTest() {
    }

    public static void main(String[] args) throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/galacticwars/clonewars/network/GalacticNetwork.java"));
        int dispatches = source.split("context\\.queue", -1).length - 1;
        if (dispatches != 19) {
            throw new AssertionError("Every Architectury payload handler must dispatch to the main thread");
        }
        int c2sRegistrations = source.split("NetworkManager\\.registerC2S", -1).length - 1;
        int s2cRegistrations = source.split("NetworkManager\\.registerS2C", -1).length - 1;
        if (c2sRegistrations != 10 || s2cRegistrations != 9
                || !source.contains("CustomPacketPayload")) {
            throw new AssertionError("Architectury networking must register all nineteen typed payload directions");
        }
        System.out.println("NetworkThreadDispatchTest passed");
    }
}
