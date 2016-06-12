package com.devsmart.kademlia;


import com.google.common.collect.ComparisonChain;

import java.net.InetSocketAddress;
import java.util.*;

public class RoutingTable {

    private final ID mLocalNode;
    public ArrayList<Peer>[] mPeers;

    @SuppressWarnings("unchecked")
    public RoutingTable(ID localId) {
        mLocalNode = localId;
        mPeers = new ArrayList[ID.NUM_BYTES*8];
        for(int i=0;i<ID.NUM_BYTES*8;i++) {
            mPeers[i] = new ArrayList<Peer>(8);
        }
    }

    public ArrayList<Peer> getBucket(ID id) {
        int numBitsInCommon = id.getNumSharedPrefixBits(mLocalNode);
        ArrayList<Peer> bucket = mPeers[numBitsInCommon];
        return bucket;
    }

    public List<Peer> getRoutingPeers(ID target) {
        ArrayList<Peer> retval = new ArrayList<Peer>();
        getAllPeers(retval);

        final DistanceComparator distanceComparator = new DistanceComparator(target);
        Comparator<Peer> comparator = new Comparator<Peer>() {
            @Override
            public int compare(Peer a, Peer b) {
                final Peer.Status statusA = a.getStatus();
                final Peer.Status statusB = b.getStatus();

                return ComparisonChain.start()
                        .compareTrueFirst(statusA == Peer.Status.Alive, statusB == Peer.Status.Alive)
                        .compare(b, a, distanceComparator)
                        .result();
            }
        };
        Collections.sort(retval, comparator);
        return retval;
    }

    public Peer getPeer(ID id, InetSocketAddress socketAddress) {
        Peer retval = new Peer(id, socketAddress);
        if(id.equals(mLocalNode)) {
            return retval;
        }

        ArrayList<Peer> bucket = getBucket(id);
        synchronized (bucket) {
            for (Peer p : bucket) {
                if (p.equals(retval)) {
                    return p;
                }
            }

            bucket.add(retval);
        }
        return retval;
    }

    public void getAllPeers(Collection<Peer> peerList) {
        for(int i=0;i<ID.NUM_BYTES*8;i++){
            peerList.addAll(mPeers[i]);
        }
    }
}
