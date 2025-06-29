package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.resources.NBTCondition;
import com.sabrepotato.citnbt.resources.NBTRule;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Objects;

public class NBTHolder {
    public final ResourceLocation texture;
    private final NBTRule rule;

    public NBTHolder(ResourceLocation texture, NBTRule rule) {
        this.texture = texture;
        this.rule = rule;
    }

    public NBTRule getRule() {
        return rule;
    }
    public ResourceLocation getTexture() {
        return texture;
    }
}
