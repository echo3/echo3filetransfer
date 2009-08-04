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

package nextapp.echo.filetransfer.app.event;

import java.util.EventListener;

/**
 * A listener for receiving upload processing events. The methods of this listener are NOT invoked from within a user interface
 * thread. Thus, any attempt to modify the user interface component hierarchy from within an implementation method will fail with an
 * exception. If you require such capabilities, register a <code>UploadListener</code> rather than an
 * <code>UploadProcessListener</code>.
 */
public interface UploadProgressListener extends EventListener {
    
    /**
     * Provides notification that an upload has started.
     * <strong>WARNING:</strong> Do not perform user interface modifications from within this listener.
     * 
     * @param e the {@link UploadEvent}
     */
    public void uploadStart(UploadEvent e);
    
    /**
     * Provides notification that an upload has progressed.
     * <strong>WARNING:</strong> Do not perform user interface modifications from within this listener.
     * 
     * @param e the {@link UploadEvent}
     */
    public void uploadProgress(UploadEvent e);
    
    /**
     * Provides notification that an upload has been canceled.
     * <strong>WARNING:</strong> Do not perform user interface modifications from within this listener.
     * 
     * @param e the {@link UploadEvent}
     */
    public void uploadCancel(UploadEvent e);
}
