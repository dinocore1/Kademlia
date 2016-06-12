package com.devsmart.kademlia;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class IDTests {

    private static ID createShortId(int firstbyte) {
        byte[] iddata = new byte[ID.NUM_BYTES];
        iddata[0] = (byte) firstbyte;
        return new ID(iddata, 0);
    }

    @Test
    public void testIDDistanceEqual() {
        ID a = createShortId(-1);
        ID b = createShortId(-1);

        BigInteger distance = a.getIntDistance(b);
        assertEquals(0, distance.intValue());
    }

    @Test
    public void testIDDistanceReflexive() {
        ID a = createShortId(0b01);
        ID b = createShortId(0b00);

        BigInteger ab = a.getIntDistance(b);
        BigInteger ba = b.getIntDistance(a);

        assertTrue(ab.compareTo(BigInteger.ZERO) > 0);
        assertEquals(ab, ba);
    }

    @Test
    public void testCompareDistance() {
        ID a = createShortId(0b01);
        ID b = createShortId(0b00);
        ID c = createShortId(0b00);

        assertEquals(1, ID.compareDistance(a, b, c));

        a = createShortId(0b00);
        b = createShortId(0b01);
        assertEquals(-1, ID.compareDistance(a, b, c));

        a = createShortId(0b00);
        b = createShortId(0b00);
        assertEquals(0, ID.compareDistance(a, b, c));

        a = createShortId(0b00);
        b = createShortId(0b10);
        assertEquals(-1, ID.compareDistance(a, b, c));
    }

    @Test
    public void testNumSharedPrefixBits() {
        ID a = createShortId(0b10000000);
        ID b = createShortId(0b10000000);

        int sharedBits = a.getNumSharedPrefixBits(b);
        assertEquals(160, sharedBits);

        b = createShortId(0b00000000);
        sharedBits = a.getNumSharedPrefixBits(b);
        assertEquals(0, sharedBits);

        b = createShortId(0b11000000);
        sharedBits = a.getNumSharedPrefixBits(b);
        assertEquals(1, sharedBits);

        b = createShortId(0b10100000);
        sharedBits = a.getNumSharedPrefixBits(b);
        assertEquals(2, sharedBits);

    }
}
