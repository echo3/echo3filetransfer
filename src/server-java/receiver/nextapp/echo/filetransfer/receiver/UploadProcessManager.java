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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import nextapp.echo.filetransfer.model.UploadProcess;

/**
 * Manages active {@link UploadProcessor} instances.
 */
public class UploadProcessManager {
    
    public static final String SESSION_KEY = UploadProcessManager.class.getName();
    
    /**
     * Creates or retrieves an {@link UploadProcessor} with the specified identifier.
     * The <code>remove()</code> method must be invoked on any created <code>UploadProcessor</code>.
     * 
     * @param request the incoming HTTP request whose session may contain the <code>UploadProcessor</code>
     * @param id the unique identifier of the upload processor
     * @param create flag indicating whether a new <code>UploadProcessor</code> should be created and stored in the event that one
     *        does not currently exist with the specified identifier (a value of false will return null when an
     *        <code>UploadProcessor</code> cannot be found)
     */
    public synchronized static UploadProcess get(HttpServletRequest request, String id, boolean create) {
        Map uploadProcessMap = (Map) request.getSession(true).getAttribute(SESSION_KEY);
        if (uploadProcessMap == null) {
            if (!create) {
                return null;
            }
            uploadProcessMap = new HashMap();
            request.getSession().setAttribute(SESSION_KEY, uploadProcessMap);
        }
        UploadProcess uploadProcess = (UploadProcess) uploadProcessMap.get(id);
        if (uploadProcess == null) {
            if (!create) {
                return null;
            }
            uploadProcess = new UploadProcess(id);
            uploadProcessMap.put(id, uploadProcess);
        }
        return uploadProcess;
    }
    
    /**
     * Removes/disposes of an {@link UploadProcessor} with the specified identifier.
     * 
     * @param request the incoming HTTP request whose session may contain the <code>UploadProcessor</code>
     * @param id the unique identifier of the upload processor
     */
    public synchronized static UploadProcess remove(HttpServletRequest request, String id) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Map uploadProcessMap = (Map) session.getAttribute(SESSION_KEY);
        if (uploadProcessMap == null) {
            return null;
        }
        UploadProcess uploadProcess = (UploadProcess) uploadProcessMap.remove(id);
        if (uploadProcessMap.size() == 0) {
            session.removeAttribute(SESSION_KEY);
        }
        return uploadProcess;
    }
}
