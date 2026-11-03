package com.sabrepenguin.citnbt;

import com.sabrepotato.citnbt.resources.conditions.RangeSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

class TestRangeSet {

    @Test
    public void testAdd() {
        RangeSet set = new RangeSet();
        set.add(0, 5);
        Assertions.assertEquals("[[0,5)]", set.toString());
        set.add(7,9);
        Assertions.assertEquals("[[0,5), [7,9)]", set.toString());
        set.add(-3, 8);
        Assertions.assertEquals("[[-3,9)]", set.toString());
        set.add(0, 5);
        Assertions.assertEquals("[[-3,9)]", set.toString());
    }

    @Test
    public void testContains() {
        RangeSet set = new RangeSet();
        set.add(0, 5);
        set.add(7,9);
        Assertions.assertFalse(set.contains(6));
        Assertions.assertFalse(set.contains(5));
        Assertions.assertTrue(set.contains(4));
        Assertions.assertTrue(set.contains(7));
    }

    @Test
    public void testIteratorThrows() {
        RangeSet set = new RangeSet();
        set.add(0, 5);
        set.add(7,9);
        PrimitiveIterator.OfInt iterator = set.toIterator();
        while(iterator.hasNext()) {
            iterator.nextInt();
        }
        Assertions.assertThrows(NoSuchElementException.class, iterator::nextInt);
    }

    @Test
    public void testIterator() {
        RangeSet set = new RangeSet();
        int[] values = new int[] {0,1,2,3,4,7,8};
        set.add(0, 5);
        set.add(7,9);
        PrimitiveIterator.OfInt iterator = set.toIterator();
        for (int value : values) {
            int next = iterator.nextInt();
            Assertions.assertEquals(value, next);
        }
    }
}