package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.resources.NBTRule;
import net.minecraft.util.ResourceLocation;

public class NBTHolder {
    public final ResourceLocation texture;
    private final ResourceLocation model;
    private final NBTRule rule;

    public NBTHolder(ResourceLocation texture, ResourceLocation model, NBTRule rule) {
        this.texture = texture;
        this.model = model;
        this.rule = rule;
    }

    public NBTRule getRule() {
        return rule;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}
