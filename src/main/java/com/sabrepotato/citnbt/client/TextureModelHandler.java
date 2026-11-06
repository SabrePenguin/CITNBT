package com.sabrepotato.citnbt.client;

import com.google.common.collect.ImmutableMap;
import com.sabrepotato.citnbt.CITNBT;
import com.sabrepotato.citnbt.Tags;
import com.sabrepotato.citnbt.config.FileNBTLoader;
import com.sabrepotato.citnbt.parser.FileLoader;
import com.sabrepotato.citnbt.parser.ItemHolder;
import com.sabrepotato.citnbt.resources.ItemRule;
import com.sabrepotato.citnbt.resources.conditions.Condition;
import com.sabrepotato.citnbt.resources.conditions.RangeSet;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
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
        for(ResourceLocation location: FileLoader.RESOURCES_NEEDING_LOADING) {
            event.getMap().registerSprite(location);
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        BAKED_MODELS.clear();
        RULES_BY_MODEL.clear();

        bakeTypes(event);

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

    private static void bakeTypes(ModelBakeEvent event) {
        for (ItemHolder holder: FileLoader.ITEMS) {
            for (ResourceLocation location: holder.items()) {
                Item item = ForgeRegistries.ITEMS.getValue(location);
                if (item == null) continue;
                if (item.getHasSubtypes()) {
                    bakeSubtypes(holder, item, event);
                } else {
                    ModelResourceLocation mrl = ModelLoader.getInventoryVariant(Objects.requireNonNull(item.getRegistryName()).toString());
                    bake(holder, mrl);
                }
            }
        }
    }

    private static void bakeSubtypes(ItemHolder holder, Item item, ModelBakeEvent event) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        for (CreativeTabs tab: item.getCreativeTabs()) {
            item.getSubItems(tab, stacks);
        }
        IntList list = new IntArrayList(stacks.size());
        for (ItemStack stack: stacks) {
            list.add(stack.getItemDamage());
        }
        RangeSet set = holder.itemstackCondition().getDamageRange();
        PrimitiveIterator.OfInt iterator = set.toIterator();
        boolean success = false;
        while (iterator.hasNext()) {
            ModelResourceLocation location = getResourceLocation(item, iterator.nextInt());
            if (location == null)
                continue;
            bake(holder, location);
            success = true;
        }
        if (success)
            return;
        List<String> names = getVariantNames(event.getModelLoader(), item);
        for (int i = 0; i < names.size(); i++) {
            if (holder.itemstackCondition().checkDamageCondition(new ItemStack(item, 1, list.getInt(i)))) {
                ModelResourceLocation mrl = ModelLoader.getInventoryVariant(names.get(i));
                bake(holder, mrl);
            }
        }
    }

    private static void bake(ItemHolder item, ModelResourceLocation mrl) {
        ItemHolder.ItemType t = item.itemType();
        if (t instanceof ItemHolder.Texture) {
            bakeTexture(item, mrl);
        } else if (t instanceof ItemHolder.Model) {
            bakeModel(item, mrl);
        } else if (t instanceof ItemHolder.Multi) {
            bakeMultiStage(item, mrl);
        }
    }

    private static void bakeTexture(ItemHolder item, ModelResourceLocation mrl) {
        if (!(item.itemType() instanceof ItemHolder.Texture texture))
            return;
        try {
            IModel model = ModelLoaderRegistry.getModel(mrl).retexture(
                    ImmutableMap.of("layer0", texture.texture()));
            IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
            ItemRule rule = new ItemRule(item.nbtConditions(), mrl, item.itemstackCondition());
            BAKED_MODELS.put(rule, bakedModel);
            RULES_BY_MODEL.computeIfAbsent(mrl, k -> new ArrayList<>()).add(rule);
        } catch (Exception e) {
            CITNBT.LOGGER.error("Unable to get model for the ModelResourceLocation {}", mrl);
            CITNBT.LOGGER.error("{}", e.getLocalizedMessage());
        }
    }

    private static void bakeModel(ItemHolder item, ModelResourceLocation mrl) {
        if (item.itemType() instanceof ItemHolder.Model modelHolder) {
            try {
                IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(modelHolder.model()));
                IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                        location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
                ItemRule rule = new ItemRule(item.nbtConditions(), mrl, item.itemstackCondition());
                BAKED_MODELS.put(rule, bakedModel);
                RULES_BY_MODEL.computeIfAbsent(mrl, k -> new ArrayList<>()).add(rule);
            } catch (Exception e) {
                CITNBT.LOGGER.error("Unable to locate model {}", modelHolder.model());
                CITNBT.LOGGER.error("{}", e.getLocalizedMessage());
            }
        }
    }

    @SuppressWarnings("SimplifyOptionalCallChains")
    private static void bakeMultiStage(ItemHolder item, ModelResourceLocation mrl) {
        if (item.itemType() instanceof ItemHolder.Multi multi) {
            try {
                IModel iModel = ModelLoaderRegistry.getModel(mrl);
                Optional<ModelBlock> modelBlock = iModel.asVanillaModel();
                if (!modelBlock.isPresent())
                    return;
                List<ItemOverride> list = modelBlock.get().getOverrides();
                for(int i = list.size()-1; i >= 0; i--) {
                    ItemOverride override = list.get(i);
                    String textureLocation = override.getLocation().toString().replaceAll("^(.*:)?item(?=/)", "$1items");
                    String modelLocation = override.getLocation().toString();
                    boolean isModel = multi.models().containsKey(modelLocation);
                    if (!multi.textures().containsKey(textureLocation) && !isModel) {
                        continue;
                    }
                    IModel overloadModel;
                    if (isModel) {
                        overloadModel = ModelLoaderRegistry.getModel(new ResourceLocation(multi.models().get(modelLocation)));
                    } else {
                        overloadModel = ModelLoaderRegistry.getModel(override.getLocation())
                                .retexture(ImmutableMap.of("layer0", multi.textures().get(textureLocation)));
                    }
                    ItemRule rule = new ItemRule(item.nbtConditions(), mrl, item.itemstackCondition(), new Condition(override));
                    IBakedModel bakedModel = overloadModel.bake(overloadModel.getDefaultState(), DefaultVertexFormats.ITEM,
                            loc ->  Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString()));
                    BAKED_MODELS.put(rule, bakedModel);
                    RULES_BY_MODEL.computeIfAbsent(mrl, k -> new ArrayList<>()).add(rule);
                }
                Map<String, String> textures = modelBlock.get().textures;
                IModel model = null;
                for(Map.Entry<String, String> e : textures.entrySet()) {
                    String texture = e.getValue();
                    if (!texture.contains(":")) {
                        texture = "minecraft:" + texture;
                    }
                    String modelTexture = texture.replaceAll("^(.*:)?items(?=/)", "$1item");
                    if (multi.models().containsKey(modelTexture)) {
                        model = ModelLoaderRegistry.getModel(new ResourceLocation(multi.models().get(modelTexture)));
                    } else if (multi.textures().containsKey(texture)) {
                        if (model == null)
                            model = ModelLoaderRegistry.getModel(mrl);
                        model = model.retexture(ImmutableMap.of(e.getKey(), multi.textures().get(texture)));
                    }
                }
                if (model == null)
                    return;

                IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                        location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
                ItemRule rule = new ItemRule(item.nbtConditions(), mrl, item.itemstackCondition());
                BAKED_MODELS.put(rule, bakedModel);
                RULES_BY_MODEL.computeIfAbsent(mrl, k -> new ArrayList<>()).add(rule);
            } catch (Exception e) {
                CITNBT.LOGGER.error("Unable to locate model {}", mrl);
                CITNBT.LOGGER.error("{}", e.getLocalizedMessage());
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
        return customModels.get(ImmutablePair.of(item.delegate, meta));
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
}
