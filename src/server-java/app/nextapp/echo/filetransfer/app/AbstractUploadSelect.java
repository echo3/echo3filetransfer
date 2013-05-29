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

package nextapp.echo.filetransfer.app;

import java.util.EventListener;
import java.util.TooManyListenersException;

import nextapp.echo.app.Border;
import nextapp.echo.app.Component;
import nextapp.echo.app.Insets;
import nextapp.echo.filetransfer.app.event.UploadEvent;
import nextapp.echo.filetransfer.app.event.UploadListener;
import nextapp.echo.filetransfer.app.event.UploadProgressListener;
import nextapp.echo.filetransfer.model.Upload;
import nextapp.echo.filetransfer.model.UploadProcess;
import nextapp.echo.filetransfer.model.event.UploadProcessEvent;
import nextapp.echo.filetransfer.model.event.UploadProcessListener;

/**
 * Abstract base class for file upload components.
 */
public abstract class AbstractUploadSelect extends Component {
    
    public static final String UPLOAD_LISTENERS_CHANGED_PROPERTY = "uploadListeners";
    public static final String UPLOAD_PROGRESS_LISTENERS_CHANGED_PROPERTY = "uploadProgressListeners";
    public static final String INPUT_UPLOAD_COMPLETE = "uploadComplete";

    public static final String PROPERTY_INSETS = "insets";
    public static final String PROPERTY_BORDER = "border";

    //FIXME Must release upload process listeners as soon as possible.
    private UploadProcessListener uploadProcessListener = new UploadProcessListener() {

        public void uploadCancel(UploadProcessEvent e) {
            doUploadCancel(e.getUpload());
        }

        public void uploadComplete(UploadProcessEvent e) {
            // Do nothing.
        }

        public void uploadProgress(UploadProcessEvent e) {
            doUploadProgress(e.getUpload());
        }

        public void uploadStart(UploadProcessEvent e) {
            doUploadStart(e.getUpload());
        }
    };
    
    private UploadProcess uploadProcess;

    /**
     * Adds an <code>UploadListener</code> to be notified of file uploads.
     * 
     * @param l the listener to add
     */
    public void addUploadListener(UploadListener l) {
        getEventListenerList().addListener(UploadListener.class, l);
        firePropertyChange(UPLOAD_LISTENERS_CHANGED_PROPERTY, null, l);
    }
    
    /**
     * Adds an <code>UploadProgressListener</code> to be notified of file upload progress.
     * <p>
     * <strong>WARNING</strong>: Upload progress events are NOT fired from within user interface threads.
     * Thus implementations MAY NOT update the state of the application's user interface or component hierarchy.
     * An {@link UploadListener} should instead be used for such purposes. 
     * 
     * @param l the listener to add
     */
    public void addUploadProgressListener(UploadProgressListener l) {
        getEventListenerList().addListener(UploadProgressListener.class, l);
        firePropertyChange(UPLOAD_PROGRESS_LISTENERS_CHANGED_PROPERTY, null, l);
    }
    
    /**
     * Cancels an in progress upload.
     */
    public void cancel() {
        UploadProcess uploadProcess = this.uploadProcess;
        if (uploadProcess != null) {
            uploadProcess.cancel();
        }
    }
    
    /**
     * Provides notification that the specified upload has been canceled.
     * 
     * @param upload the upload
     */
    public void doUploadCancel(Upload upload) {
        if (!hasUploadProgressListeners()) {
            return;
        }
        UploadEvent e = new UploadEvent(this, upload);
        EventListener[] listeners = getEventListenerList().getListeners(UploadProgressListener.class);
        for (int i = 0; i < listeners.length; ++i){
            ((UploadProgressListener) listeners[i]).uploadCancel(e);
        }
    }
    
    /**
     * Provides notification that the specified upload has completed.
     * 
     * @param upload the upload
     */
    public void doUploadComplete(Upload upload) {
        UploadEvent e = new UploadEvent(this, upload);
        EventListener[] listeners = getEventListenerList().getListeners(UploadListener.class);
        for (int i = 0; i < listeners.length; ++i){
            ((UploadListener) listeners[i]).uploadComplete(e);
        }
    }
    
