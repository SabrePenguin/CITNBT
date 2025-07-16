package com.sabrepotato.citnbt.resources.conditions;

import com.sabrepotato.citnbt.CITNBT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Range {
    final int start;
    final int end;
    private final static Pattern pattern = Pattern.compile("^(-?\\d+)(?:-(-?\\d+)?)?$");
    Range(int start, int end) {
        if (start > end) {
            this.end = start;
            this.start = end;
        } else {
            this.start = start;
            this.end = end;
        }
    }

    boolean contains(int value) {
        return value >= start && value <= end;
    }

    public static Range parse(String input, int minVal, int maxVal) {
        Matcher matcher = pattern.matcher(input.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid range format: " + input);
        }
        int start = Integer.parseInt(matcher.group(1));
        if (start < minVal) start = minVal;
        String endstr = matcher.group(2);
        int end;
        if (input.endsWith("-") && endstr == null) {
            end = maxVal;
        } else if (endstr != null) {
            end = Integer.parseInt(endstr);
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
        return new Range(start, end);
    }

    public static Range parse(String input) {
        return parse(input, 0, 65535);
    }
}
