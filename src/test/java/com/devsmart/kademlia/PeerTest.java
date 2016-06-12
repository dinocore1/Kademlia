package com.devsmart.kademlia;


import org.junit.Test;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;

public class PeerTest {

    private static ID createShortId(int firstbyte) {
        byte[] iddata = new byte[ID.NUM_BYTES];
        iddata[0] = (byte) firstbyte;
        return new ID(iddata, 0);
    }

    @Test
    public void testHashCodeEquals() {
        Peer a = new Peer(createShortId(0), new InetSocketAddress("localhost", 8080));
        Peer b = new Peer(createShortId(0), new InetSocketAddress("127.0.0.1", 8080));

        int aHash = a.hashCode();
        int bHash = b.hashCode();

        assertEquals(aHash, bHash);
    }

    @Test
    public void testHashCodeNotEqual() {
        Peer a = new Peer(createShortId(0), new InetSocketAddress("localhost", 8080));
        Peer b = new Peer(createShortId(0), new InetSocketAddress("127.0.0.1", 8081));

        int aHash = a.hashCode();
        int bHash = b.hashCode();

        assertNotEquals(aHash, bHash);
    }

    @Test
    public void testEquals() {
        Peer a = new Peer(createShortId(0), new InetSocketAddress("localhost", 8080));
        Peer b = new Peer(createShortId(0), new InetSocketAddress("127.0.0.1", 8080));

        assertTrue(a.equals(b));
    }

    @Test
    public void testNotEqual() {
        Peer a = new Peer(createShortId(0), new InetSocketAddress("localhost", 8080));
        Peer b = new Peer(createShortId(1), new InetSocketAddress("127.0.0.1", 8080));

        assertFalse(a.equals(b));
    }
}
