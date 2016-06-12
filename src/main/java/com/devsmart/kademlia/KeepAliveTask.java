package com.devsmart.kademlia;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;

public class KeepAliveTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveTask.class);

    private final Peer mPeer;
    private final Message mPingMessage = new Message();
    private final ID mLocalId;
    private final DatagramSocket mSocket;

    public KeepAliveTask(Peer peer, ID localId, DatagramSocket socket) {
        mPeer = peer;
        mLocalId = localId;
        mSocket = socket;
    }

    @Override
    public void run() {

        try {
            //send ping
            logger.debug("sending PING to {}", mPeer);
            Message.PingMessage.formatRequest(mPingMessage, mLocalId);
            mPingMessage.mPacket.setSocketAddress(mPeer.getInetSocketAddress());
            mSocket.send(mPingMessage.mPacket);
        } catch (Exception e) {
            logger.error("", e);
        }


    }
}
