package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.resources.conditions.ItemstackCondition;
import com.sabrepotato.citnbt.resources.conditions.NBTCondition;
import com.sabrepotato.citnbt.resources.ItemRule;
import com.sabrepotato.citnbt.resources.conditions.Range;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

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
//                                List<String> damageRange = Arrays.asList(props.getProperty("damage", "").split(" "));
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
                                        } else if (val.startsWith("icontains:")) {
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.ICONTAINS, val.substring(10)));
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

    public static void clearRules() {
        CONFIG_RULES.clear();
    }
}
