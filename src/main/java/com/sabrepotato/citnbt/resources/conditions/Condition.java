package com.sabrepotato.citnbt.resources.conditions;

import com.sabrepotato.citnbt.resources.ItemRule;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Condition other)) return false;
        return Objects.equals(override.getLocation(), other.override.getLocation()) &&
                Objects.equals(override.mapResourceValues, other.override.mapResourceValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(override.getLocation(), override.mapResourceValues);
    }
}
