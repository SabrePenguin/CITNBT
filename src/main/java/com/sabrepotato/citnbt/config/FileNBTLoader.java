package com.sabrepotato.citnbt.config;

import com.mojang.realmsclient.util.Pair;
import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.parser.FileLoader;
import com.sabrepotato.citnbt.resources.conditions.ItemstackCondition;
import com.sabrepotato.citnbt.resources.conditions.NBTCondition;
import com.sabrepotato.citnbt.resources.conditions.RangeSet;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class FileNBTLoader {

    public static void loadFiles() {
        loadFiles("resources");
    }

    private static void loadFiles(String directory) {
        File resourceDir = new File(Minecraft.getMinecraft().gameDir, directory);

        if (resourceDir.exists() && resourceDir.isDirectory()) {
            Path resource = resourceDir.toPath();
            Path citResource = resource.resolve("optifine/cit");
            try (Stream<Path> stream = Files.walk(citResource)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".properties"))
                        .forEach(FileLoader::loadProperty)
                ;
            } catch (Exception e) {

            }
            try (Stream<Path> stream = Files.walk(resource)){
                stream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            if (path.toString().endsWith(".cit.properties")) {
                                FileLoader.loadProperty(path);
                            }
                });
            } catch (Exception e) {
                CITNBT.LOGGER.error("Unable to read directory {}", resource);
            }

        }
    }

    private static void loadProperty(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String method = properties.getProperty("method", "average"); // TODO
            String cap = properties.getProperty("cap"); // TODO
            String fade = properties.getProperty("fade", "0.5"); // TODO
            String useGlint = properties.getProperty("useGlint", "true"); // TODO
            String type = properties.getProperty("type", "item");
            String items = properties.getProperty("items");
            List<String> itemList = Arrays.asList(properties.getProperty("items").split(" "));
            String texture = properties.getProperty("texture");
            Map<String, String> textures = new HashMap<>();
            String model = properties.getProperty("model");
            String damage = properties.getProperty("damage");
            String damageMask = properties.getProperty("damageMask");
            String stackSize = properties.getProperty("stackSize");
            String enchantments = properties.getProperty("enchantments", properties.getProperty("enchantmentIDs"));
            String enchantmentLevels = properties.getProperty("enchantmentLevels");
            String hand = properties.getProperty("hand", "any");
            String nbt = properties.getProperty("nbt");
            String weight = properties.getProperty("weight", "0");
            int fileWeight = tryParseInt(weight, 0);
            ItemstackCondition itemstack = FileNBTLoader.setItemStackCondition(damage, stackSize, hand, enchantments, enchantmentLevels, damageMask);
            List<NBTCondition> rules = new ArrayList<>();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("nbt.")) {
                    String nbtPath = key.substring(4);
                    String val = properties.getProperty(key);
                    rules.add(setNbtCondition(nbtPath, val));
                } else if (key.startsWith("nbt")) {
                    rules.add(setNbtCondition("", nbt));
                } else if (key.startsWith("texture.")) {
                    String subkey = key.substring(8);
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
                        CITNBT.LOGGER.error("Invalid key, should follow texture[.<namespace>].<stage>: {}", subkey);
                        continue;
                    }
                    String newKey = namespace + ":items/" + item;
                    textures.put(newKey, properties.getProperty(key));
                } else if (key.startsWith("model.")) {
                    String subkey = key.substring(6);
                    String[] split = subkey.split("\\.");
                    String namespace;
                    String item;
                    if (split.length == 1){
                        namespace = "minecraft";
                        item = split[0];
                    } else if (split.length == 2) {
                        namespace = split[0];
                        item = split[1];
                    } else {
                        CITNBT.LOGGER.error("Invalid key, should follow model[.<namespace>].<stage>: {}", subkey);
                        continue;
                    }
                    String newKey = namespace + ":item/" + item;
                    textures.put(newKey, properties.getProperty(key));
                }
            }
            if (type.equals("item")) {
            } else if (type.equals("enchantment")) {
                String blend = properties.getProperty("blend", "add");
                String speed = properties.getProperty("speed", "1");
                String rotation = properties.getProperty("rotation");
                String layer = properties.getProperty("layer", "0");
                String duration = properties.getProperty("duration", "0");
            } else if (type.equals("armor")) {
                //The only one that doesn't use texture
            } else if (type.equals("elytra")) {

            }
        } catch (IOException e) {
            CITNBT.LOGGER.error("Unable to read file {}", path);
        }
    }

    private static ItemstackCondition setItemStackCondition(
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
            RangeSet ranges = new RangeSet();
            range.forEach(subRange -> {
                Pair<Integer, Integer> pair = RangeSet.parse(subRange, 0, 65535);
                ranges.add(pair.first(), pair.second());
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
