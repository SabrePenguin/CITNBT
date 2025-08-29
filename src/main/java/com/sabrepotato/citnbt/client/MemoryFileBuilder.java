package com.sabrepotato.citnbt.client;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

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
        InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(MemoryFileBuilder.class.getResourceAsStream("/assets/"
                        + location.getNamespace() + "/models/item/"
                        + location.getPath() + ".json"
                ))
        );
        JsonParser parser = new JsonParser();
        return parser.parse(reader).getAsJsonObject();
    }

    public static byte[] cloneAndModify(String original, String texture) throws IOException {
        original = original.replaceAll("^(.*:)?item[s]/", "");
        JsonObject json = loadModelJson(new ResourceLocation(original));

        JsonObject textures = json.has("textures") ? json.getAsJsonObject("textures") : new JsonObject();
        textures.addProperty("layer0", texture);
        json.add("textures", textures);
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static JsonObject loadModelJson(ResourceLocation loc) throws IOException {
        IResource res = Minecraft.getMinecraft().getResourceManager().getResource(
                new ResourceLocation(loc.getNamespace(), "models/item/" + loc.getPath() + ".json")
        );

        try (InputStreamReader reader = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
