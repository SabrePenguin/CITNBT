package com.sabrepotato.citnbt.resources;

import com.sabrepotato.citnbt.resources.conditions.ItemstackCondition;
import com.sabrepotato.citnbt.resources.conditions.NBTCondition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ItemRule {
    public final List<NBTCondition> conditions;
    public final ItemstackCondition stackCondition;
    public final ModelResourceLocation location;

    public ItemRule(List<NBTCondition> conditions, ModelResourceLocation location, ItemstackCondition itemstackCondition) {
        this.conditions = conditions;
        this.location = location;
        this.stackCondition = itemstackCondition;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemRule other)) return false;
        return Objects.equals(conditions, other.conditions);
    }

    public boolean matches(ItemStack stack, @Nullable EntityLivingBase player) {
        //TODO: Add nullability to each type of condition to allow skipping unused conditions
        NBTTagCompound compound = stack.getTagCompound();
        for(NBTCondition condition: conditions) {
            if (!condition.matches(compound)) return false;
        }
        if (!stackCondition.checkConditions(stack, player)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditions.hashCode(), stackCondition, location);
    }

    public ModelResourceLocation getLocation() {
        return location;
    }
}
