package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.resources.conditions.ItemstackCondition;
import com.sabrepotato.citnbt.resources.conditions.NBTCondition;
import com.sabrepotato.citnbt.resources.ItemRule;
import com.sabrepotato.citnbt.resources.conditions.Range;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileNBTLoader {

    public static List<NBTHolder> CONFIG_RULES = new ArrayList<>();

    public static void loadFiles() {
        File resourceDir = new File(Minecraft.getMinecraft().gameDir, "resources");

        if (resourceDir.exists() && resourceDir.isDirectory()) {
            try (Stream<Path> stream = Files.walk(resourceDir.toPath())){
                stream.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".properties"))
                        .forEach(path -> {
                            CITNBT.LOGGER.info("Loaded file {}", path.toString());
                            try (InputStream in = Files.newInputStream(path)) {
                                Properties props = new Properties();
                                props.load(in);
                                List<String> items = Arrays.asList(props.getProperty("items").split(" "));
                                String damage = props.getProperty("damage");
                                String stackSize = props.getProperty("stackSize");
                                String hand = props.getProperty("hand");
                                String enchantments = props.getProperty("enchantments", props.getProperty("enchantmentIDs"));
                                String enchantLevels= props.getProperty("enchantmentLevels");
                                ItemstackCondition stack = new ItemstackCondition();
                                if (damage != null) {
                                    addDamageRule(stack, damage);
                                }
                                if (stackSize != null) {
                                    addStackRule(stack, stackSize);
                                }
                                if (hand != null) {
                                    stack.addHand(hand);
                                }
                                if (enchantments != null) {
                                    splitEnchants(stack, enchantments);
                                }
                                if (enchantLevels != null) {
                                    addEnchantRange(stack, enchantLevels);
                                }
                                String texture = props.getProperty("texture");
                                String model = props.getProperty("model");
                                if (items.isEmpty() || (texture == null && model == null)) return;
                                List<ModelResourceLocation> itemLocs = items.stream().map(item -> new ModelResourceLocation(item, "inventory")).collect(Collectors.toList());
                                ResourceLocation textureLoc = (texture != null) ? new ResourceLocation(texture): null;
                                ResourceLocation modelLoc = (model != null) ? new ResourceLocation(model) : null;

                                List<NBTCondition> rules = new ArrayList<>();
                                for (String key : props.stringPropertyNames()) {
                                    if (key.startsWith("nbt.")) {
                                        String nbtPath = key.substring(4);
                                        String val = props.getProperty(key);
                                        if (val.startsWith("contains:")) {
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.CONTAINS, val.substring(9)));
                                        } else if (val.startsWith("regex:")) {
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.REGEX, val.substring(6)));
                                        } else if (val.startsWith("icontains:")) {
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.ICONTAINS, val.substring(10)));
                                        } else if (val.startsWith("iregex:")) {
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.IREGEX, val.substring(7)));
                                        } else if (val.startsWith("exists:")) {
                                            String bool = val.substring(7);
                                            if (bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("false")) {
                                                rules.add(new NBTCondition(nbtPath, NBTCondition.Type.EXISTS, val.substring(7)));
                                            } else {
                                                CITNBT.LOGGER.warn("Unable to apply exists rule to {} on path {}: " +
                                                        "Invalid value: {}", itemLocs, nbtPath, bool);
                                            }
                                        } else if (val.startsWith("range:")) {
                                            List<String> range = Arrays.asList(val.substring(6).split(" "));
                                            List<Range> ranges = new ArrayList<>();
                                            range.forEach(subRange -> {
                                                ranges.add(Range.parse(subRange, 0, 65535));
                                            });
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.RANGE, ranges));
                                        } else if (val.startsWith("raw:")) {
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.RAW, val.substring(4)));
                                        } else {
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.EQUALS, val));
                                        }
                                    }
                                }
                                itemLocs.forEach(itemLoc -> {
                                    ItemRule rule = new ItemRule(rules, itemLoc, stack);
                                    CONFIG_RULES.add(new NBTHolder(textureLoc, modelLoc, rule));
                                });
                            } catch (IOException e) {
                                CITNBT.LOGGER.error("Unable to read file {}", path);
                            } catch (NullPointerException e) {
                                CITNBT.LOGGER.error("Could not create an array, you might be missing an items property");
                            }
                        });
            } catch (Exception e) {
                CITNBT.LOGGER.error("Unable to read directory {}", resourceDir.toPath());
            }
        }
    }

    //TODO: Move these to ItemstackCondition
    public static void addDamageRule(ItemstackCondition condition, String damageRange) {
        if(damageRange.startsWith("range:")) {
            List<String> range = Arrays.asList(damageRange.substring(6).split(" "));
            range.forEach(subRange -> {
                condition.addDamageRange(Range.parse(subRange, 0, 65535));
            });
        } else {
            try {
                condition.addDamageRange(Range.parse(damageRange, 0, 65535));
            } catch (NumberFormatException e){
                CITNBT.LOGGER.error("Not a valid integer: {}", damageRange);
            }
        }
    }

    public static void addStackRule(ItemstackCondition condition, String stackRange) {
        if(stackRange.startsWith("range:")) {
            List<String> range = Arrays.asList(stackRange.substring(6).split(" "));
            range.forEach(subRange -> {
                condition.addStackRange(Range.parse(subRange, 0, 65535));
            });
        } else {
            try {
                condition.addStackRange(Range.parse(stackRange, 0, 65535));
            } catch (NumberFormatException e){
                CITNBT.LOGGER.error("Not a valid integer: {}", stackRange);
            }
        }
    }

    private static void splitEnchants(ItemstackCondition condition, String enchantments) {
        List<String> splitString = Arrays.asList(enchantments.split(" "));
        splitString.forEach(e -> {
            ResourceLocation loc = e.contains(":") ? new ResourceLocation(e) : new ResourceLocation("minecraft", e);
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(loc);
            if (enchantment != null) condition.addEnchantment(enchantment);
            else CITNBT.LOGGER.error("Not a valid registry: {}", loc);
        });
    }

    private static void addEnchantRange(ItemstackCondition condition, String levels) {
        List<String> splitString = Arrays.asList(levels.split(" "));
        splitString.forEach(subrange -> {
            splitString.forEach(subRange ->
                condition.addLevel(Range.parse(subrange, 0, 65535))
            );
        });
    }

    public static void clearRules() {
        CONFIG_RULES.clear();
    }
}
