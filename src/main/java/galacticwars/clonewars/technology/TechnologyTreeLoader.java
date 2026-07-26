package galacticwars.clonewars.technology;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import galacticwars.clonewars.GalacticWars;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class TechnologyTreeLoader {
    private static final FileToIdConverter CONVERTER =
            FileToIdConverter.json("galacticwars/technology");

    private TechnologyTreeLoader() {
    }

    public static TechnologyCatalog load(ResourceManager manager, Set<String> factionPaths) throws IOException {
        LinkedHashSet<String> factions = new LinkedHashSet<>();
        factionPaths.forEach(path -> factions.add(GalacticWars.MODID + ":" + path));
        LinkedHashMap<String, TechnologyNodeDefinition> nodes = new LinkedHashMap<>();
        for (var entry : CONVERTER.listMatchingResources(manager).entrySet()) {
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                String factionId = requiredString(root, "faction");
                for (JsonElement element : root.getAsJsonArray("nodes")) {
                    JsonObject json = element.getAsJsonObject();
                    String id = requiredString(json, "id");
                    Set<String> prerequisites = strings(json, "prerequisites");
                    Map<String, Integer> inputs = counts(json.getAsJsonObject("inputs"));
                    Set<String> recipes = strings(json, "recipes");
                    TechnologyNodeDefinition node = new TechnologyNodeDefinition(
                            id,
                            factionId,
                            requiredString(json, "name"),
                            prerequisites,
                            inputs,
                            json.get("work").getAsInt(),
                            recipes);
                    if (nodes.putIfAbsent(id, node) != null) {
                        throw new IllegalArgumentException("Duplicate technology node " + id);
                    }
                    for (String recipeId : recipes) {
                        Identifier recipe = Identifier.parse(recipeId);
                        Identifier resource = Identifier.fromNamespaceAndPath(
                                recipe.getNamespace(), "recipe/" + recipe.getPath() + ".json");
                        if (manager.getResource(resource).isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Technology " + id + " references unknown recipe " + recipeId);
                        }
                    }
                }
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException(
                        "Invalid technology tree " + CONVERTER.fileToId(entry.getKey()), exception);
            }
        }
        return new TechnologyCatalog(nodes, factions);
    }

    private static String requiredString(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonPrimitive()
                || json.get(key).getAsString().isBlank()) {
            throw new IllegalArgumentException("Missing string " + key);
        }
        return json.get(key).getAsString();
    }

    private static Set<String> strings(JsonObject json, String key) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (!json.has(key)) {
            return values;
        }
        json.getAsJsonArray(key).forEach(value -> {
            String text = value.getAsString();
            if (!values.add(text)) {
                throw new IllegalArgumentException("Duplicate " + key + " entry " + text);
            }
        });
        return values;
    }

    private static Map<String, Integer> counts(JsonObject json) {
        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        for (var entry : json.entrySet()) {
            values.put(entry.getKey(), entry.getValue().getAsInt());
        }
        return values;
    }
}
