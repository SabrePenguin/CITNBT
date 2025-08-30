package com.sabrepotato.citnbt.client;

import com.google.common.collect.ImmutableMap;
import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.config.FileNBTLoader;
import com.sabrepotato.citnbt.config.NBTHolder;
import com.sabrepotato.citnbt.resources.FakeResourcePack;
import com.sabrepotato.citnbt.resources.ItemRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

import static com.sabrepotato.citnbt.resources.ExternalResourcePack.MODEL_PACK;

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
                    event.getMap().registerSprite(new ResourceLocation(item));
                    CITNBT.LOGGER.info("Creating texture: {}", item);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getResourceManager() instanceof SimpleReloadableResourceManager sm) {
            sm.reloadResourcePack(MODEL_PACK);
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
                } else if (holder.isModelOverload()) {
//                    Map<String, String> m = holder.getTextureSet();
//                    byte[] bytes = MemoryFileBuilder.loadStages(targetModel, m);
//                    ResourceLocation temp = new ResourceLocation("citnbt",
//                            "item/" + targetModel.getPath());
//                    MODEL_PACK.addModel(
//                            new ResourceLocation("citnbt",
//                                    "models/item/" + targetModel.getPath() + ".json"),
//                            bytes
//                    );
                    // TODO: Find layer0, put it into model retexture.
                    IModel iModel = ModelLoaderRegistry.getModel(targetModel);
                    Optional<ModelBlock> modelBlock = iModel.asVanillaModel();
                    if (modelBlock.isPresent()) {
                        List<ItemOverride> list = modelBlock.get().getOverrides();
                        CITNBT.LOGGER.info("List: {}", list);
                        for(ItemOverride override: list) {

                        }
                        // We need to insert into rules manually here for all other
                    }
//                    for (Map.Entry<String, String> override: m.entrySet()) {
//                        byte[] sub_bytes = MemoryFileBuilder.cloneAndModify(override.getKey(), override.getValue());
//
//                        String new_location = override.getValue().replaceAll("^(.*:)?item(?=/)", "$1items");
//                        ResourceLocation helper = new ResourceLocation(new_location);
//                        MODEL_PACK.addModel(
//                                new ResourceLocation(
//                                        helper.getNamespace(),
//                                        "models/" + helper.getPath() + ".json"
//                                ),
//                                sub_bytes);
//                        ModelResourceLocation t = new ModelResourceLocation(new_location, "inventory"); // Correct
//                        IModel sub_model = ModelLoaderRegistry.getModel(helper);
//                        IBakedModel bakedModel = sub_model.bake(sub_model.getDefaultState(), DefaultVertexFormats.ITEM,
//                                location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
////                        if (event.getModelRegistry().getObject(t) == null) {
//                            event.getModelRegistry().putObject(t, bakedModel);
//                            CITNBT.LOGGER.info("Creating model for: {}", t.toString());
////                        }
//                    }
                    model = ModelLoaderRegistry.getModel(targetModel);
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
