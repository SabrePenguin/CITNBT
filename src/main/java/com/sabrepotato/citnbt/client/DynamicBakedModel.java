package com.sabrepotato.citnbt.client;

import com.sabrepotato.citnbt.resources.NBTRule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;
import java.util.*;

public class DynamicBakedModel implements IBakedModel {
    private final IBakedModel defaultModel;
    private final List<NBTRule> rules;

    public DynamicBakedModel(IBakedModel defaultModel, List<NBTRule> rules) {
        this.defaultModel = defaultModel;
        this.rules = rules;
    }


    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState iBlockState, @Nullable EnumFacing enumFacing, long rand) {
        return defaultModel.getQuads(iBlockState, enumFacing, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return defaultModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return defaultModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return defaultModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return defaultModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return defaultModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                for (NBTRule rule : rules) {
                    if (rule.matches(stack)) {
                        IBakedModel bake = TextureModelHandler.BAKED_MODELS.get(rule);
                        if (bake != null) return bake;
                    }
                }
                return defaultModel;
            }
        };
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state) {
        return defaultModel.isAmbientOcclusion(state);
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return defaultModel.handlePerspective(cameraTransformType);
    }
}
