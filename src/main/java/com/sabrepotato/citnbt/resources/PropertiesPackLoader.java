package com.sabrepotato.citnbt.resources;

import com.sabrepotato.citnbt.CITNBT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;

import java.io.File;

public class PropertiesPackLoader implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        Minecraft mc = Minecraft.getMinecraft();

        for (ResourcePackRepository.Entry entry : mc.getResourcePackRepository().getRepositoryEntries()) {
            IResourcePack pack = entry.getResourcePack();

            if (pack instanceof AbstractResourcePack) {
                File packFile = ((AbstractResourcePack) pack).resourcePackFile;
                if (packFile.isDirectory()) {
                    CITNBT.LOGGER.warn("Is a directory: {}", packFile);
                } else if (packFile.getName().endsWith(".zip")) {
                    CITNBT.LOGGER.warn("Is a zip file: {}", packFile.getName());
                }
            }
        }
    }


}
