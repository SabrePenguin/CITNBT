package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.resources.ItemRule;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NBTHolder implements Comparable<NBTHolder> {
    public final ResourceLocation texture;
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

    @Override
    public int compareTo(@NotNull NBTHolder other) {
        if (this.weight != other.weight) return (-1)*Integer.compare(this.weight, other.weight);
        if (!Objects.equals(this.filename, other.filename)) return this.filename.compareTo(other.filename);
        return 0; // We don't care about the order
    }
}
