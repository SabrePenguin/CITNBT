package com.sabrepotato.citnbt.proxy;

import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.resources.ExternalResourcePack;
import com.sabrepotato.citnbt.config.ITNBT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.List;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ExternalResourcePack.ensurePackMcmetaExists();
        ExternalResourcePack.injectExternalResources();
        ITNBT.loadFiles();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        if (manager instanceof FallbackResourceManager) {
            List<IResourcePack> packs = ObfuscationReflectionHelper.getPrivateValue(FallbackResourceManager.class, (FallbackResourceManager) manager, "resourcePacks");
            CITNBT.LOGGER.info("Availalbe packs: {}", packs);
        }
        Minecraft.getMinecraft().getResourceManager().getResourceDomains().forEach(domain -> CITNBT.LOGGER.info("Available domain: {}", domain));
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("cit:items/stick_glow");
        if (sprite == Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
            CITNBT.LOGGER.error("Missing sprite");
        }
    }
}
