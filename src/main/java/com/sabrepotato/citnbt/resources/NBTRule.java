package com.sabrepotato.citnbt.resources;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.Objects;

public class NBTRule {
    public final List<NBTCondition> conditions;
    public final ModelResourceLocation location;

    public NBTRule(List<NBTCondition> conditions, ModelResourceLocation location) {
        this.conditions = conditions;
        this.location = location;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NBTRule other)) return false;
        return Objects.equals(conditions, other.conditions);
    }

    public boolean matches(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        for(NBTCondition condition: conditions) {
            if (!condition.matches(compound)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditions);
    }

    public ModelResourceLocation getLocation() {
        return location;
    }
}
