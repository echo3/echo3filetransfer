/* 
 * This file is part of the Echo File Transfer Library.
 * Copyright (C) 2002-2007 NextApp, Inc.
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

import java.util.TooManyListenersException;

import nextapp.echo.app.Component;
import nextapp.echo.app.Extent;
import nextapp.echo.app.FillImage;
import nextapp.echo.filetransfer.app.event.UploadCancelEvent;
import nextapp.echo.filetransfer.app.event.UploadEvent;
import nextapp.echo.filetransfer.app.event.UploadFailEvent;
import nextapp.echo.filetransfer.app.event.UploadFinishEvent;
import nextapp.echo.filetransfer.app.event.UploadListener;
import nextapp.echo.filetransfer.app.event.UploadProgressEvent;
import nextapp.echo.filetransfer.app.event.UploadStartEvent;

/**
 * A component that allows users to upload files to the application from remote
 * clients.
 */
public class UploadSelect extends Component {

	public static final Extent DEFAULT_HEIGHT = new Extent(50);
	public static final int DEFAULT_PROGRESS_INTERVAL = 1000;
	public static final Extent DEFAULT_WIDTH = new Extent(275);
	
	public static final String PROPERTY_BROWSE_BUTTON_BACKGROUND_IMAGE = "browseButtonBackgroundImage";
	public static final String PROPERTY_BROWSE_BUTTON_HEIGHT = "browseButtonHeight";
	public static final String PROPERTY_BROWSE_BUTTON_ROLLOVER_BACKGROUND_IMAGE = "browseButtonRolloverBackgroundImage";
	public static final String PROPERTY_BROWSE_BUTTON_TEXT = "browseButtonText";
	public static final String PROPERTY_BROWSE_BUTTON_WIDTH = "browseButtonWidth";
	public static final String PROPERTY_FILE_SELECTOR_WIDTH = "fileSelectorWidth";
	public static final String PROPERTY_HEIGHT = "height"; 
	public static final String PROPERTY_PROGRESS_INTERVAL = "progressInterval"; 
	public static final String PROPERTY_QUEUE_ENABLED = "queueEnabled";
	public static final String PROPERTY_SEND_BUTTON_HEIGHT = "sendButtonHeight";
	public static final String PROPERTY_SEND_BUTTON_DISPLAYED = "sendButtonDisplayed";
	public static final String PROPERTY_SEND_BUTTON_TEXT = "sendButtonText";
	public static final String PROPERTY_SEND_BUTTON_WAIT_TEXT = "sendButtonWaitText";
	public static final String PROPERTY_SEND_BUTTON_WIDTH = "sendButtonWidth";
    public static final String PROPERTY_WIDTH = "width";
    
    public static final String UPLOAD_CANCELED_PROPERTY = "uploadCanceled"; 
    public static final String UPLOAD_LISTENER_CHANGED_PROPERTY = "uploadListener"; 
	
    private int[] canceledUploads;
    private UploadListener uploadListener;
    
    /**
     *
     */
    public UploadSelect() {
    	this.canceledUploads = new int[0];
	}
    
    /**
     * Adds an <code>UploadListener</code> to be notified of file uploads.
     * This listener is <strong>unicast</strong>, only one may be added.
     * 
     * @param l The listener to add.
     */
    public void addUploadListener(UploadListener l) 
    throws TooManyListenersException {
        if (uploadListener != null) {
            throw new TooManyListenersException();
        } else {
            uploadListener = l;
            firePropertyChange(UPLOAD_LISTENER_CHANGED_PROPERTY, null, l);
        }
    }

    /**
     * Cancels the upload with given index.
     * 
     * @param uploadIndex the index of the upload
     */
    public void cancelUpload(int uploadIndex) {
    	cancelUpload(uploadIndex, true);
    }
    
