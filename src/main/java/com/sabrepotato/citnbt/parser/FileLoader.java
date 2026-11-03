package com.sabrepotato.citnbt.parser;

import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.config.NBTHolder;
import com.sabrepotato.citnbt.resources.conditions.ItemstackCondition;
import com.sabrepotato.citnbt.resources.conditions.NBTCondition;
import com.sabrepotato.citnbt.resources.conditions.Range;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class FileLoader {

    public static List<NBTHolder> ITEM_RULES = new ArrayList<>();

    public static List<ItemHolder> ITEMS = new ArrayList<>();

    public static void loadProperty(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String type = properties.getProperty("type", "item");

            List<String> itemList = Arrays.asList(properties.getProperty("items").split(" "));

            String damage = properties.getProperty("damage");
            String stackSize = properties.getProperty("stackSize");
            String hand = properties.getProperty("hand", "any");
            String enchantments = properties.getProperty("enchantments", properties.getProperty("enchantmentIDs"));
            String enchantmentLevels = properties.getProperty("enchantmentLevels");
            String damageMask = properties.getProperty("damageMask");
            ItemstackCondition condition = ConditionParser.setItemStackCondition(
                    damage, stackSize, hand, enchantments, enchantmentLevels, damageMask);


            String texture = properties.getProperty("texture");
            Map<String, String> textures = new HashMap<>();
            String model = properties.getProperty("model");
            Map<String, String> models = new HashMap<>();

            String weight = properties.getProperty("weight", "0");
            int fileWeight = tryParseInt(weight, 0);

            String nbt = properties.getProperty("nbt");
            List<NBTCondition> rules = new ArrayList<>();

            for(String key: properties.stringPropertyNames()) {
                if (key.startsWith("nbt.")) {
                    String nbtPath = key.substring(4);
                    String val = properties.getProperty(key);
                    rules.add(setNbtCondition(nbtPath, val));
                } else if (key.startsWith("nbt")) {
                    rules.add(setNbtCondition("", nbt));
                }
                if (key.startsWith("texture.")) {
                    String newKey = addKey(key, "texture.");
                    if (newKey != null) {
                        textures.put(newKey, properties.getProperty(key));
                    }
                }
                if (key.startsWith("model.")) {
                    String newKey = addKey(key, "model.");
                    if (newKey != null) {
                        models.put(newKey, properties.getProperty(key));
                    }
                }
            }

            switch (type) {
                case "item" -> {
                    if (itemList.isEmpty()) return;
                    List<ResourceLocation> iList = itemList.stream().map(ResourceLocation::new).collect(Collectors.toList());
                    ItemHolder.ItemType itemType;
                    if (texture != null) itemType = new ItemHolder.Texture(texture);
                    else if (model != null) itemType = new ItemHolder.Model(model);
                    else if (!textures.isEmpty()) itemType = new ItemHolder.MultiTexture(textures);
                    else if (!models.isEmpty()) itemType = new ItemHolder.MultiModel(models);
                    else {
                        CITNBT.LOGGER.error("File {} is missing a texture or model", path);
                        return;
                    }
                    ITEMS.add(new ItemHolder(iList, rules, condition, itemType, fileWeight));
                }
                case "enchantment" -> {
                    String blend = properties.getProperty("blend", "add");
                    String speed = properties.getProperty("speed", "1");
                    String rotation = properties.getProperty("rotation");
                    String layer = properties.getProperty("layer", "0");
                    String duration = properties.getProperty("duration", "0");
                }
                case "armor" -> {
                    //The only one that doesn't use texture
                }
                case "elytra" -> {
                }
            }
        } catch (IOException e) {
            CITNBT.LOGGER.error("Unable to read file {}", path);
        }
    }

    private static String addKey(String key, String prefix) {
        String subkey = key.substring(prefix.length());
        String[] split = subkey.split("\\.");
        String namespace;
        String item;
        if (split.length == 1) {
            namespace = "minecraft";
            item = split[0];
        } else if (split.length == 2) {
            namespace = split[0];
            item = split[1];
        } else {
            CITNBT.LOGGER.error("Invalid key, should follow {}[<namespace>.]<stage>: {}", prefix, subkey);
            return null;
        }
        return namespace + ":items/" + item;
    }

    private static NBTCondition setNbtCondition(String nbtPath, String value) {
        if (value.startsWith("contains:")) {
            return new NBTCondition(nbtPath, NBTCondition.Type.CONTAINS, value.substring(9));
        } else if (value.startsWith("regex:")) {
            return new NBTCondition(nbtPath, NBTCondition.Type.REGEX, value.substring(6));
        } else if (value.startsWith("icontains:")) {
            return new NBTCondition(nbtPath, NBTCondition.Type.ICONTAINS, value.substring(10));
        } else if (value.startsWith("iregex:")) {
            return new NBTCondition(nbtPath, NBTCondition.Type.IREGEX, value.substring(7));
        } else if (value.startsWith("exists:")) {
            String bool = value.substring(7);
            if (bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("false")) {
                return new NBTCondition(nbtPath, NBTCondition.Type.EXISTS, value.substring(7));
            } else {
                CITNBT.LOGGER.warn("Unable to apply exists rule on path {}: " +
                        "Invalid value: {}", nbtPath, bool);
            }
        } else if (value.startsWith("range:")) {
            List<String> range = Arrays.asList(value.substring(6).split(" "));
            List<Range> ranges = new ArrayList<>();
            range.forEach(subRange -> {
                ranges.add(Range.parse(subRange, 0, 65535));
            });
            return new NBTCondition(nbtPath, NBTCondition.Type.RANGE, ranges);
        } else if (value.startsWith("raw:")) {
            return new NBTCondition(nbtPath, NBTCondition.Type.RAW, value.substring(4));
        }
        return new NBTCondition(nbtPath, NBTCondition.Type.EQUALS, value);
    }

    private static int tryParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
