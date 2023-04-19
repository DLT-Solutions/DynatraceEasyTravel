package com.dynatrace.easytravel.launcher.misc;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * The read methods of this InputStream will block by repeatedly calling Thread.sleep() until unblock() is called.
 * 
 * @author peter.kaiser
 */
public class BlockingInputStream extends InputStream {
    
    private static final Logger LOGGER = Logger.getLogger(BlockingInputStream.class.getName());
    
    private volatile CountDownLatch blockingLatch; // NOPMD
    
    private volatile boolean closed = false; // NOPMD
    
    
    public BlockingInputStream() {
        blockingLatch = new CountDownLatch(1);
    }
    
    
    @Override
    public int read() throws IOException {
        block();
        return 0;
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        block();
        return 0;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        block();
        return 0;
    }
    
    
    private void block() {
        try {
            if (!closed) {
                blockingLatch.await();
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.WARNING, "Waiting for the blocking CountDownLatch was Interrupted.", ie);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * All blocked Threads will continue execution after this call as soon as Thread.sleep() returns. 
     *
     * @author peter.kaiser
     */
    public synchronized void unblock() {
        CountDownLatch blk = blockingLatch;
        blockingLatch = new CountDownLatch(1);
        blk.countDown();
    }
    
    
    @Override
    public synchronized void close() throws IOException {
        super.close();
        closed = true;
        unblock();
    }

}
