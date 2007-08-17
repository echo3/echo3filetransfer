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

package nextapp.echo.filetransfer.webcontainer.service;

import java.util.HashMap;
import java.util.Map;

import nextapp.echo.filetransfer.app.UploadProgress;
import nextapp.echo.webcontainer.RenderState;

/**
 * <code>RenderState</code> implementation for <code>UploadSelect</code> components.
 * <p>
 * This class is thread-safe.
 */
public class UploadRenderState implements RenderState {
    private int maxUploadIndex;
	private final Map progress;
    private int[] ended;
    
	/**
	 * Creates a new <code>UploadRenderState</code>.
	 */
	public UploadRenderState() {
	    this.maxUploadIndex = -1;
		this.progress = new HashMap();
        this.ended = new int[0];
	}
    
    /**
     * Gets the maximum upload index currently in use. This index can be used to prevent duplicate indices when performing
     * client-side refreshes.
     * 
     * @return the index or <code>-1</code> if no uploads indices are present.
     */
    public int getMaxUploadIndex() {
        return maxUploadIndex;
    }
	
    /**
     * Announces that an upload with given index has started.
     * 
     * @param uploadIndex the upload index
     */
    public void uploadStarted(int uploadIndex) {
        maxUploadIndex = uploadIndex;
    }
    
    /**
     * Determines whether the upload with the specified index has ended.
     * 
     * @param uploadIndex the upload index
     * @return <code>true</code> if the upload has ended.
     */
    public boolean isUploadEnded(int uploadIndex) {
        synchronized (ended) {
            for (int i = 0; i < ended.length; i++) {
                if (ended[i] == uploadIndex) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Announces that an upload with given index has ended.
     * 
     * @param uploadIndex
     */
    public void uploadEnded(int uploadIndex) {
        synchronized (ended) {
            int[] newEnded = new int[ended.length + 1];
            System.arraycopy(ended, 0, newEnded, 0, ended.length);
            newEnded[ended.length] = uploadIndex;
            ended = newEnded;
        }
    }
    
    /**
     * Gets the progress for the given upload index.
     * 
     * @param uploadIndex the upload index
     * @return the progress if available, <code>null</code> otherwise.
     */
    public UploadProgress getProgress(int uploadIndex) {
        return (UploadProgress) progress.get(new Integer(uploadIndex));
    }

	/**
	 * Sets the progress for the specified upload index.
	 * 
	 * @param uploadIndex the upload index
	 * @param progress the progress
	 */
	public void setProgress(int uploadIndex, UploadProgress progress) {
		Integer key = new Integer(uploadIndex);
		synchronized (this.progress) {
			if (this.progress.containsKey(key)) {
				throw new IllegalStateException();
			}
			this.progress.put(key, progress);
		}
	}
}