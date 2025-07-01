package com.sabrepotato.citnbt.proxy;

import com.sabrepotato.citnbt.resources.ExternalResourcePack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ExternalResourcePack.ensurePackMcmetaExists();
        ExternalResourcePack.injectExternalResources();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}
