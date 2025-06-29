package com.sabrepotato.citnbt;

import com.sabrepotato.citnbt.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID, name = Tags.MODNAME, version = Tags.VERSION)
public class CITNBT {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODNAME);

    @SidedProxy(
            modId = Tags.MODID,
            clientSide = "com.sabrepotato.citnbt.proxy.ClientProxy",
            serverSide = "com.sabrepotato.citnbt.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
