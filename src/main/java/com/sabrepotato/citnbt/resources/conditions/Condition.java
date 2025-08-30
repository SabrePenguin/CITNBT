package com.sabrepotato.citnbt.resources.conditions;

import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class Condition {
    private final ItemOverride override;

    public Condition() {
        this.override = null;
    }

    public Condition(ItemOverride override) {
        this.override = override;
    }

    public boolean check(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase livingEntity) {
        Item item = stack.getItem();
        if (!item.hasCustomProperties() || override == null) return false;
        return override.matchesItemStack(stack, worldIn, livingEntity);
    }
}
