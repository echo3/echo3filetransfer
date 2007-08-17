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

package nextapp.echo.filetransfer.app.event;

import java.io.InputStream;

import nextapp.echo.filetransfer.app.UploadSelect;

/**
 * An event indicating that a file upload has finished.
 */
public class UploadFinishEvent extends UploadEvent {
    private final String contentType;
    private final String fileName;
    private final InputStream inputStream;
    private final long size;

	/**
	 * Creates a new <code>UploadFinishEvent</code>.
	 * 
     * @param source the source of the event
     * @param index the index of the upload
     * @param fileName the name of the file, may not contain path information
	 * @param inputStream an input stream containing the uploaded file
	 * @param size the size of the uploaded file, in bytes
	 * @param contentType the content type of the uploaded file
	 */
	public UploadFinishEvent(UploadSelect source, int index, String fileName, InputStream inputStream, long size, String contentType) {
		super(source, index);
		this.fileName = fileName;
		this.inputStream = inputStream;
		this.size = size;
		this.contentType = contentType;
	}
    
    /**
     * Returns the content type of the uploaded file.
     *
     * @return The content type of the uploaded file.
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Returns the base file name of the file.
     *
     * @return the name of the file, contains no path information.
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
	 * Returns an input stream containing the uploaded file. Implementations
	 * should take care of closing this stream, since it may hold on to
	 * resources.
	 * 
	 * @return An input stream containing the uploaded file.
	 */
    public InputStream getInputStream() {
        return inputStream;
    }
    
    /**
     * Returns the size of the uploaded file, in bytes.
     *
     * @return the size.
     */
    public long getSize() {
        return size;
    }
}