package com.sabrepotato.citnbt.client;

import com.google.common.collect.ImmutableMap;
import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.config.FileNBTLoader;
import com.sabrepotato.citnbt.config.NBTHolder;
import com.sabrepotato.citnbt.resources.ItemRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber
public class TextureModelHandler {

    public static final Map<ItemRule, IBakedModel> BAKED_MODELS = new HashMap<>();
    private static final Map<ModelResourceLocation, List<ItemRule>> RULES_BY_MODEL = new HashMap<>();
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        FileNBTLoader.loadFiles();
        for(NBTHolder rule : FileNBTLoader.ITEM_RULES) {
//            CITNBT.LOGGER.info("Loading rule: {} for {}", rule.texture, rule.getRule().getLocation());
            if (rule.texture != null ) {
                event.getMap().registerSprite(rule.texture);
            }
        }
    }


    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        FileNBTLoader.clearRules();
        FileNBTLoader.loadFiles();
        BAKED_MODELS.clear();
        RULES_BY_MODEL.clear();
        for (NBTHolder holder : FileNBTLoader.ITEM_RULES) {
            try {
                ItemRule rule = holder.getRule();
                ModelResourceLocation targetModel = rule.getLocation();

                IModel model;
                if (holder.getModel() != null) {
                    model = ModelLoaderRegistry.getModel(holder.getModel());
                } else if (holder.getTexture() != null) {
                    model = ModelLoaderRegistry.getModel(targetModel).retexture(
                            ImmutableMap.of("layer0", holder.getTexture().toString()));
                } else {
                    model = ModelLoaderRegistry.getModel(targetModel);
                }
                IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                        location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));

                BAKED_MODELS.put(rule, bakedModel);
                RULES_BY_MODEL.computeIfAbsent(targetModel, k -> new ArrayList<>()).add(rule);
            } catch (Exception e) {
                CITNBT.LOGGER.error("Error baking model for rule: {}", holder.getRule());
            }
        }

        for (Map.Entry<ModelResourceLocation, List<ItemRule>> entry : RULES_BY_MODEL.entrySet()) {
            ModelResourceLocation modelLoc = entry.getKey();
            List<ItemRule> rules = entry.getValue();

            IBakedModel original = event.getModelRegistry().getObject(modelLoc);
            if (original != null) {
                IBakedModel wrapped = new DynamicBakedModel(original, rules);
                event.getModelRegistry().putObject(modelLoc, wrapped);
            } else {
                CITNBT.LOGGER.error("Invalid target: {}", modelLoc);
            }
        }
    }
}
