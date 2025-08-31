package com.sabrepotato.citnbt.client;

import com.google.common.collect.ImmutableMap;
import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.config.FileNBTLoader;
import com.sabrepotato.citnbt.config.NBTHolder;
import com.sabrepotato.citnbt.resources.ItemRule;
import com.sabrepotato.citnbt.resources.conditions.Condition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.*;


@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber
public class TextureModelHandler {

    public static final Map<ItemRule, IBakedModel> BAKED_MODELS = new HashMap<>();
    private static final Map<ModelResourceLocation, List<ItemRule>> RULES_BY_MODEL = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        FileNBTLoader.loadFiles();
        for(NBTHolder rule : FileNBTLoader.ITEM_RULES) {
            if (rule.texture != null ) {
                event.getMap().registerSprite(rule.texture);
            } else if (rule.getTextureSet() != null) {
                for (String item: rule.getTextureSet().values()) {
                    ResourceLocation layer = new ResourceLocation(item);
                    event.getMap().registerSprite(layer);
                }
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

                IModel model = null;
                if (holder.getModel() != null) {
                    model = ModelLoaderRegistry.getModel(holder.getModel());
                } else if (holder.getTexture() != null) {
                    model = ModelLoaderRegistry.getModel(targetModel).retexture(
                            ImmutableMap.of("layer0", holder.getTexture().toString()));
                } else if (holder.isModelOverload()) {
                    Map<String, String> overloadMap = holder.getTextureSet();
                    // TODO: Remove used textures from map, report remaining as missing
                    IModel iModel = ModelLoaderRegistry.getModel(targetModel);
                    Optional<ModelBlock> modelBlock = iModel.asVanillaModel();
                    if (modelBlock.isPresent()) {
                        List<ItemOverride> list = modelBlock.get().getOverrides();
                        for(int i = list.size()-1; i >= 0; i--) {
                            ItemOverride override = list.get(i);
                            String location = override.getLocation().toString().replaceAll("^(.*:)?item(?=/)", "$1items");
                            if (!overloadMap.containsKey(location)) {
                                continue;
                            }
                            IModel newModel = ModelLoaderRegistry.getModel(override.getLocation())
                                    .retexture(ImmutableMap.of("layer0", overloadMap.get(location))).process(ImmutableMap.of());
                            ItemRule newRule = new ItemRule(holder.getRule(), new Condition(override));
                            IBakedModel bakedModel = newModel.bake(newModel.getDefaultState(), DefaultVertexFormats.ITEM,
                                    loc ->  Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString()));
                            BAKED_MODELS.put(newRule, bakedModel);
                            RULES_BY_MODEL.computeIfAbsent(targetModel, k -> new ArrayList<>()).add(newRule);
                        }
                        Map<String, String> textures = modelBlock.get().textures;
                        for(Map.Entry<String, String> e : textures.entrySet()) {
                            String texture = e.getValue();
                            if (!texture.contains(":")) {
                                texture = "minecraft:" + texture;
                            }
                            if (overloadMap.containsKey(texture)) {
                                if (model == null)
                                    model = ModelLoaderRegistry.getModel(targetModel);
                                model = model.retexture(ImmutableMap.of(e.getKey(), overloadMap.get(texture)));
                            }
                        }
                    }
                    if (model == null) continue;
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
