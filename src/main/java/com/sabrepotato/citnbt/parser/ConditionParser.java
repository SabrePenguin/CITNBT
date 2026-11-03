package com.sabrepotato.citnbt.parser;

import com.sabrepotato.citnbt.resources.conditions.ItemstackCondition;

public class ConditionParser {

    public static ItemstackCondition setItemStackCondition(
            String damage, String stackSize, String hand,
            String enchantments, String enchantmentLevels, String damageMask) {
        ItemstackCondition stack = new ItemstackCondition();
        if (damage != null) {
            stack.addDamageRule(damage);
        }
        if (stackSize != null) {
            stack.addStackRule(stackSize);
        }
        if (hand != null) {
            stack.addHand(hand);
        }
        if (enchantments != null) {
            stack.addEnchantments(enchantments);
        }
        if (enchantmentLevels != null) {
            stack.addEnchantRange(enchantmentLevels);
        }
        if (damageMask != null) {
            stack.addDamageMask(damageMask);
        }
        return stack;
    }
}