    /**
     * Cancels the upload with given index.
     * 
     * @param uploadIndex the index of the upload
     * @param notifyListener specify <code>true</code> to notify listener
     */
    public void cancelUpload(int uploadIndex, boolean notifyListener) {
    	int[] newCanceledUploads = new int[canceledUploads.length + 1];
    	System.arraycopy(canceledUploads, 0, newCanceledUploads, 0, canceledUploads.length);
    	newCanceledUploads[canceledUploads.length] = uploadIndex;
    	canceledUploads = newCanceledUploads;
    	
    	firePropertyChange(UPLOAD_CANCELED_PROPERTY, null, null);
    	
    	if (notifyListener) {
    		notifyListener(new UploadCancelEvent(this, uploadIndex));
    	}
    }
    
    /**
     * Returns the background image of the browse button.
     * 
     * @return the background image.
     */
    public FillImage getBrowseButtonBackgroundImage() {
        return (FillImage) get(PROPERTY_BROWSE_BUTTON_BACKGROUND_IMAGE);
    }
    
    /**
     * Returns the height of the browse button. 
     * 
     * @return the height.
     */
    public Extent getBrowseButtonHeight() {
    	return (Extent) get(PROPERTY_BROWSE_BUTTON_HEIGHT);
    }

    /**
     * Returns the background image of the browse button when the mouse cursor is inside
     * its bounds. 
     * 
     * @return the background image.
     */
    public FillImage getBrowseButtonRolloverBackgroundImage() {
    	return (FillImage) get(PROPERTY_BROWSE_BUTTON_ROLLOVER_BACKGROUND_IMAGE);
    }
    
    /**
     * Returns the text displayed in the browse button.
     * 
     * @return the text.
     */
    public Extent getBrowseButtonText() {
    	return (Extent) get(PROPERTY_BROWSE_BUTTON_TEXT);
    }
    
    /**
     * Returns the width of the browse button. 
     * 
     * @return the width.
     */
    public Extent getBrowseButtonWidth() {
    	return (Extent) get(PROPERTY_BROWSE_BUTTON_WIDTH);
    }
    
    /**
	 * Returns the indices of all canceled uploads.
	 * 
	 * @return an array containing the indices, if no uploads are canceled, an
	 *         empty array is returned.
	 */
    public int[] getCanceledUploads() {
    	return canceledUploads;
    }
    
    /**
	 * Returns the overall width of the file selector, including the browse
	 * button.
	 * 
	 * @return the width.
	 */
    public Extent getFileSelectorWidth() {
    	return (Extent)get(PROPERTY_FILE_SELECTOR_WIDTH);
    }

    /**
	 * Returns the overall height of the component. Defaults to <code>50</code>
	 * pixels.
	 * 
	 * @return the height.
	 */
    public Extent getHeight() {
        Extent value = (Extent)get(PROPERTY_HEIGHT);
		return value != null ? value : DEFAULT_HEIGHT;
    }
    
    /**
	 * Returns the interval between subsequent progress notifications. The
	 * default is <code>1000</code>ms.
	 * 
	 * @return the interval in milliseconds.
	 */
    public int getProgessInterval() {
    	Integer value = (Integer)get(PROPERTY_PROGRESS_INTERVAL);
		return value != null ? value.intValue() : DEFAULT_PROGRESS_INTERVAL;
    }
    
    /**
     * Returns the height of the send button. 
     * 
     * @return the height.
     */
    public Extent getSendButtonHeight() {
    	return (Extent) get(PROPERTY_SEND_BUTTON_HEIGHT);
    }
    
    /**
     * Returns the text displayed in the send button.
     * 
     * @return the text.
     */
    public String getSendButtonText() {
        return (String)get(PROPERTY_SEND_BUTTON_TEXT);
    }
    
    /**
     * Returns the text displayed in the send button when an upload is in progress.
     * 
     * @return the text.
     */
    public String getSendButtonWaitText() {
        return (String)get(PROPERTY_SEND_BUTTON_WAIT_TEXT);
    }
    
