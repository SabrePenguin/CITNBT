package com.sabrepotato.citnbt.client;

import com.google.gson.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class MemoryFileBuilder {
    /**
     * Loads the stages and replaces the existing overrides and textures
     * @param location The location of the model
     * @param mappedStages A mutable reference to the map. This will remove the default
     *                     texture.
     * @return The byte representation of the json
     */
    public static byte[] loadStages(ModelResourceLocation location, Map<String, String> mappedStages) {
        JsonObject base = loadJsonFromResource(location);
//        JsonObject stageJson = deepCopy(base);

        JsonElement textures = base.get("textures");
        if (textures != null && textures.isJsonObject()) {
            JsonObject texture = textures.getAsJsonObject();
            JsonElement l0_element = texture.get("layer0");
            if (l0_element != null && l0_element.isJsonPrimitive()) {
                JsonPrimitive layer0 = l0_element.getAsJsonPrimitive();
                String l0 = layer0.getAsString();
                String new_tex = mappedStages.remove(l0);
                if (new_tex != null) {
                    texture.addProperty("layer0", new_tex);
                }
            }

        }
        JsonElement overrides = base.get("overrides");
        if (overrides != null && overrides.isJsonArray()) {
            for(JsonElement element: overrides.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    JsonObject o = element.getAsJsonObject();
                    JsonElement model_element = o.get("model");
                    if (model_element != null && model_element.isJsonPrimitive()) {
                        JsonPrimitive model = model_element.getAsJsonPrimitive();
                        String model_tex = model.getAsString();
                        model_tex = model_tex.replaceAll("^(.*:)?item(?=/)", "$1items");
                        String new_tex = mappedStages.get(model_tex);

                        if (new_tex != null) {
                            o.addProperty("model", new_tex);
                        }
                    }
                }
            }
        }
        byte[] bytes = base.toString().getBytes(StandardCharsets.UTF_8);
        return bytes;
    }

    private static JsonObject loadJsonFromResource(ModelResourceLocation location) {
        String loc = "/assets/" + location.getNamespace() + "/models/item" + location.getPath() + ".json";
        InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(MemoryFileBuilder.class.getResourceAsStream("/assets/"
                        + location.getNamespace() + "/models/item/"
                        + location.getPath() + ".json"
                ))
        );
        JsonParser parser = new JsonParser();
        return parser.parse(reader).getAsJsonObject();
    }
}
