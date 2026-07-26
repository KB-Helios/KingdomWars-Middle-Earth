package galacticwars.clonewars.world;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum BlueprintSiteKind {
    OUTPOST("outpost"),
    COMMAND_CENTER("command_center");

    public static final Codec<BlueprintSiteKind> CODEC =
            Codec.STRING.xmap(BlueprintSiteKind::byId, BlueprintSiteKind::id);

    private final String id;

    BlueprintSiteKind(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static BlueprintSiteKind byId(String id) {
        String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        for (BlueprintSiteKind kind : values()) {
            if (kind.id.equals(normalized)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("unknown blueprint site kind " + id);
    }
}
