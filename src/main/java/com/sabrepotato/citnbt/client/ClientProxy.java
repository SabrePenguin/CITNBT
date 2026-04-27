package com.sabrepotato.citnbt.client;

import com.sabrepotato.citnbt.resources.ExternalResourcePack;
import com.sabrepotato.citnbt.util.IProxy;

public class ClientProxy implements IProxy {

    public void init() {

    }

	@Override
	public void preInit() {
		ExternalResourcePack.ensurePackMcmetaExists();
		ExternalResourcePack.injectExternalResources();
	}
}
