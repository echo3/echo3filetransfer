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
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import nextapp.echo.filetransfer.app.DownloadCommand;
import nextapp.echo.filetransfer.app.DownloadProvider;
import nextapp.echo.webcontainer.Connection;
import nextapp.echo.webcontainer.ContentType;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.UserInstance;
import nextapp.echo.webcontainer.WebContainerServlet;

/**
 * Service to provide downloaded files. 
 * 
 * @see DownloadCommand
 * @author sgodden
 */
public class DownloadService
implements Service {

    private static final String SERVICE_ID = "Echo.RemoteClient.CommandExec.Download";
    private static final String PARAMETER_DOWNLOAD_UID = "duid";
    private static final String[] URL_PARAMETERS = new String[] { PARAMETER_DOWNLOAD_UID };
    private static final DownloadService INSTANCE = new DownloadService();

    /**
     * Installs the service in the registry.
     */
    public static void install() {
        WebContainerServlet.getServiceRegistry().add(INSTANCE);
    }

    /**
     * Default constructor, not externally instantiable.
     */
    private DownloadService() { }

    /**
     * Creates a URI from which to download the file.
     * 
     * @param userInstance the relevant {@link UserInstance}
     * @param downloadId the id of the download command
     * @return the download URI
     */
    public String createUri(UserInstance userInstance, String downloadId) {
        return userInstance.getServiceUri(this, URL_PARAMETERS, new String[] { downloadId });
    }

    /**
     * @see nextapp.echo.webcontainer.Service#getId()
     */
    public String getId() {
        return SERVICE_ID;
    }

    /**
     * @see nextapp.echo.webcontainer.Service#getVersion()
     */
    public int getVersion() {
        return DO_NOT_CACHE;
    }

    /**
     * @see nextapp.echo.webcontainer.Service#service(nextapp.echo.webcontainer.Connection)
     */
    public void service(Connection conn)
            throws IOException {
        UserInstance userInstance = (UserInstance) conn.getUserInstance();
        if (userInstance == null) {
            serviceBadRequest(conn, "No container available.");
            return;
        }
        String downloadId = conn.getRequest().getParameter(PARAMETER_DOWNLOAD_UID);
        if (downloadId == null) {
            serviceBadRequest(conn, "Download UID not specified.");
            return;
        }
        DownloadCommand download = DownloadCommandPeer.getAndRemoveDownload(downloadId);

        if (download == null) {
            serviceBadRequest(conn, "Download UID is not valid.");
            return;
        }
        service(conn, download);
    }

    /**
     * Internal processing to handle the download request.
     * 
     * @param conn the connection.
     * @param download the download command.
     * @throws IOException
     */
    private void service(Connection conn, DownloadCommand download)
    throws IOException {
        OutputStream out = conn.getOutputStream();
        DownloadProvider provider = download.getProvider();
        HttpServletResponse response = conn.getResponse();

        if (provider.getFileName() == null) {
            response.setHeader("Content-Disposition", "attachment");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + provider.getFileName() + "\"");
        }
        if (provider.getSize() > 0) {
            response.setHeader("Content-Length", String.valueOf(provider.getSize()));
        }
        String contentType = provider.getContentType();
        if (contentType == null) {
            response.setContentType("application/octet-stream");
        } else {
            response.setContentType(provider.getContentType());
        }
        response.setHeader("Cache-Control", "");
        response.setHeader("Pragma", "");
        provider.writeFile(out);
    }

    /**
     * Sets the response status indicating that a bad request was made to this service.
     * 
     * @param conn the connection.
     * @param message the error message.
     */
    private void serviceBadRequest(Connection conn, String message) {
        conn.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        conn.setContentType(ContentType.TEXT_PLAIN);
        conn.getWriter().write(message);
    }

    /**
     * Returns an instance for public use.
     * 
     * @return an instance for public use.
     */
    public static final DownloadService getInstance() {
        return INSTANCE;
    }
}