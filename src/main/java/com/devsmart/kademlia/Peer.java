package com.devsmart.kademlia;


import com.google.common.io.BaseEncoding;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer {

    private static final int TIME_DIEING = 15 * 1000;
    private static final int TIME_DEAD = 60 * 1000;
    private static final Random RANDOM = new Random();

    public enum Status {
        Unknown,
        Alive,
        Dying,
        Dead
    }

    public final ID id;
    private InetSocketAddress mSocketAddress;
    private long mFirstSeen = -1;
    private long mLastSeen = -1;
    private Future<?> mKeepAliveTask;
    private final long mCreated;

    public Peer(ID id, InetSocketAddress socketAddress) {
        this.id = id;
        this.mSocketAddress = socketAddress;
        this.mCreated = System.nanoTime();
    }

    public InetSocketAddress getInetSocketAddress() {
        return mSocketAddress;
    }

    public void markSeen() {
        mLastSeen = System.nanoTime();
        if(mFirstSeen == -1) {
            mFirstSeen = mLastSeen;
        }
    }

    public long getLastSeenMillisec() {
        long retval = System.nanoTime() - mLastSeen;
        return retval / 1000000;
    }

    public long getFirstSeen() {
        long retval = System.nanoTime() - mFirstSeen;
        return retval / 1000000;
    }

    public long getAge() {
        long retval = System.nanoTime() - mCreated;
        return retval / 1000000;
    }

    public Status getStatus() {
        if(mFirstSeen == -1){
            return Status.Unknown;
        }
        final long lastSeen = getLastSeenMillisec();
        if(lastSeen < TIME_DIEING) {
            return Status.Alive;
        } else if(lastSeen < TIME_DEAD) {
            return Status.Dying;
        } else {
            return Status.Dead;
        }
    }

    public synchronized void startKeepAlive(ScheduledExecutorService executorService, ID localId, DatagramSocket socket) {
        if(mKeepAliveTask == null) {
            KeepAliveTask task = new KeepAliveTask(this, localId, socket);

            long wait = 100 + RANDOM.nextInt(600);
            mKeepAliveTask = executorService.scheduleWithFixedDelay(task, wait, 1500, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void stopKeepAlive() {
        if(mKeepAliveTask != null) {
            mKeepAliveTask.cancel(false);
            mKeepAliveTask = null;
        }
    }

    @Override
    public int hashCode() {
        int retval = id.hashCode() ^ mSocketAddress.hashCode();
        return retval;
    }

    @Override
    public boolean equals(Object obj) {
        boolean retval = false;
        if(obj instanceof Peer) {
            Peer o = (Peer)obj;
            retval = id.equals(o.id) && mSocketAddress.equals(o.mSocketAddress);
        }
        return retval;
    }

    @Override
    public String toString() {
        return String.format("[%s:%s]",
                id.toString(BaseEncoding.base64Url()).substring(0, 6),
                mSocketAddress
        );
    }
}