    /**
     * Returns the width of the send button. 
     * 
     * @return the width.
     */
    public Extent getSendButtonWidth() {
    	return (Extent) get(PROPERTY_SEND_BUTTON_WIDTH);
    }

    /**
     * Returns the upload listener that will be notified of file uploads.
     *
     * @return the listener.
     */
    public UploadListener getUploadListener() {
        return uploadListener;
    }
    
    /**
	 * Returns the overall width of the component. Defaults to <code>275</code>
	 * pixels.
     *
     * @return the width.
     */
    public Extent getWidth() {
        Extent value = (Extent)get(PROPERTY_WIDTH);
		return value != null ? value : DEFAULT_WIDTH;
    }
    
    /**
	 * Returns whether queueing multiple file uploads is enabled. By default,
	 * queueing is disabled.
	 * 
	 * @return <code>true</code> if queuing is enabled.
	 * 
	 * @see #setQueueEnabled(boolean)
	 */
    public boolean isQueueEnabled() {
    	Boolean enabled = (Boolean)get(PROPERTY_QUEUE_ENABLED);
    	return enabled != null ? enabled.booleanValue() : false;
    }

    /**
	 * Returns whether the send button is displayed. By default, the button is
	 * displayed.
	 * 
	 * @return <code>true</code> if the button is displayed.
	 * 
	 * @see #setSendButtonDisplayed(boolean)
	 */
    public boolean isSendButtonDisplayed() {
        Boolean displayed = (Boolean)get(PROPERTY_SEND_BUTTON_DISPLAYED);
        return displayed != null ? displayed.booleanValue() : true;
    }
    
    /**
     * Determines if the upload with given index is canceled.
     * 
     * @param uploadIndex the index of the upload
     * 
     * @return <code>true</code> if canceled.
     */
    public boolean isUploadCanceled(int uploadIndex) {
    	for (int i = 0; i < canceledUploads.length; i++) {
			if (canceledUploads[i] == uploadIndex) {
				return true;
			}
		}
    	return false;
    }

    /**
     * Notifies the listener that the given event has occurred.
     * 
     * @param e the event
     */
    public void notifyListener(UploadEvent e) {
    	if (uploadListener == null) {
    		return;
    	}
    	if (e instanceof UploadCancelEvent) {
    		uploadListener.fileUploadCanceled((UploadCancelEvent) e);
    	} else if (e instanceof UploadFailEvent) {
    		uploadListener.fileUploadFailed((UploadFailEvent) e);
    	} else if (e instanceof UploadFinishEvent) {
    		uploadListener.fileUploadFinished((UploadFinishEvent) e);
    	} else if (e instanceof UploadProgressEvent) {
    		uploadListener.fileUploadProgressed((UploadProgressEvent) e);
    	} else if (e instanceof UploadStartEvent) {
    		uploadListener.fileUploadStarted((UploadStartEvent) e);
    	}
    }
    
    /**
     * Removes a (the) <code>UploadListener</code> from this 
     * <code>UploadSelect</code>.
     * 
     * @param l The listener to remove.
     */
    public void removeUploadListener(UploadListener l) {
        if (l.equals(uploadListener)) {
            uploadListener = null;
        }
        firePropertyChange(UPLOAD_LISTENER_CHANGED_PROPERTY, l, null);
    }
    
    /**
     * Sets the background image of the browse button.
     * 
     * @param newValue the new background image
     */
    public void setBrowseButtonBackgroundImage(FillImage newValue) {
        set(PROPERTY_BROWSE_BUTTON_BACKGROUND_IMAGE, newValue);
    }
    
    /**
	 * Sets the height of the browse button. Values must be in fixed (i.e., not
	 * percent) units.
	 * 
	 * @param newValue the new height
	 */
    public void setBrowseButtonHeight(Extent newValue) {
    	set(PROPERTY_BROWSE_BUTTON_HEIGHT, newValue);
    }
    
