package com.sabrepotato.citnbt.resources.conditions;

import net.minecraft.nbt.*;

import java.util.List;
import java.util.regex.Pattern;

public class NBTCondition {
    public enum Type {
        EQUALS,
        CONTAINS,
        ICONTAINS,
        EXISTS,
        NOT_EQUALS,
        RANGE,
    }

    private final String nbtPath;
    private final Type type;
    private final String expectedValue;
    private final boolean shouldTagExist;
    private final List<Range> range;

    public NBTCondition(String nbtPath, Type type, String expectedValue) {
        this.nbtPath = nbtPath;
        this.type = type;
        this.expectedValue = expectedValue;
        this.shouldTagExist = expectedValue.equalsIgnoreCase("true");
        this.range = null;
    }

    public NBTCondition(String nbtPath, Type type, List<Range> rangeValue) {
        this.nbtPath = nbtPath;
        this.type = type;
        this.expectedValue = null;
        this.shouldTagExist = true;
        this.range = rangeValue;
    }

    public boolean matches(NBTTagCompound compound) {
        if (compound == null) return type == Type.EXISTS && !this.shouldTagExist;
        NBTBase current = compound;
        for (String key : nbtPath.split("\\.")) {
            if (!(current instanceof NBTTagCompound)) {
                return false;
            }
            current = ((NBTTagCompound) current).getTag(key);
            if (current == null) {
                return type == Type.EXISTS && !this.shouldTagExist;
            }
        }

        if (type == Type.EXISTS) {
            return this.shouldTagExist;
        } else if (type == Type.RANGE) {
            return this.isValueInRange(current);
        }

        String actual = getValueAsString(current);

        if (actual == null) return false;

        return switch (type) {
            case EQUALS -> actual.equals(expectedValue);
            case CONTAINS -> Pattern.compile(Pattern.quote(expectedValue)).matcher(actual).find();
            case ICONTAINS -> Pattern.compile(Pattern.quote(expectedValue), Pattern.CASE_INSENSITIVE).matcher(actual).find();
            case NOT_EQUALS -> !actual.equals(expectedValue);
            default -> false;
        };
    }

    private String getValueAsString(NBTBase tag) {
        if (tag instanceof NBTTagString) {
            return ((NBTTagString) tag).getString();
        } else if (tag instanceof NBTTagByte) {
            return Byte.toString(((NBTTagByte) tag).getByte());
        } else if (tag instanceof NBTTagInt) {
            return Integer.toString(((NBTTagInt) tag).getInt());
        } else if (tag instanceof NBTTagLong) {
            return Long.toString(((NBTTagLong) tag).getLong());
        } else if (tag instanceof NBTTagFloat) {
            return Float.toString(((NBTTagFloat) tag).getFloat());
        } else if (tag instanceof NBTTagDouble) {
            return Double.toString(((NBTTagDouble) tag).getDouble());
        }
        return null;
    }

    private boolean isValueInRange(NBTBase tag) {
        for (Range r: this.range) {
            if (tag instanceof NBTTagInt && r.contains(((NBTTagInt) tag).getInt())) return true;
            if (tag instanceof NBTTagLong && r.contains(((NBTTagLong) tag).getLong())) return true;
            if (tag instanceof NBTTagShort && r.contains(((NBTTagShort) tag).getShort())) return true;
            if (tag instanceof NBTTagDouble && r.contains(((NBTTagDouble) tag).getDouble())) return true;
            if (tag instanceof NBTTagFloat && r.contains(((NBTTagFloat) tag).getFloat())) return true;
        }
        return false;
    }
}
