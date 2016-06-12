package com.devsmart.kademlia;


import com.google.common.io.BaseEncoding;

import java.math.BigInteger;
import java.util.Arrays;

public class ID {
    public static final int NUM_BYTES = 20;

    private byte[] mData = new byte[NUM_BYTES];

    public static ID fromBase64String(String str) {
        byte[] data = BaseEncoding.base64Url().decode(str);
        return new ID(data, 0);
    }

    public ID(byte[] buf, int offset) {
        System.arraycopy(buf, offset, mData, 0, NUM_BYTES);
    }

    public int write(byte[] buf, int offset) {
        System.arraycopy(mData, 0, buf, offset, NUM_BYTES);
        return NUM_BYTES;
    }

    /**
     * Compare the distance between ac and bc. Return -1 if ac < bc,
     * 1 if ac > bc and 0 if ac == bc.
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static int compareDistance(ID a, ID b, ID c) {
        for(int i=0;i<NUM_BYTES;i++) {
            int ac = a.mData[i] ^ c.mData[i];
            int bc = b.mData[i] ^ c.mData[i];

            if(ac < bc) {
                return -1;
            } else if(ac > bc) {
                return 1;
            }
        }

        return 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mData);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ID) {
            ID o = (ID) obj;
            return Arrays.equals(mData, o.mData);
        } else {
            return false;
        }
    }

    public String toString(BaseEncoding encoding) {
        return encoding.encode(mData, 0, NUM_BYTES);
    }

    @Override
    public String toString() {
        return toString(BaseEncoding.base64Url());
    }

    public byte[] distance(ID o) {
        byte[] retval = new byte[NUM_BYTES];
        for(int i=0;i<NUM_BYTES;i++) {
            retval[i] = (byte) (mData[i] ^ o.mData[i]);
        }
        return retval;
    }

    public BigInteger getIntDistance(ID o) {
        byte[] data = distance(o);
        return new BigInteger(1, data);
    }

    public int getNumSharedPrefixBits(ID o) {
        int retval = 0;
        for(int i=0;i<NUM_BYTES;i++) {

            int distance = 0xFF & (mData[i] ^ o.mData[i]);
            if(distance < 1) {
                retval += 8;
            } else if(distance < 2) {
                retval += 7;
                break;
            } else if(distance < 4) {
                retval += 6;
                break;
            } else if(distance < 8) {
                retval += 5;
                break;
            } else if(distance < 16) {
                retval += 4;
                break;
            } else if(distance < 32) {
                retval += 3;
                break;
            } else if(distance < 64) {
                retval += 2;
                break;
            } else if(distance < 128) {
                retval += 1;
                break;
            } else {
                break;
            }
        }

        return retval;
    }


}