    /**
     * Sets the background image of the browse button when the mouse cursor is inside
     * its bounds.
     * 
     * @param newValue the new background image
     */
    public void setBrowseButtonRolloverBackgroundImage(FillImage newValue) {
    	set(PROPERTY_BROWSE_BUTTON_ROLLOVER_BACKGROUND_IMAGE, newValue);
    }
    
    /**
     * Sets the text displayed in the browse button.
     * 
     * @param newValue the new text
     */
    public void setBrowseButtonText(String newValue) {
    	set(PROPERTY_BROWSE_BUTTON_TEXT, newValue);
    }
    
    /**
	 * Sets the width of the browse button. Values must be in fixed (i.e., not
	 * percent) units.
	 * 
	 * @param newValue the new width
	 */
    public void setBrowseButtonWidth(Extent newValue) {
    	set(PROPERTY_BROWSE_BUTTON_WIDTH, newValue);
    }

    /**
	 * Sets the overall width of the file selector, including the browse button.
	 * Values must be in fixed (i.e., not percent) units.
	 * 
	 * @param newValue the new width value
	 */
    public void setFileSelectorWidth(Extent newValue) {
    	set(PROPERTY_FILE_SELECTOR_WIDTH, newValue);
    }
    
    /**
	 * Sets the overall height of the component. Values must be in fixed (i.e.,
	 * not percent) units.
	 * 
	 * @param newValue the new height value
	 */
    public void setHeight(Extent newValue) {
        set(PROPERTY_HEIGHT, newValue);
    }

    /**
     * Sets the interval between subsequent progress notifications.
     *
     * @param newValue the new interval in milliseconds
     */
    public void setProgessInterval(int newValue) {
    	if (newValue <= 0) {
    		throw new IllegalArgumentException();
    	}
    	set(PROPERTY_PROGRESS_INTERVAL, new Integer(newValue));
    }
    
    /**
	 * Sets whether queueing multiple file uploads is enabled.
	 * 
	 * @param newValue <code>true</code> if queuing is enabled
	 */
    public void setQueueEnabled(boolean newValue) {
    	set(PROPERTY_QUEUE_ENABLED, Boolean.valueOf(newValue));
    }

    /**
	 * Sets whether the send button is displayed.
	 * 
	 * @param newValue <code>true</code> if the send button is displayed
	 */
    public void setSendButtonDisplayed(boolean newValue) {
        set(PROPERTY_SEND_BUTTON_DISPLAYED, Boolean.valueOf(newValue));
    }
    
    /**
	 * Sets the height of the send button. Values must be in fixed (i.e., not
	 * percent) units.
	 * 
	 * @param newValue the new height
	 */
    public void setSendButtonHeight(Extent newValue) {
    	set(PROPERTY_SEND_BUTTON_HEIGHT, newValue);
    }

    /**
     * Sets the text displayed in the send button.
     * 
     * @param newValue the new text
     */
    public void setSendButtonText(String newValue) {
        set(PROPERTY_SEND_BUTTON_TEXT, newValue);
    }

    /**
	 * Sets the text displayed in the send button when an upload is in progress.
	 * 
	 * @param newValue the new text
	 */
    public void setSendButtonWaitText(String newValue) {
        set(PROPERTY_SEND_BUTTON_WAIT_TEXT, newValue);
    }
    
    /**
     * Sets the width of the send button. Values must be in fixed (i.e., not
	 * percent) units.
     * 
     * @param newValue the new width
     */
    public void setSendButtonWidth(Extent newValue) {
    	set(PROPERTY_SEND_BUTTON_WIDTH, newValue);
    }

    /**
	 * Sets the overall width of the component. Values must be in fixed (i.e.,
	 * not percent) units.
	 * 
	 * @param newValue the new width value
	 */
    public void setWidth(Extent newValue) {
    	set(PROPERTY_WIDTH, newValue);
    }
}