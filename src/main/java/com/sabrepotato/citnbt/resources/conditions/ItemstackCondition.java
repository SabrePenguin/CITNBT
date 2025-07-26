package com.sabrepotato.citnbt.resources.conditions;

import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.config.CITNBTConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemstackCondition {
    private List<Range> damageRange;
    private Integer damageMask;
    private Float percentageLow;
    private Float percentageHigh;
    private List<Range> stackRange;
    //TODO: Make a better enchantment format (minecraft:flame:3)
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

    public void setDamageMask(int damageMask) {
        this.damageMask = damageMask;
    }

    public void setPercentage(int low, int high) {
        this.percentageLow = low / 100f;
        this.percentageHigh = high / 100f;
    }

    public void setPercentage(int percentage) {
        this.percentageLow = percentage / 100f;
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
            if (damageMask != null) {
                damage = damage & damageMask;
            }
            int finalDamage = damage;
            boolean result = damageRange.stream().anyMatch(range -> range.contains(finalDamage));
            if (!result) return false;
        }
        if (percentageLow != null) {
            int maxDamage = itemStack.getMaxDamage();
            int currentDamage = itemStack.getItemDamage();
            if (percentageHigh != null) {
                if (Math.round(percentageLow * maxDamage) > currentDamage || currentDamage > Math.round(percentageHigh * maxDamage)) return false;
            } else {
                if (Math.round(percentageLow * maxDamage) != currentDamage) return false;
            }
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
        if (levelRange != null && enchantments == null) {
            if (EnchantmentHelper.getEnchantments(itemStack).values().stream().noneMatch(level ->
                    levelRange.stream().anyMatch(range -> range.contains(level))
            )) return false;
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

    public void addDamageRule(String damageRange) {
        if (damageRange.startsWith("range:")) {
            List<String> range = Arrays.asList(damageRange.substring(6).split(" "));
            range.forEach(subRange -> {
                this.addDamageRange(Range.parse(subRange, 0, 65535));
            });
        } else if (damageRange.contains("%")) {
            List<String> percentRange = Arrays.asList(damageRange.split(" "));
            if (percentRange.size() == 1) {
                this.setPercentage(Integer.parseInt(percentRange.get(0).replace("%", "")));
            } else if (percentRange.size() == 2) {
                int low = Integer.parseInt(percentRange.get(0).replace("%", ""));
                int high = Integer.parseInt(percentRange.get(1).replace("%", ""));
                if (low > high) {
                    int temp = low;
                    low = high;
                    high = temp;
                }
                this.setPercentage(low, high);
            } else {
                CITNBT.LOGGER.error("Invalid count of percentages. Only 1 or 2 allowed: {}", damageRange);
            }
        } else {
            try {
                this.addDamageRange(Range.parse(damageRange, 0, 65535));
            } catch (NumberFormatException e){
                CITNBT.LOGGER.error("Not a valid integer: {}", damageRange);
            }
        }
    }

    public void addDamageMask(String damageMask) {
        try {
            this.setDamageMask(Integer.parseInt(damageMask));
        } catch (NumberFormatException e) {
            CITNBT.LOGGER.error("Not a valid integer: {}", damageMask);
        }
    }

    public void addStackRule(String stackRange) {
        if(stackRange.startsWith("range:")) {
            List<String> range = Arrays.asList(stackRange.substring(6).split(" "));
            range.forEach(subRange -> {
                this.addStackRange(Range.parse(subRange, 0, 65535));
            });
        } else {
            try {
                this.addStackRange(Range.parse(stackRange, 0, 65535));
            } catch (NumberFormatException e){
                CITNBT.LOGGER.error("Not a valid integer: {}", stackRange);
            }
        }
    }

    public void addEnchantments(String enchantments) {
        List<String> splitString = Arrays.asList(enchantments.split(" "));
        splitString.forEach(e -> {
            ResourceLocation loc = e.contains(":") ? new ResourceLocation(e) : new ResourceLocation("minecraft", e);
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(loc);
            if (enchantment != null) this.addEnchantment(enchantment);
            else CITNBT.LOGGER.error("Not a valid registry: {}", loc);
        });
    }

    public void addEnchantRange(String levels) {
        List<String> splitString = Arrays.asList(levels.split(" "));
        splitString.forEach(subrange -> {
            splitString.forEach(subRange ->
                    this.addLevel(Range.parse(subrange, 0, 65535))
            );
        });
    }
}