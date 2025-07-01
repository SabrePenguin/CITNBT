package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.resources.NBTCondition;
import com.sabrepotato.citnbt.resources.NBTRule;
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
                                String texture = props.getProperty("texture");
                                String model = props.getProperty("model");
                                if (items.isEmpty() || texture == null) return;
                                List<ModelResourceLocation> itemLocs = items.stream().map(item -> new ModelResourceLocation(item, "inventory")).collect(Collectors.toList());
                                ResourceLocation textureLoc = new ResourceLocation(texture);
                                ResourceLocation modelLoc = (model != null) ? new ResourceLocation(model) : null;

                                List<NBTCondition> rules = new ArrayList<>();
                                for (String key : props.stringPropertyNames()) {
                                    if (key.startsWith("nbt.")) {
                                        String nbtPath = key.substring(4);
                                        String val = props.getProperty(key);
                                        if (val.startsWith("contains:")) {
                                            // TODO: Insert ot Rule
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.CONTAINS, val.substring(9)));
                                        } else if (val.equals("exists")) {
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.EXISTS, "exists"));
                                        } else {
                                            rules.add(new NBTCondition(nbtPath, NBTCondition.Type.EQUALS, val));
                                        }
                                    }
                                }
                                itemLocs.forEach(itemLoc -> {
                                    NBTRule rule = new NBTRule(rules, itemLoc);
                                    CONFIG_RULES.add(new NBTHolder(textureLoc, modelLoc, rule));
                                });
                            } catch (IOException e) {
                                CITNBT.LOGGER.error("Unable to read file {}", path);
                            }
                        });
            } catch (Exception e) {
                CITNBT.LOGGER.error("Unable to read directory {}", resourceDir.toPath());
            }
        }
    }

    public static void clearRules() {
        CONFIG_RULES.clear();
    }
}
