package com.devsmart.kademlia;


import com.google.common.base.Throwables;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

public class Message {

    public static final int PING = 0;
    public static final int FINDPEERS = 1;
    public static final int CONNECT = 2;

    private static final int FLAG_RESPONSE = 0x10;
    /*

        Version: 2bits
        X: Reserved: 2bits
        R: (Response): 1bit
        PT (Payload Type): 3bits

         0 1 2 3 4 5 6 7 8
        +-+-+-+-+-+-+-+-+-
        |Ver|   |R| PT  |


        # SocketAddress #
          0   1   2   3   4   5
        +------------------------+
        | IPv4 Addr.    | Port   |

        ID: 20-byte value
        SocketAddress: 4-byte address, 2 byte port

        ## Payload Types ##

        ### Ping ###
        PT: 0

        Request Payload:
        ID: Local Peer's ID

        Response Payload:
        ID: Remote Peer's ID
        SocketAddress: the socket address that the ping request came in on

        ### FindPeers ###
        PT: 1

        Request Payload:
        ID

        Response Payload:

        {ID, SocketAddress}[]

        ### Connect ###
        PT: 2

        Request Payload:
        TTL: 1-byte begins at 0, incremented by each hop
        ID: destId
        ID: fromId
        SocketAddress[]: from address

        Response Payload:
        TTL: 1-byte begins at 0, incremented by each hop
        ID: destId
        Yes/No, ID, SocketAddress[]

        */
    byte[] mRawData = new byte[64 * 1024];
    public final DatagramPacket mPacket = new DatagramPacket(mRawData, mRawData.length);

    private static int writeIPv4AddressPort(byte[] buf, int offset, InetAddress address, int port) {
        byte[] addressBytes = address.getAddress();
        System.arraycopy(addressBytes, 0, buf, offset, 4);
        buf[offset + 4] = (byte) (0xFF & port);
        buf[offset + 5] = (byte) (0xFF & (port >>> 8));

        return 6;
    }

    private static InetSocketAddress readIPv4AddressPort(byte[] buf, int offset) {
        try {
            byte[] addressData = new byte[4];
            System.arraycopy(buf, offset, addressData, 0, 4);
            InetAddress address = InetAddress.getByAddress(addressData);

            int port = buf[offset + 4] & 0xFF;
            port |= ((buf[offset + 5] << 8) & 0xFF00);

            return new InetSocketAddress(address, port);
        } catch (UnknownHostException e) {
            Throwables.propagate(e);
            return null;
        }
    }

    public boolean parseData() {
        return false;
    }

    public int version() {
        return mRawData[0] >>> 6;
    }

    public int getType() {
        return 0x7 & mRawData[0];
    }

    public boolean isResponse() {
        return (FLAG_RESPONSE & mRawData[0]) > 0;
    }

    public InetSocketAddress getRemoteSocketAddress() {
        return (InetSocketAddress) mPacket.getSocketAddress();
    }

    public void prepareReceive() {
        mPacket.setData(mRawData);
    }

    public static class PingMessage {

        public static ID getId(Message msg) {
            return new ID(msg.mRawData, 1);
        }

        public static InetSocketAddress getSocketAddress(Message msg) {
            return readIPv4AddressPort(msg.mRawData, 1 + ID.NUM_BYTES);
        }

        public static void formatRequest(Message msg, ID id) {
            msg.mRawData[0] = PING;
            int offset = 1;
            offset += id.write(msg.mRawData, 1);
            msg.mPacket.setData(msg.mRawData, 0, offset);
        }

        public static void formatResponse(Message msg, ID id, InetSocketAddress remoteAddress) {
            msg.mRawData[0] = PING | FLAG_RESPONSE;

            int offset = 1;
            offset += id.write(msg.mRawData, offset);
            offset += writeIPv4AddressPort(msg.mRawData, offset, remoteAddress.getAddress(), remoteAddress.getPort());
            msg.mPacket.setData(msg.mRawData, 0, offset);
        }
    }

    public static class FindPeersMessage {

        public static void formatRequest(Message msg, ID targetId) {
            msg.mRawData[0] = FINDPEERS;

            int offset = 1;
            offset += targetId.write(msg.mRawData, 1);
            msg.mPacket.setData(msg.mRawData, 0, offset);
        }

        public static void formatResponse(Message msg, Collection<Peer> peers) {
            msg.mRawData[0] = FINDPEERS | FLAG_RESPONSE;

            int max = Math.min(8, peers.size());
            msg.mRawData[1] = (byte) max;

            int i = 0;

            int offset = 2;
            for (Peer p : peers) {
                offset += p.id.write(msg.mRawData, offset);
                InetSocketAddress socketAddress = p.getInetSocketAddress();
                offset += writeIPv4AddressPort(msg.mRawData, offset, socketAddress.getAddress(), socketAddress.getPort());

                if (++i >= max) {
                    break;
                }
            }
            msg.mPacket.setData(msg.mRawData, 0, offset);
        }

        public static ID getTargetId(Message msg) {
            return new ID(msg.mRawData, 1);
        }

        public static Collection<Peer> getPeers(Message msg) {
            final int numPeers = 0x00ff & msg.mRawData[1];
            ArrayList<Peer> retval = new ArrayList<Peer>(numPeers);

            int offset = 2;
            for (int i = 0; i < numPeers; i++) {
                ID id = new ID(msg.mRawData, offset);
                offset += ID.NUM_BYTES;
                InetSocketAddress socketAddress = readIPv4AddressPort(msg.mRawData, offset);
                offset += 6;
                Peer p = new Peer(id, socketAddress);
                retval.add(p);
            }

            return retval;
        }
    }

    public static class ConnectMessage {

        public static void formatRequest(Message msg, int tty, ID targetId, ID fromID, Collection<InetSocketAddress> addresses) {
            msg.mRawData[0] = CONNECT;
            int offset = 1;

            //tty
            msg.mRawData[offset] = (byte) tty;
            offset += 1;

            offset += targetId.write(msg.mRawData, offset);

            offset += fromID.write(msg.mRawData, offset);

            final int max = Math.min(4, addresses.size());
            msg.mRawData[offset] = (byte) max;
            offset += 1;
            int i = 0;
            for (InetSocketAddress address : addresses) {
                offset += writeIPv4AddressPort(msg.mRawData, offset, address.getAddress(), address.getPort());
                if (++i >= max) {
                    break;
                }
            }

            msg.mPacket.setData(msg.mRawData, 0, offset);
        }

        public static ID getTargetId(Message msg) {
            return new ID(msg.mRawData, 2);
        }

        public static ID getFromId(Message msg) {
            int offset = 2 + ID.NUM_BYTES;
            return new ID(msg.mRawData, offset);
        }

        public static int getTTY(Message msg) {
            return msg.mRawData[1];
        }

        public static Collection<InetSocketAddress> getSocketAddresses(Message msg) {
            ArrayList<InetSocketAddress> retval = new ArrayList<InetSocketAddress>(4);
            int offset = 2 + 2 * ID.NUM_BYTES;
            final int size = 0xff & msg.mRawData[offset];
            offset += 1;

            for (int i = 0; i < size; i++) {
                retval.add(readIPv4AddressPort(msg.mRawData, offset));
                offset += 6;
            }

            return retval;
        }


    }
}
