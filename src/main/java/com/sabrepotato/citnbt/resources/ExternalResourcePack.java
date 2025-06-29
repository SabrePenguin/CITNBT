package com.sabrepotato.citnbt.resources;

import com.sabrepotato.citnbt.CITNBT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ExternalResourcePack {
    private static final File RESOURCE_DIR = new File(Minecraft.getMinecraft().gameDir, "resources");
    private static final File PACK_META = new File(RESOURCE_DIR, "pack.mcmeta");

    public static void ensurePackMcmetaExists() {
        if (!PACK_META.exists()) {
            try {
                String json = "{ \"pack\": { \"pack_format\": 3, \"description\": \"Auto CIT Resource\" } }";
                Files.write(PACK_META.toPath(), json.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                CITNBT.LOGGER.error("Unable to generate mcmeta: {}. " +
                        "This mod will not work, as it cannot force loading a resource pack. " +
                        "This is not a critical mod, so this will not force a stop.", e.getLocalizedMessage());
            }
        }
    }

    public static void injectExternalResources() {
        File resourceDir = new File(Minecraft.getMinecraft().gameDir, "resources");
        IResourcePack flatPack = new FlattenedResourcePack(resourceDir);

        Minecraft mc = Minecraft.getMinecraft();
        try {
            List<IResourcePack> defaultPacks = ObfuscationReflectionHelper.getPrivateValue(
                    Minecraft.class, mc, "defaultResourcePacks"
            );

            if (!defaultPacks.contains(flatPack)) {
                defaultPacks.add(flatPack);
            }
            CITNBT.LOGGER.info("Loaded FlatResourcePack: ./resources");
            List<IResourcePack> allPacks = new ArrayList<>(defaultPacks);
            IResourcePack mcPack = mc.defaultResourcePack;
            if (!allPacks.contains(mcPack)) {
                allPacks.add(mcPack);
            }
            IResourceManager rm = mc.getResourceManager();
            if (rm instanceof SimpleReloadableResourceManager) {
                ((SimpleReloadableResourceManager) rm).reloadResources(allPacks);
            }
        } catch (Exception e) {
            CITNBT.LOGGER.error("Unable to load resources: {}", e.getLocalizedMessage());
        }
    }
}
