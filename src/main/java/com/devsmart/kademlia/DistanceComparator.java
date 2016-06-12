package com.devsmart.kademlia;


import com.devsmart.kademlia.Peer;

import java.util.Comparator;

public class DistanceComparator implements Comparator<Peer> {

    public final ID mCompare;

    public DistanceComparator(ID compareTo) {
        mCompare = compareTo;
    }

    @Override
    public int compare(Peer a, Peer b) {
        return ID.compareDistance(a.id, b.id, mCompare);
    }
}