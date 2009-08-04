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

package nextapp.echo.filetransfer.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nextapp.echo.filetransfer.model.event.UploadProcessEvent;
import nextapp.echo.filetransfer.model.event.UploadProcessListener;

/**
 * A representation of an upload process.  Multiple {@link Upload} objects may be contained in a single <code>UploadProcess</code>
 * if they are being transferred from client to server at the same time.
 */
public class UploadProcess {
    
    /**
     * {@link Upload} implementation.
     */
    private class UploadImpl
    implements Upload {
        
        private String contentType;
        private String fileName;
        private InputStream in;
        private long progress;
        private long size;
        private int status;
        
        /**
         * @see nextapp.echo.filetransfer.model.Upload#cancel()
         */
        public void cancel() {
            setStatus(STATUS_CANCELED);
        }
        
        /**
         * @see nextapp.echo.filetransfer.model.Upload#getContentType()
         */
        public String getContentType() {
            return contentType;
        }
    
        /**
         * @see nextapp.echo.filetransfer.model.Upload#getFileName()
         */
        public String getFileName() {
            return fileName;
        }
    
        /**
         * @see nextapp.echo.filetransfer.model.Upload#getInputStream()
         */
        public InputStream getInputStream() {
            return in;
        }
    
        /**
         * @see nextapp.echo.filetransfer.model.Upload#getProgress()
         */
        public long getProgress() {
            return progress;
        }
    
        /**
         * @see nextapp.echo.filetransfer.model.Upload#getSize()
         */
        public long getSize() {
            return size;
        }
        
        /**
         * @see nextapp.echo.filetransfer.model.Upload#getStatus()
         */
        public int getStatus() {  
            return status;
        }
    
        /**
         * Sets the content type of the file.
         * 
         * @param contentType the content type
         * @see #getFileName()
         */
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
        
        /**
         * Sets the name of the file.
         * 
         * @param fileName the file name
         * @see #getFileName()
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        /**
         * Sets the input stream used to retrieve the uploaded file's content.
         * 
         * @param in the {@link InputStream} used to retrieve the file
         * @see #getInputStream()
         */
        public void setInputStream(InputStream in) {
            this.in = in;
        }
        
        /**
         * Sets the progress.
         * 
         * @param progress the progress value
         * @see #getProgress()
         */
        public void setProgress(long progress) {
            this.progress = progress;
        }
        
        /**
         * Sets the size of the file.
         * 
         * @param size the size of the file, in bytes
         * @see #getSize()
         */
        public void setSize(long size) {
            this.size = size;
        }
        
        /**
         * Sets the upload status.
         * 
         * @param status the upload status
         * @see #getStatus()
         */
        public void setStatus(int status) {
            if (this.status == STATUS_CANCELED) {
                return;
            }
            this.status = status;
        }
        
        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "Upload: ContentType=" + contentType + " FileName=" + fileName + " Size=" + size +
                  " InputStream=" + (in == null ? null : in.getClass().getName()) + " Progress=" + (progress / 1000) + "K" +
                  " Status=" + status;
        }
    }  
    
    /**
     * The id of the <code>UploadProcess</code>.
     */
    private String id;
    
    /**
     * Set of registered {@link UploadProcessListener}s.
     */
    private Set listeners;
    
    /**
     * Combined size of all uploads.
     */
    private long size = -1;
    
    /**
     * List of {@link Upload}s being managed.
     */
    private List uploadList = new ArrayList();
    
    /**
     * Array cache of uploads.  Cleared when uploads changed, lazy (re)created by invocations to {@link #getUploads} 
     */
    private Upload[] uploads = null;
    
    /**
     * Flag indicating whether the entire process has been canceled.
     */
    private boolean canceled = false;
    
    /**
     * Creates a new <code>UploadProcess</code>.
     * 
     * @param id a unique identifier
     */
    public UploadProcess(String id) {
        super();
        this.id = id;
    }
    
    /**
     * Adds an {@link UploadProcessListener} to receive notification of {@link UploadProcessEvent}s.
     * 
     * @param l the listener to add.
     */
    public void addProcessListener(UploadProcessListener l) {
        if (listeners == null) {
            listeners = new HashSet();
        }
        listeners.add(l);
    }
    
    /**
     * Cancels the <code>UploadProcess</code>.  All contained uploads will be canceled.
     * {@link UploadProcessListener}s will be notified of the cancellation.
     */
    public void cancel() {
        Upload[] uploads = getUploads();
        for (int i = 0; i < uploads.length; ++i) {
            uploads[i].cancel();
        }
        if (listeners == null) {
            return;
        }
        UploadProcessEvent e = new UploadProcessEvent(this, null);
        UploadProcessListener[] listenerArray = new UploadProcessListener[listeners.size()];
        listeners.toArray(listenerArray);
        for (int i = 0; i < listenerArray.length; ++i) {
            listenerArray[i].uploadCancel(e);
        }
    }
    
    /**
     * Completes an individual {@link Upload}.
     * The specified {@link InputStream} and size information will be stored in the {@link Upload}.
     * 
     * @param upload the <code>Upload</code>
     * @param in an {@link InputStream} containing the upload data
     * @param size the length of the data, in bytes
     */
    public void complete(Upload upload, InputStream in, long size) {
        ((UploadImpl) upload).setSize(size);
        if (upload.getStatus() == Upload.STATUS_IN_PROGRESS) {
            ((UploadImpl) upload).setInputStream(in);
            ((UploadImpl) upload).setStatus(Upload.STATUS_COMPLETE);
            
            if (listeners == null) {
                return;
            }
            UploadProcessEvent e = new UploadProcessEvent(this, upload);
            UploadProcessListener[] listenerArray = new UploadProcessListener[listeners.size()];
            listeners.toArray(listenerArray);
            for (int i = 0; i < listenerArray.length; ++i) {
                listenerArray[i].uploadComplete(e);
            }
        } else {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    /**
     * Configures an upload, storing its file name and content type.
     * 
     * @param upload the {@link Upload} to configure
     * @param contentType the content type
     * @param fileName the file name
     */
    public void configure(Upload upload, String contentType, String fileName) {
        ((UploadImpl) upload).setContentType(contentType);
        ((UploadImpl) upload).setFileName(fileName);
    }
    
    /**
     * Creates an {@link Upload} instance, adding it to the process.
     * 
     * @return the upload instance
     */
    public Upload createUpload() {
        UploadImpl upload = new UploadImpl();
        uploadList.add(upload);
        uploads = null;
        return upload;
    }
    
    /**
     * Disposes of the <code>UploadProcess</code>, clearing all data.
     */
    public void dispose() {
        size = -1;
        uploadList.clear();
        uploads = null;
    }

    /**
     * Returns the total progress of all uploads.
     * 
     * @return the total progress, in bytes
     */
    public synchronized long getProgress() {
        getUploads();
        long progress = 0;
        for (int i = 0; i < uploads.length; ++i) {
            progress += uploads[i].getProgress();
        }
        return progress;
    }
    
    /**
     * Returns the unique identifier of the <code>UploadProcess</code>.
     * 
     * @return the unique identifier
     */
    public String getId() {
        return id;
    }
    
    /**
     * Returns the total upload size.
     */
    public long getSize() {
        return size;
    }
    
    public Upload getUpload() {
        getUploads();
        return uploads.length == 0 ? null : uploads[0];
    }
    
    /**
     * Retrieves all contained {@link Upload}s.
     * 
     * @return the contained uploads
     */
    public synchronized Upload[] getUploads() {
        if (uploads == null) {
            uploads = new Upload[uploadList.size()];
            uploadList.toArray(uploads);
        }
        return uploads;
    }
    
    /**
     * Initializes the <code>UploadState</code> state object.
     * 
     * @param size the combined size of all uploads
     */
    public void init(long size) {
        if (this.size != -1) {
            throw new IllegalStateException("UploadState already initialized.");
        }
        if (size < 0) {
            throw new IllegalStateException("Invalid upload size.");
        }
        this.size = size;
    }
    
    /**
     * Determines if the upload process has been canceled.
     * 
     * @return true if the upload process has been canceled
     */
    public boolean isCanceled() {
        return canceled;
    }
    
    /**
     * Determines if all uploads have been completed, i.e., no uploads have a status
     * of {@link Upload#STATUS_IN_PROGRESS}
     * 
     * @return true if all uploads have been completed
     */
    public synchronized boolean isComplete() {
        getUploads();
        for (int i = 0; i < uploads.length; ++i) {
            if (uploads[i].getStatus() == Upload.STATUS_IN_PROGRESS) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Determines if the <code>UploadState</code> is initialized.
     * 
     * @return true if the <code>UploadState</code> has been initialized
     */
    public boolean isInitialized() {
        return size != -1;
    }
    
    /**
     * Sets the progress of a specific {@link Upload}.
     * Reports upload progress to interested listeners. 
     * 
     * @param upload the {@link Upload}
     * @param bytesRead the number of bytes received
     */
    public void progress(Upload upload, long bytesRead) {
        ((UploadImpl) upload).setProgress(bytesRead);
        if (listeners == null) {
            return;
        }
        UploadProcessEvent e = new UploadProcessEvent(this, upload);
        UploadProcessListener[] listenerArray = new UploadProcessListener[listeners.size()];
        listeners.toArray(listenerArray);
        for (int i = 0; i < listenerArray.length; ++i) {
            listenerArray[i].uploadProgress(e);
        }
    }
    
    /**
     * Removes an {@link UploadProcessListener} from receiving notification of {@link UploadProcessEvent}s.
     * 
     * @param l the listener to remove.
     */
    public void removeProcessListener(UploadProcessListener l) {
        if (listeners == null) {
            return;
        }
        listeners.remove(l);
    }

    /**
     * Sets the status of all in-progress uploads to the specified value.
     * 
     * @param status the new status
     */
    public synchronized void setStatus(int status) {
        Upload[] uploads = getUploads();
        for (int i = 0; i < uploads.length; ++i) {
            if (uploads[i].getStatus() == Upload.STATUS_IN_PROGRESS) {
                ((UploadImpl) uploads[i]).setStatus(status);
            }
        }
    }
    
    /**
     * Notifies listeners that an {@link Upload} has started.
     * 
     * @param upload the {@link Upload}
     */
    public void start(Upload upload) {
        if (listeners == null) {
            return;
        }
        UploadProcessEvent e = new UploadProcessEvent(this, upload);
        UploadProcessListener[] listenerArray = new UploadProcessListener[listeners.size()];
        listeners.toArray(listenerArray);
        for (int i = 0; i < listenerArray.length; ++i) {
            listenerArray[i].uploadStart(e);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "UploadProcess: " + getProgress() + "/" + getSize();
    }
}
