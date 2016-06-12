package com.devsmart.kademlia;


import java.net.DatagramSocket;

public class Node {

    private final ID mLocalId;
    private final DatagramSocket mSocket;

    public Node(ID localId, DatagramSocket socket) {
        mLocalId = localId;
        mSocket = socket;
    }

    public void start() {

    }

    public void shutdown() {
        
    }

}
