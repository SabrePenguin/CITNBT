package com.sabrepotato.citnbt.resources;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@SideOnly(Side.CLIENT)
public class FakeResourcePack implements IResourcePack {

    private final Map<ResourceLocation, byte[]> modelJsons = new HashMap<>();
    private final String domain;

    public FakeResourcePack(String domain) {
        this.domain = domain;
    }

    public void addModel(ResourceLocation location, byte[] data) {
        modelJsons.put(location, data);
    }

    public void clear() {
        modelJsons.clear();
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        byte[] data = modelJsons.get(location);
        if (data != null) {
            return new ByteArrayInputStream(data);
        }
        throw new IOException("No such resource: " + location);
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return modelJsons.containsKey(location);
    }

    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton(domain);
    }

    @Override
    public @Nullable <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return null;
    }

    @Override
    public String getPackName() {
        return "Generated Models Pack";
    }
}
