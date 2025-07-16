package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.resources.ItemRule;
import net.minecraft.util.ResourceLocation;

public class NBTHolder {
    public final ResourceLocation texture;
    private final ResourceLocation model;
    private final ItemRule rule;

    public NBTHolder(ResourceLocation texture, ResourceLocation model, ItemRule rule) {
        this.texture = texture;
        this.model = model;
        this.rule = rule;
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
}
