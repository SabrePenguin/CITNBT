package com.sabrepotato.citnbt.config;

import com.sabrepotato.citnbt.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@Config(modid = Tags.MODID)
public class CITNBTConfig {

    @Config.Name("Optifine Compatibility")
    public static OptifineCompat compat = new OptifineCompat(true, true);

    public static class OptifineCompat {
        @Config.Name("Enable Optifine Hand")
        @Config.Comment("Allows different models in the inventory, splitting off from Optifine's handling.")
        public boolean differentModelInInventoryFromHand;
        @Config.Name("Enable Optifine Ench Behavior")
        @Config.Comment("Ensures that optifine behavior of matching one enchantment and level is followed")
        public boolean matchOneEnchAndLevel = true;

        public OptifineCompat(final boolean dmiifh, final boolean moeal) {
            this.differentModelInInventoryFromHand = dmiifh;
            this.matchOneEnchAndLevel = moeal;
        }
    }

    @Mod.EventBusSubscriber(modid = Tags.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MODID)) {
                ConfigManager.sync(Tags.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
