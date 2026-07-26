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
        if (dispatches != 17) {
            throw new AssertionError("Every Architectury payload handler must dispatch to the main thread");
        }
        int c2sRegistrations = source.split("NetworkManager\\.registerC2S", -1).length - 1;
        int s2cRegistrations = source.split("NetworkManager\\.registerS2C", -1).length - 1;
        if (c2sRegistrations != 9 || s2cRegistrations != 8
                || !source.contains("CustomPacketPayload")) {
            throw new AssertionError("Architectury networking must register all seventeen typed payload directions");
        }
        System.out.println("NetworkThreadDispatchTest passed");
    }
}
