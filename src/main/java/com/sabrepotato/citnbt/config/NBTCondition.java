package com.sabrepotato.citnbt.config;

import net.minecraft.nbt.*;

public class NBTCondition {
    public enum Type {
        EQUALS,
        CONTAINS,
        EXISTS,
        NOT_EQUALS
    }

    private final String nbtPath;
    private final Type type;
    private final String expectedValue;

    public NBTCondition(String nbtPath, Type type, String expectedValue) {
        this.nbtPath = nbtPath;
        this.type = type;
        this.expectedValue = expectedValue;
    }

    public boolean matches(NBTTagCompound compound) {
        if (compound == null) return false;
        NBTBase current = compound;
        for (String key : nbtPath.split("\\.")) {
            if (!(current instanceof NBTTagCompound)) {
                return false;
            }
            current = ((NBTTagCompound) current).getTag(key);
            if (current == null) {
                return false;
            }
        }

        if (type == Type.EXISTS) {
            return true;
        }

        String actual = getValueAsString(current);

        if (actual == null) return false;

        return switch (type) {
            case EQUALS -> actual.equals(expectedValue);
            case CONTAINS -> actual.contains(expectedValue);
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
}
