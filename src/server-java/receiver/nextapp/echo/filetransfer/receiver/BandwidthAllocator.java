/* 
 * This file is part of the Echo File Transfer Library.
 * Copyright (C) 2002-2009 NextApp, Inc.
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */

package nextapp.echo.filetransfer.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides throttled stream copying.
 */
public class BandwidthAllocator {
    
    /**
     * An allocation handle.
     */
    private static class Allocation {

        /**
         * The number of bytes which may be transferred in the current allocation cycle.
         */
        private int remainingBytes;
    }

    /**
     * Interface for communicating with an in-progress transfer.
     * Provides notifications of progress and the capability to abort the transfer.
     */
    public static interface Tracker {
        
        /**
         * Provides notification of transfered data.
         * 
         * @param bytes the cumulative amount of data transferred
         */
        public void bytesTransferred(long bytes);
        
        /**
         * Determines whether the transfer should be aborted.
         * Returning false will allow the transfer to continue, returning true will cause it to abort.
         * 
         * @return true if the transfer should be aborted
         */
        public boolean isAborted();
    }
    
    private static final int BUFFER_SIZE = 4096;
    
    /**
     * The set of active {@link Allocation}s.
     */
    private Set allocations = new HashSet();
    
    /**
     * Interval between allocations, in milliseconds.
     */
    private int interval = 25;
    
    /**
     * The last time bytes were allocated to {@link Allocation}s.
     */
    private long lastAllocationTime = 0;
    
    /**
     * Flag indicating whether throttling is enabled.
     */
    private boolean throttling = false;
    
    /**
     * The maximum number of bytes per second which should be transmitted.
     */
    private int bandwidth;
    
    /**
     * The maximum number of bytes per interval should be transmitted.
     */
    private int bytesPerInterval;
    
    /**
     * Creates a new <code>BandwidthAllocator</code> with throttling disabled.
     */
    public BandwidthAllocator() {
        super();
        setBandwidth(1024 * 1024); // 1MB per second.
    }
    
    /**
     * Creates a new <code>BandwidthAllocator</code> that will allow a specific number of bytes per second to be transferred.
     * 
     * @param targetBytesPerSecond the maximum number of bytes per second which should be transmitted
     */
    public BandwidthAllocator(int targetBytesPerSecond) {
        this();
        setThrottling(true);
        setBandwidth(targetBytesPerSecond);
    }
    
    /**
     * Allocates bytes to all registered {@link Allocation}s.
     */
    private synchronized void allocate() {
        long time = System.currentTimeMillis();
        if (time - interval < lastAllocationTime) {
            // Do nothing, it's not yet time to allocate.
            return;
        }
        
        int bytes = bytesPerInterval / allocations.size();
        
        Iterator it = allocations.iterator();
        while (it.hasNext()) {
            Allocation allocation = (Allocation) it.next();
            allocation.remainingBytes = bytes;
        }
        
        lastAllocationTime = time;
    }
    
    /**
     * Reads from an {@link InputStream} writing retrieved information to an {@link OutputStream}.
     * 
     * @param tracker a {@link Tracker} which can be used to observe progress (may be null if monitoring not desired)
     * @param input the {@link InputStream} to read from
     * @param output the {@link OutputStream} to write to
     */
    public void copy(Tracker tracker, InputStream input, OutputStream output)
    throws IOException {
        BandwidthAllocator.Allocation handle = create();
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                if (tracker.isAborted()) {
                    return;
                }
                output.write(buffer, 0, n);
                if (tracker != null) {
                    tracker.bytesTransferred(n);
                }
                if (throttling) {
                    handle.remainingBytes -= n;
                    while (handle.remainingBytes <= 0) {
                        allocate();
                        if (handle.remainingBytes <= 0) {
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
            }
        } finally {
            remove(handle);
        }
    }
    
    /**
     * Creates a new {@link Allocation}, adding it to the set of active allocation handles.
     * 
     * @return the created {@link Allocation}
     */
    private synchronized Allocation create() {
        Allocation handle = new Allocation();
        allocations.add(handle);
        return handle;
    }
    
    /**
     * Returns the number of bytes per second which the bandwidth allocator will allow.
     * 
     * @return the number of bytes per second
     */
    public int getBandwidth() {
        return bandwidth;
    }
    
    /**
     * Determines whether throttling is currently enabled.
     * 
     * @return true if throttling is enabled
     */
    public boolean isThrottling() {
        return throttling;
    }
    
    /**
     * Removes a {@link Allocation} from the set of active allocation handles.
     * 
     * @param handle the {@link Allocation} to remove
     */
    private synchronized void remove(Allocation handle) {
        allocations.remove(handle);
    }
    
    /**
     * Sets whether throttling is enabled.
     * When throttling is not enabled, the copy() implementation will perform without any delays.
     * 
     * @param newValue true to enable throttling
     */
    public void setThrottling(boolean newValue) {
        throttling = newValue;
    }
    
    /**
     * Sets the number of bytes per second which the bandwidth allocator will allow.
     * 
     * @param newValue the number of bytes per second
     */
    public void setBandwidth(int newValue) {
        if (newValue > 0) {
            bandwidth = newValue;
            bytesPerInterval = newValue / (1000 / interval);
        }
    }
}
