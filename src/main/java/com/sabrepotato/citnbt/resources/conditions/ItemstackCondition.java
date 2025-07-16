package com.sabrepotato.citnbt.resources.conditions;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemstackCondition {
    private List<Range> damageRange;
    private List<Range> stackRange;

    public void addDamageRange(Range range) {
        if (damageRange == null) {
            this.damageRange = new ArrayList<>();
        }
        this.damageRange.add(range);
    }

    public void addStackRange(Range range) {
        if (stackRange == null) {
            this.stackRange = new ArrayList<>();
        }
        this.stackRange.add(range);
    }

    public boolean checkConditions(ItemStack itemStack) {
        if (damageRange != null) {
            int damage = itemStack.getItemDamage();
            boolean result = damageRange.stream().anyMatch(range -> range.contains(damage));
            if (result) return true;
        }
        if (stackRange != null) {
            int count = itemStack.getCount();
            boolean result = stackRange.stream().anyMatch(range -> range.contains(count));
            if(result) return true;
        }
        return false;
    }
}