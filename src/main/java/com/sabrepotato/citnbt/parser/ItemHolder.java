package com.sabrepotato.citnbt.parser;

import com.github.bsideup.jabel.Desugar;
import com.sabrepotato.citnbt.resources.conditions.ItemstackCondition;
import com.sabrepotato.citnbt.resources.conditions.NBTCondition;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

@Desugar
public record ItemHolder(List<ResourceLocation> items, List<NBTCondition> nbtConditions, ItemstackCondition itemstackCondition, ItemType itemType, int weight) {

    public interface ItemType {}
    @Desugar
    public record Texture(String texture) implements ItemType {
    }
    @Desugar
    public record MultiTexture(Map<String, String> texture) implements ItemType {
    }
    @Desugar
    public record Model(String model) implements ItemType {
    }
    @Desugar
    public record MultiModel(Map<String, String> model) implements ItemType {
    }
}
