package com.sabrepotato.citnbt.proxy;

import com.sabrepotato.citnbt.resources.ExternalResourcePack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@SuppressWarnings("unused")
public class ClientProxy implements IProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ExternalResourcePack.ensurePackMcmetaExists();
        ExternalResourcePack.injectExternalResources();
    }
}
