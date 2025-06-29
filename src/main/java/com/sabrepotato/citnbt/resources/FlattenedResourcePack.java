package com.sabrepotato.citnbt.resources;

import com.google.gson.JsonParser;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class FlattenedResourcePack implements IResourcePack {
    private final File base;

    public FlattenedResourcePack(File base) {
        this.base = base;
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        File file = resolve(location);
        if (file.isFile()) {
            return new FileInputStream(file);
        }
        throw new FileNotFoundException(location.toString());
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return resolve(location).isFile();
    }

    @Override
    public Set<String> getResourceDomains() {
        File[] dirs = base.listFiles(File::isDirectory);
        Set<String> domains = new HashSet<>();
        if (dirs != null) {
            for (File dir : dirs) {
                domains.add(dir.getName());
            }
        }
        return domains;
    }

    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer serializer, String section) throws IOException {
        File meta = new File(base, "pack.mcmeta");
        if (!meta.isFile()) return null;

        try (FileReader reader = new FileReader(meta)) {
            JsonParser parser = new JsonParser();
            return serializer.parseMetadataSection(section, parser.parse(reader).getAsJsonObject());
        }
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        throw new FileNotFoundException("No pack.png");
    }

    @Override
    public String getPackName() {
        return "Flat Resource Folder";
    }

    private File resolve(ResourceLocation location) {
        // minecraft/resources/<namespace>/<path>
        return new File(base, new File(location.getNamespace(), location.getPath()).getPath());
    }
}
