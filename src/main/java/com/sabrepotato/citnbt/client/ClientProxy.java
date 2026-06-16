package com.sabrepotato.citnbt.client;

import com.sabrepotato.citnbt.resources.ExternalResourcePack;
import com.sabrepotato.citnbt.util.IProxy;
import com.sabrepotato.citnbt.util.ModLoaded;

@SuppressWarnings("unused")
public class ClientProxy implements IProxy {

    public void init() {
//        Minecraft mc = Minecraft.getMinecraft();
//        if (mc.getResourceManager() instanceof IReloadableResourceManager) {
//            ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(new PropertiesPackLoader());
//        }
    }

	@Override
	public void preInit() {
		if (!ModLoaded.RESOURCE_LOADER_LOADED) {
			ExternalResourcePack.ensurePackMcmetaExists();
			ExternalResourcePack.injectExternalResources();
		}
	}
}
