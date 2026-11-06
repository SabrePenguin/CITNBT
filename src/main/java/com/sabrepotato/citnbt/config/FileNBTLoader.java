package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.parser.FileLoader;
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
                        .filter(path -> path.toString().endsWith(".cit.properties"))
                        .forEach(FileLoader::loadProperty);
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
}