    /**
     * Provides notification that the specified upload has progressed.
     * 
     * @param upload the upload
     */
    public void doUploadProgress(Upload upload) {
        if (!hasUploadProgressListeners()) {
            return;
        }
        UploadEvent e = new UploadEvent(this, upload);
        EventListener[] listeners = getEventListenerList().getListeners(UploadProgressListener.class);
        for (int i = 0; i < listeners.length; ++i){
            ((UploadProgressListener) listeners[i]).uploadProgress(e);
        }
    }
    
    /**
     * Provides notification that the specified upload has started.
     * 
     * @param upload the upload
     */
    public void doUploadStart(Upload upload) {
        if (!hasUploadProgressListeners()) {
            return;
        }
        UploadEvent e = new UploadEvent(this, upload);
        EventListener[] listeners = getEventListenerList().getListeners(UploadProgressListener.class);
        for (int i = 0; i < listeners.length; ++i){
            ((UploadProgressListener) listeners[i]).uploadStart(e);
        }
    }
    
    /**
     * Determines if any <code>UploadListener</code>s are currently registered.
     * 
     * @return true if any <code>UploadListener</code>s are registered
     */
    public boolean hasUploadListeners() {
        return !hasEventListenerList() || getEventListenerList().getListenerCount(UploadListener.class) > 0;
    }

    /**
     * Determines if any <code>UploadProgressListener</code>s are currently registered.
     * 
     * @return true if any <code>UploadProgressListener</code>s are registered
     */
    public boolean hasUploadProgressListeners() {
        return !hasEventListenerList() || getEventListenerList().getListenerCount(UploadProgressListener.class) > 0;
    }
    
    /**
     * @see nextapp.echo.app.Component#processInput(java.lang.String, java.lang.Object)
     */
    public void processInput(String inputName, Object inputValue) {
        if (INPUT_UPLOAD_COMPLETE.equals(inputName)) {
            Upload[] uploads = (Upload[]) inputValue;
            for (int i = 0; i < uploads.length; ++i) {
                doUploadComplete(uploads[i]);
            }
        }
    }
    
    /**
     * Removes an <code>UploadProgressListener</code> to be notified of file upload progress.
     * <p>
     * <strong>WARNING</strong>: Upload progress events are NOT fired from within user interface threads.
     * Thus implementations MAY NOT update the state of the application's user interface or component hierarchy.
     * An {@link UploadListener} should instead be used for such purposes. 
     * 
     * @param l the listener to add
     */
    public void removeUploadProgressListener(UploadProgressListener l) {
        if (hasEventListenerList()) {
            getEventListenerList().removeListener(UploadProgressListener.class, l);
        }
        firePropertyChange(UPLOAD_PROGRESS_LISTENERS_CHANGED_PROPERTY, l, null);
    }
    
    /**
     * Removes an <code>UploadListener</code> from being notified of file uploads.
     * 
     * @param l the listener to add
     */
    public void removeUploadListener(UploadListener l) 
    throws TooManyListenersException {
        if (hasEventListenerList()) {
            getEventListenerList().removeListener(UploadListener.class, l);
        }
        firePropertyChange(UPLOAD_LISTENERS_CHANGED_PROPERTY, l, null);
    }
    
    /**
     * Sets the active {@link UploadProcess} representing incoming uploads.
     * This operation does not and may not change the UI state, it may be invoked outside of a user interface thread.
     * 
     * @param uploadProcess the new {@link UploadProcess}
     */
    public void setUploadProcess(UploadProcess uploadProcess) {
        if (this.uploadProcess != null) {
            this.uploadProcess.removeProcessListener(uploadProcessListener);
        }
        this.uploadProcess = uploadProcess;
        if (this.uploadProcess != null) {
            this.uploadProcess.addProcessListener(uploadProcessListener);
        }
    }

    /**
     * Returns the default inset between the border and cells of the component.
     *
     * @return the inset
     */
    public Insets getInsets() {
        return (Insets) get(PROPERTY_INSETS);
    }

    /**
     * Sets the inset between the border and cells of the component.
     *
     * @param newValue the new inset
     */
    public void setInsets(Insets newValue) {
        set(PROPERTY_INSETS, newValue);
    }

    /**
     * Sets the <code>Border</code> that encloses the component.
     *
     * @param newValue the new border
     */
    public void setBorder(Border newValue) {
        set(PROPERTY_BORDER, newValue);
    }

    /**
     * Returns the <code>Border</code> that encloses the component.
     *
     * @return the border
     */
    public Border getBorder() {
        return (Border) get(PROPERTY_BORDER);
    }

}
