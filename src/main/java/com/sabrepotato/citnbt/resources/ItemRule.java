package com.sabrepotato.citnbt.resources;

import com.sabrepotato.citnbt.resources.conditions.Condition;
import com.sabrepotato.citnbt.resources.conditions.ItemstackCondition;
import com.sabrepotato.citnbt.resources.conditions.NBTCondition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ItemRule {
    public final List<NBTCondition> conditions;
    public final ItemstackCondition stackCondition;
    public final Condition condition;
    public final ModelResourceLocation location;

    public ItemRule(List<NBTCondition> conditions, ModelResourceLocation location, ItemstackCondition itemstackCondition) {
        this.conditions = conditions;
        this.location = location;
        this.stackCondition = itemstackCondition;
        this.condition = null;
    }

    public ItemRule(List<NBTCondition> conditions, ModelResourceLocation location, ItemstackCondition itemstackCondition, Condition overrideCondition) {
        this.conditions = conditions;
        this.location = location;
        this.stackCondition = itemstackCondition;
        this.condition = overrideCondition;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemRule other)) return false;
        return Objects.equals(conditions, other.conditions);
    }

    public boolean matches(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase player) {
        //TODO: Add nullability to each type of condition to allow skipping unused conditions
        NBTTagCompound compound = stack.getTagCompound();
        if (condition != null) {
            if (!condition.check(stack, worldIn, player)) return false;
        }
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
