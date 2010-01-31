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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A representation of an uploaded file.
 */
public interface Upload {
    
    /** Status flag indicating the upload is in progress. */
    public static final int STATUS_IN_PROGRESS = 0; 
    
    /** Status flag indicating the upload has completed. */
    public static final int STATUS_COMPLETE = 1;
    
    /** Status flag indicating the upload has been canceled. */
    public static final int STATUS_CANCELED = 2;
    
    /** Status flag indicating a general input/output error has occurred. */
    public static final int STATUS_ERROR_IO = 3;
    
    /** Status flag indicating the upload is too large. */
    public static final int STATUS_ERROR_OVERSIZE = 4;
    
    /**
     * Marks the upload as canceled.
     */
    public void cancel();
    
    /**
     * Returns the content type of the uploaded file.
     *
     * @return the content type of the uploaded file
     */
    public String getContentType();
    
    /**
     * Returns the base file name of the file.
     *
     * @return the name of the file, contains no path information
     */
    public String getFileName();
    
    /**
     * Returns the temporary file in which the data is stored.
     * May return null if the file has not been written to disk in a temporary store.
     * 
     * @return the <code>File</code>, if available
     */
    public File getFile();
    
    /**
     * Returns an input stream containing the uploaded file. Implementations
     * should take care of closing this stream, since it may hold on to
     * resources.  This method may only be invoked once.
     * 
     * @return an input stream containing the uploaded file
     */
    public InputStream getInputStream() 
    throws IOException;
    
    /**
     * Returns the size of the uploaded file, in bytes.
     *
     * @return the size
     */
    public long getSize();
    
    /**
     * Returns the file status, described the <code>STATUS_XXX</code> flags defined in this object.
     * 
     * @return the file status
     */
    public int getStatus();
    
    /**
     * Returns the number of bytes which have been uploaded.
     * 
     * @return the number of bytes uploaded
     */
    public long getProgress();
}
