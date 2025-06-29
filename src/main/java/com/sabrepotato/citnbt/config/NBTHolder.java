package com.sabrepotato.citnbt.config;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Objects;

public class NBTHolder {
    public final ModelResourceLocation itemId;
    public final List<NBTCondition> rules;
    public final ResourceLocation texture;

    public NBTHolder(ModelResourceLocation itemId, List<NBTCondition> rules, ResourceLocation texture) {
        this.itemId = itemId;
        this.rules = rules;
        this.texture = texture;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NBTHolder other)) return false;
        return Objects.equals(rules, other.rules)
                && Objects.equals(itemId, other.itemId)
                && Objects.equals(texture, other.texture);
    }

    public boolean matches(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        for(NBTCondition condition: rules) {
            if (!condition.matches(compound)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, rules, texture);
    }
}
