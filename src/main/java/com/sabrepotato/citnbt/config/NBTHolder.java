package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.resources.ItemRule;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class NBTHolder implements Comparable<NBTHolder> {
    public final ResourceLocation texture;
    private final Map<String, String> texture_set;
    private final ResourceLocation model;
    private final ItemRule rule;
    private final String filename;
    private final int weight;

    public NBTHolder(ResourceLocation texture, ResourceLocation model, ItemRule rule, String filename, int weight) {
        this.texture = texture;
        this.model = model;
        this.rule = rule;
        this.filename = filename;
        this.weight = weight;
        this.texture_set = null;
    }

    public NBTHolder(Map<String, String> texture, ResourceLocation model, ItemRule rule, String filename, int weight) {
        this.texture_set = texture;
        this.texture = null;
        this.model = model;
        this.rule = rule;
        this.filename = filename;
        this.weight = weight;
    }

    public ItemRule getRule() {
        return rule;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public boolean isModelOverload() {
        return texture_set != null;
    }

    public Map<String, String> getTextureSet() {
        return texture_set;
    }

    @Override
    public int compareTo(@NotNull NBTHolder other) {
        if (this.weight != other.weight) return (-1)*Integer.compare(this.weight, other.weight);
        if (!Objects.equals(this.filename, other.filename)) return this.filename.compareTo(other.filename);
        return 0; // We don't care about the order
    }
}
