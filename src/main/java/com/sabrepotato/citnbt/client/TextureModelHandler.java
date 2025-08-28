package com.sabrepotato.citnbt.client;

import com.google.common.collect.ImmutableMap;
import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.config.FileNBTLoader;
import com.sabrepotato.citnbt.config.NBTHolder;
import com.sabrepotato.citnbt.resources.FakeResourcePack;
import com.sabrepotato.citnbt.resources.ItemRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
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
    private static FakeResourcePack modelPack;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        FileNBTLoader.loadFiles();
        for(NBTHolder rule : FileNBTLoader.ITEM_RULES) {
//            CITNBT.LOGGER.info("Loading rule: {} for {}", rule.texture, rule.getRule().getLocation());
            if (rule.texture != null ) {
                event.getMap().registerSprite(rule.texture);
            } else if (rule.getTextureSet() != null) {
                for (String item: rule.getTextureSet().values()) {
                    event.getMap().registerSprite(new ResourceLocation(item));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (modelPack == null) {
            modelPack = new FakeResourcePack("citnbt");
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getResourceManager() instanceof SimpleReloadableResourceManager sm) {
                sm.reloadResourcePack(modelPack);
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
                } else if (holder.isModelOverload()) {
                    Map<String, String> m = holder.getTextureSet();
                    byte[] bytes = MemoryFileBuilder.loadStages(targetModel, m);
                    ResourceLocation temp = new ResourceLocation("citnbt",
                            "item/" + targetModel.getPath());
                    MODEL_PACK.addModel(
                            new ResourceLocation("citnbt",
                                    "models/item/" + targetModel.getPath() + ".json"),
                            bytes
                    );
                    model = ModelLoaderRegistry.getModel(temp);
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
