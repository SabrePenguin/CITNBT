package com.sabrepotato.citnbt.resources.conditions;

import com.sabrepotato.citnbt.config.CITNBTConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemstackCondition {
    private List<Range> damageRange;
    private List<Range> stackRange;
    private List<Enchantment> enchantments;
    private List<Range> levelRange;
    private EnumHand hand;

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

    public void addEnchantment(Enchantment enchantment) {
        if (enchantments == null) {
            enchantments = new ArrayList<>();
        }
        enchantments.add(enchantment);
    }

    public void addLevel(Range level) {
        if (levelRange == null) {
            levelRange = new ArrayList<>();
        }
        levelRange.add(level);
    }

    public void addHand(String hand) {
        if (hand.equalsIgnoreCase("any")) {
            this.hand = null;
        } else if (hand.equalsIgnoreCase("off")) {
            this.hand = EnumHand.OFF_HAND;
        } else if (hand.equalsIgnoreCase("main")) {
            this.hand = EnumHand.MAIN_HAND;
        }
    }

    public boolean checkConditions(ItemStack itemStack, @Nullable EntityLivingBase entity) {
        if (damageRange != null) {
            int damage = itemStack.getItemDamage();
            boolean result = damageRange.stream().anyMatch(range -> range.contains(damage));
            if (!result) return false;
        }
        if (stackRange != null) {
            int count = itemStack.getCount();
            boolean result = stackRange.stream().anyMatch(range -> range.contains(count));
            if(!result) return false;
        }
        if (enchantments != null) {
            if (levelRange != null) {
                if(CITNBTConfig.compat.matchOneEnchAndLevel) {
                    if (enchantments.stream().noneMatch(ench ->
                            levelRange.stream().anyMatch(range -> range.contains(EnchantmentHelper.getEnchantmentLevel(ench, itemStack)))
                    )) return false;
                } else {
                    if (!enchantments.stream().allMatch(ench ->
                            levelRange.stream().anyMatch(range -> range.contains(EnchantmentHelper.getEnchantmentLevel(ench, itemStack)))
                    )) return false;
                }
            } else {
                if(CITNBTConfig.compat.matchOneEnchAndLevel) {
                    if (enchantments.stream().noneMatch(ench ->
                            EnchantmentHelper.getEnchantmentLevel(ench, itemStack) != 0
                    )) return false;
                } else {
                    if (!enchantments.stream().allMatch(ench ->
                            EnchantmentHelper.getEnchantmentLevel(ench, itemStack) != 0
                    )) return false;
                }
            }
        }
        if (hand != null && entity != null) {
            switch (this.hand) {
                case OFF_HAND -> {
                    if(itemStack != entity.getHeldItemOffhand()) return false;
                }
                case MAIN_HAND -> {
                    if (CITNBTConfig.compat.differentModelInInventoryFromHand) {
                        if (itemStack == entity.getHeldItemOffhand()) return false;
                    } else {
                        if(itemStack != entity.getHeldItemMainhand()) return false;
                    }
                }
            }
        }
        return true;
    }
}