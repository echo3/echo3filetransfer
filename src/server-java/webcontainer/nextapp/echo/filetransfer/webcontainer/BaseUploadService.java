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

package nextapp.echo.filetransfer.webcontainer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nextapp.echo.filetransfer.app.AbstractUploadSelect;
import nextapp.echo.webcontainer.Connection;
import nextapp.echo.webcontainer.ContentType;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.UserInstance;

/**
 * Base service for file upload operations.
 */
public abstract class BaseUploadService implements Service {

    public String getUrl(Connection conn, AbstractUploadSelect uploadSelect) {
        return conn.getUserInstance().getServiceUri(this,  new String[]{ "i" }, 
                new String[]{ conn.getUserInstance().getClientRenderId(uploadSelect)});
    }
    
    /**
     * Validates if the request contains a valid UploadSelect render id and index. 
     * 
     * @see Service#service(Connection)
     */
    public void service(Connection conn) throws IOException {
        UserInstance userInstance = conn.getUserInstance();
        if (userInstance == null) {
            serviceBadRequest(conn, "No user instance available.");
            return;
        }
        HttpServletRequest request = conn.getRequest();
        String processId = request.getParameter("pid");
        if (processId == null) {
            serviceBadRequest(conn, "Process id not specified.");
            return;
        }

        service(conn, processId);
    }
    
    /**
     * Performs the actual service of the request.
     * 
     * @param conn the <code>Connection</code>
     * @param processId the {@link nextapp.echo.filetransfer.model.UploadProcess} identifier
     * @throws IOException
     */
    public abstract void service(Connection conn, String processId) throws IOException;
    
    /**
     * Serves a bad request message.
     * 
     * @param conn the <code>Connection</code>
     * @param message the error message to send
     */
    protected static void serviceBadRequest(Connection conn, String message) {
        conn.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        conn.setContentType(ContentType.TEXT_PLAIN);
        conn.getWriter().write(message);
    }
}