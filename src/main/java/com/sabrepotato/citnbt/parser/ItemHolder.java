package com.sabrepotato.citnbt.parser;

import com.github.bsideup.jabel.Desugar;
import com.sabrepotato.citnbt.resources.conditions.ItemstackCondition;
import com.sabrepotato.citnbt.resources.conditions.NBTCondition;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Desugar
public record ItemHolder(
        List<ResourceLocation> items, List<NBTCondition> nbtConditions, ItemstackCondition itemstackCondition,
        ItemType itemType, int weight, String filename)
    implements Comparable<ItemHolder> {

    public interface ItemType {}
    @Desugar
    public record Texture(String texture) implements ItemType {
    }
    @Desugar
    public record Model(String model) implements ItemType {
    }
    @Desugar
    public record Multi(Map<String, String> textures, Map<String, String> models) implements ItemType {
    }

    @Override
    public int compareTo(@NotNull ItemHolder other) {
        if (this.weight != other.weight) return (-1)*Integer.compare(this.weight, other.weight);
        if (!Objects.equals(this.filename, other.filename)) return this.filename.compareTo(other.filename);
        return 0; // We don't care about the order
    }
}
