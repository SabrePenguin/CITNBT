package com.sabrepotato.citnbt.resources.conditions;

import com.mojang.realmsclient.util.Pair;
import com.sabrepotato.citnbt.CITNBT;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RangeSet {

    /**
     * Bounds of the Range. Contains pairs of integers, in [inclusive, exclusive]
     */
    private final IntList boundaries;

    public RangeSet() {
        boundaries = new IntArrayList();
    }

    public void add(int lower, int upper) {
        if (lower >= upper) {
            CITNBT.LOGGER.error("Lower bound is equal or greater to upper: {} >= {}", lower, upper);
            return;
        }
        int index = 0;
        int size = boundaries.size();
        while (index < size && boundaries.getInt(index+1) <= lower) {
            index += 2;
        }

        while (index < size && boundaries.getInt(index) <= upper) {
            lower = Math.min(lower, boundaries.getInt(index));
            upper = Math.max(upper, boundaries.getInt(index+1));

            boundaries.removeInt(index+1);
            boundaries.removeInt(index);
            size = boundaries.size();
        }

        boundaries.add(index, upper);
        boundaries.add(index, lower);
    }

    protected int binarySearch(double key) {
        int midVal;
        int to = boundaries.size() - 1;
        int from = 0;
        while (from <= to) {
            final int mid = (from + to) >>> 1;
            midVal = boundaries.getInt(mid);
            if (midVal < key) from = mid + 1;
            else if (midVal > key) to = mid - 1;
            else return mid;
        }
        return -(from + 1);
    }

    protected int binarySearch(int key) {
        int midVal;
        int to = boundaries.size() - 1;
        int from = 0;
        while (from <= to) {
            final int mid = (from + to) >>> 1;
            midVal = boundaries.getInt(mid);
            if (midVal < key) from = mid + 1;
            else if (midVal > key) to = mid - 1;
            else return mid;
        }
        return -(from + 1);
    }

    public boolean contains(int value) {
        int index = binarySearch(value);
        if (index >= 0) {
            return (index % 2 == 0);
        }
        int trueIndex = ((-index) - 1);
        return (trueIndex % 2 != 0);
    }

    public boolean contains(long value) {
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
            return false;
        return contains((int) value);
    }

    public boolean contains(double value) {
        int index = binarySearch(value);
        if (index >= 0) {
            return (index % 2 == 0);
        }
        int trueIndex = ((-index) - 1);
        return (trueIndex % 2 != 0);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < boundaries.size(); i+=2) {
            if (i > 0)
                builder.append(", ");
            builder.append("[").append(boundaries.getInt(i)).append(",").append(boundaries.getInt(i+1)).append(")");
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    @SuppressWarnings("EqualsNullCheck")
    public boolean equals(Object obj) {
        if (!(obj instanceof RangeSet other)) return false;
        return boundaries.equals(other.boundaries);
    }

    @Override
    public int hashCode() {
        return boundaries.hashCode();
    }

    private final static Pattern pattern = Pattern.compile("^(\\(-?\\d+\\)|-?\\d+)(?:-(\\(-?\\d+\\)|\\d+)?)?$");

    public static Pair<Integer, Integer> parse(String input, int minVal, int maxVal) {
        Matcher matcher = pattern.matcher(input.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid range format: " + input);
        }
        int start = Integer.parseInt(matcher.group(1).replace("(","").replace(")", ""));
        if (start < minVal) start = minVal;
        String endstr = matcher.group(2);
        int end;
        if (input.endsWith("-") && endstr == null) {
            end = maxVal;
        } else if (endstr != null) {
            end = Integer.parseInt(endstr.replace("(","").replace(")", ""));
            if (end < minVal) {
                CITNBT.LOGGER.warn("Upper limit is below minimum value {}: {}", minVal, end);
                end = minVal;
            }
            if (end > maxVal) {
                CITNBT.LOGGER.warn("Upper limit is above maximum value {}: {}", maxVal, end);
                end = maxVal;
            }
        } else {
            end = start;
        }
        return Pair.of(start, end+1);
    }

    public PrimitiveIterator.OfInt toIterator() {
        return new RangeSetIterator(boundaries);
    }

    private static class RangeSetIterator implements PrimitiveIterator.OfInt {

        private final IntList boundaries;
        private int range;
        private int currentVal;
        private int currentEnd;

        RangeSetIterator(IntList boundaries) {
            this.boundaries = boundaries;
            range = 0;
            if (!this.boundaries.isEmpty()) {
                currentVal = boundaries.getInt(0);
                currentEnd = boundaries.getInt(1);
            } else {
                currentVal = 0;
                currentEnd = 0;
            }
        }

        @Override
        public int nextInt() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return currentVal++;
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public boolean hasNext() {
            while (currentVal >= currentEnd && range < boundaries.size()) {
                range += 2;
                if (range < boundaries.size()) {
                    currentVal = boundaries.getInt(range);
                    currentEnd = boundaries.getInt(range+1);
                }
            }
            return range < boundaries.size() && currentVal < currentEnd;
        }
    }
}
