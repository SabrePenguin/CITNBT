package com.sabrepotato.citnbt.client;

import com.google.common.collect.ImmutableMap;
import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.Tags;
import com.sabrepotato.citnbt.config.FileNBTLoader;
import com.sabrepotato.citnbt.config.NBTHolder;
import com.sabrepotato.citnbt.parser.FileLoader;
import com.sabrepotato.citnbt.parser.ItemHolder;
import com.sabrepotato.citnbt.resources.ItemRule;
import com.sabrepotato.citnbt.resources.conditions.Condition;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IRegistryDelegate;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
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
        if (false) {
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
                } else if (holder.isTextureOverload()) {
                    {
                    Map<String, String> overloadMap = holder.getTextureSet();
                    // TODO: Remove used textures from map, report remaining as missing
                    // TODO: Check if initial size is same, try to swap to texture instead
                    IModel iModel = ModelLoaderRegistry.getModel(targetModel);
                    Optional<ModelBlock> modelBlock = iModel.asVanillaModel();
                    if (modelBlock.isPresent()) {
                        List<ItemOverride> list = modelBlock.get().getOverrides();
                        for(int i = list.size()-1; i >= 0; i--) {
                            ItemOverride override = list.get(i);
                            String textureLocation = override.getLocation().toString().replaceAll("^(.*:)?item(?=/)", "$1items");
                            String modelLocation = override.getLocation().toString();
                            if (!overloadMap.containsKey(textureLocation) && !overloadMap.containsKey(modelLocation)) {
                                continue;
                            }
                            boolean isModel = overloadMap.containsKey(modelLocation);
                            IModel newModel = isModel ? ModelLoaderRegistry.getModel(
                                    new ResourceLocation(overloadMap.get(modelLocation))) :
                                    ModelLoaderRegistry.getModel(override.getLocation())
                                            .retexture(ImmutableMap.of("layer0", overloadMap.get(textureLocation)));
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
                            String modelTexture = texture.replaceAll("^(.*:)?items(?=/)", "$1item");
                            if (overloadMap.containsKey(modelTexture)) {
                                model = ModelLoaderRegistry.getModel(new ResourceLocation(overloadMap.get(modelTexture)));
                            } else if (overloadMap.containsKey(texture)) {
                                if (model == null)
                                    model = ModelLoaderRegistry.getModel(targetModel);
                                model = model.retexture(ImmutableMap.of(e.getKey(), overloadMap.get(texture)));
                            }
                        }
                    }
                    if (model == null) continue;}
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
        }}

        bakeNewTypes(event);

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

    private static void bakeNewTypes(ModelBakeEvent event) {
        for (ItemHolder holder: FileLoader.ITEMS) {
            for (ResourceLocation location: holder.items()) {
                Item item = ForgeRegistries.ITEMS.getValue(location);
                if (item == null) continue;
                if (item.getHasSubtypes()) {
                    NonNullList<ItemStack> stacks = NonNullList.create();
                    for (CreativeTabs tab: item.getCreativeTabs()) {
                        item.getSubItems(tab, stacks);
                    }
                    IntList list = new IntArrayList(stacks.size());
                    for (ItemStack stack: stacks) {
                        list.add(stack.getItemDamage());
                    }
                    // Run this second, run ModelLoader.customModels check first, as that contains customModelDefinitions of modded items
                    // Run through the range
                    List<String> names = getVariantNames(event.getModelLoader(), item);
                    for (int i = 0; i < names.size(); i++) {
                        if (holder.itemstackCondition().checkDamageCondition(new ItemStack(item, 1, list.getInt(i)))) {
                            bake(new ResourceLocation(names.get(i)), holder);
                        }
                    }
                } else {
                    bake(Objects.requireNonNull(item.getRegistryName()), holder);
                }
            }
        }
    }

    private static void bake(ResourceLocation rl, ItemHolder item) {
        ModelResourceLocation mrl = ModelLoader.getInventoryVariant(rl.toString());
        if (item.itemType() instanceof ItemHolder.Texture texture) {
            try {
                IModel model = ModelLoaderRegistry.getModel(mrl).retexture(
                        ImmutableMap.of("layer0", texture.texture()));
                IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                        location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
                ItemRule rule = new ItemRule(item.nbtConditions(), mrl, item.itemstackCondition());
                BAKED_MODELS.put(rule, bakedModel);
                RULES_BY_MODEL.computeIfAbsent(mrl, k -> new ArrayList<>()).add(rule);
            } catch (Exception e) {
                CITNBT.LOGGER.error("Error baking model for rule: {}", rl);
                CITNBT.LOGGER.error("Error: {}", e.getLocalizedMessage());
            }
        }
    }

    private static final List<String> EMPTY_LIST = Collections.emptyList();
    private static Map<Pair<IRegistryDelegate<Item>, Integer>, ModelResourceLocation> customModels = null;

    @SuppressWarnings("unchecked")
    private static ModelResourceLocation getResourceLocation(Item item, int meta) {
        if (customModels == null) {
            try {
                Class<ModelLoader> loaderClass = ModelLoader.class;
                Field field = loaderClass.getDeclaredField("customModels");
                field.setAccessible(true);
                Object value = field.get(null);
                customModels = (Map<Pair<IRegistryDelegate<Item>, Integer>, ModelResourceLocation>) value;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return customModels.get(Pair.of(item, meta));
    }

    @SuppressWarnings("unchecked")
    private static List<String> getVariantNames(ModelLoader loader, Item item) {
        try {
            Class<?> loaderClass = loader.getClass().getSuperclass();
            Method getVariantNames = loaderClass.getDeclaredMethod("getVariantNames", Item.class);
            getVariantNames.setAccessible(true);
            Object invoked = getVariantNames.invoke(loader, item);
            if (invoked instanceof List<?> newList && !newList.isEmpty() && newList.get(0) instanceof String) {
                return (List<String>) newList;
            }
        } catch (NoSuchMethodException e) {
            CITNBT.LOGGER.error("Unable to getVariantNames from ModelLoader");
        } catch (InvocationTargetException | IllegalAccessException e) {
            CITNBT.LOGGER.error("Unable to execute getVariantNames from ModelLoader");
        }
        return EMPTY_LIST;
    }

    private static ResourceLocation getItemLocation(String location)
    {
        ResourceLocation resourcelocation = new ResourceLocation(location.replaceAll("#.*", ""));
        return new ResourceLocation(resourcelocation.getNamespace(), "item/" + resourcelocation.getPath());
    }
}
