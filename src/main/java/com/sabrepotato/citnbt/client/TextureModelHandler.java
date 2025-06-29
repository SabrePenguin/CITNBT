package com.sabrepotato.citnbt.client;

import com.google.common.collect.ImmutableMap;
import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.config.NBTLoader;
import com.sabrepotato.citnbt.config.NBTHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
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

    public static final Map<NBTHolder, IBakedModel> BAKED_MODELS = new HashMap<>();
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for(NBTHolder rule : NBTLoader.RULES) {
            CITNBT.LOGGER.info("Loading rule: {} for {}", rule.texture, rule.itemId);
            event.getMap().registerSprite(rule.texture);
        }
    }


    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ModelResourceLocation, List<NBTHolder>> rulesByModel = new HashMap<>();
        for(NBTHolder rule : NBTLoader.RULES) {
            rulesByModel.computeIfAbsent(rule.itemId, k -> new ArrayList<>()).add(rule);
        }

        for (Map.Entry<ModelResourceLocation, List<NBTHolder>> entry : rulesByModel.entrySet()) {
            ModelResourceLocation modelLoc = entry.getKey();
            IBakedModel original = event.getModelRegistry().getObject(modelLoc);
            if (original == null) {
                CITNBT.LOGGER.error("Invalid target: {}", modelLoc);
                continue;
            }

            for (NBTHolder rule : entry.getValue()) {
                try {
                    IModel model;
                    model = ModelLoaderRegistry.getModel(rule.itemId);
                    model = model.retexture(ImmutableMap.of("layer0", rule.texture.toString()));

                    IBakedModel baked = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                            location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
                    BAKED_MODELS.put(rule, baked);
                } catch (Exception e) {
                    CITNBT.LOGGER.error("Error baking model for rule: {}", rule);
                }
            }
            event.getModelRegistry().putObject(modelLoc,
                    new DynamicBakedModel(original, entry.getValue()));
        }
    }
}
