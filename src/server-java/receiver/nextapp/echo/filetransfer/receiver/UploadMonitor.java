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
import javax.servlet.http.HttpServletRequest;
import nextapp.echo.filetransfer.model.UploadProcess;

/**
 * Handler for upload monitoring requests.  Used by servlet and Echo-service based implementations.
 */
public class UploadMonitor {
    
    /**
     * Generates an XML response string.
     * 
     * @param contentXml the contents of the document (inside the "m" element).
     */
    private static String createResponse(String contentXml) {
        return "<?xml version=\"1.0\"?><m>" + contentXml + "</m>";
    }
    
    /**
     * Processes an  upload monitor/control request.
     * 
     * @param request the incoming HTTP request
     * @return an XML document, as a String, to be rendered in the response
     * @throws IOException
     */
    public static String processRequest(HttpServletRequest request) 
    throws IOException {
        String processId = request.getParameter("pid");
        UploadProcess uploadProcess = UploadProcessManager.get(request, processId, false);
        if (uploadProcess == null) {
            return createResponse("<s v=\"unknownpid\"/>");
        }
        String command = request.getParameter("command");
        if ("cancel".equals(command)) {
            uploadProcess.cancel();
        }
        
        if (uploadProcess.isCanceled()) {
            return createResponse("<s v=\"cancel\"/>");
        } else if (uploadProcess.isComplete()) {
            return createResponse("<s v=\"complete\"/>");
        } else {
            return createResponse("<s p=\"" + uploadProcess.getProgress() + "/" + uploadProcess.getSize() + "\"/>");        
        }
    }
    
    /** Non-instantiable class. */
    private UploadMonitor() { }
}
