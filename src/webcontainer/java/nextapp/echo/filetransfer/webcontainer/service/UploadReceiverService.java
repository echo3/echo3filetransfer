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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import nextapp.echo.filetransfer.app.UploadProgress;
import nextapp.echo.filetransfer.app.UploadSelect;
import nextapp.echo.filetransfer.app.event.UploadFailEvent;
import nextapp.echo.filetransfer.app.event.UploadStartEvent;
import nextapp.echo.filetransfer.webcontainer.UploadProviderFactory;
import nextapp.echo.filetransfer.webcontainer.sync.component.UploadSelectPeer;
import nextapp.echo.webcontainer.Connection;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.WebContainerServlet;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

/**
 * Processes a file upload HTTP request from the client.
 */
public class UploadReceiverService extends BaseUploadService {

    private static final Service INSTANCE = new UploadReceiverService();

    static {
        WebContainerServlet.getServiceRegistry().add(INSTANCE);
    }

    private UploadReceiverService() {
        super();
    }

    /**
     * Installs this service.
     */
    public static void install() {
        WebContainerServlet.setMultipartRequestWrapper(UploadProviderFactory.getUploadProvider());
    }

    /**
     * @see Service#getId()
     */
    public String getId() {
        return "EchoFileTransfer.UploadReceiverService";
    }

    /**
     * @see Service#getVersion()
     */
    public int getVersion() {
        return DO_NOT_CACHE;
    }

    /**
     * @see BaseUploadService#service(Connection, UploadSelect, int)
     */
    public void service(Connection conn, UploadSelect uploadSelect, int uploadIndex) throws IOException {
        HttpServletRequest request = conn.getRequest();
        if (!ServletFileUpload.isMultipartContent(request)) {
            serviceBadRequest(conn, "Request must contain multipart content.");
            return;
        }
        if (uploadSelect.getUploadListener() == null) {
            serviceBadRequest(conn, "UploadSelect does not have a listener.");
            return;
        }
        String contentLengthHeader = request.getHeader("Content-Length");
        long contentLength;
        if (contentLengthHeader != null) {
            contentLength = Math.max(Long.parseLong(contentLengthHeader), -1);
        } else {
            contentLength = -1;
        }

        UploadRenderState renderState = UploadSelectPeer.getRenderState(uploadSelect, conn.getUserInstance());
        UploadProgress progress = new UploadProgress(contentLength);
        renderState.setProgress(uploadIndex, progress);
        uploadSelect.notifyListener(new UploadStartEvent(uploadSelect, uploadIndex, FilenameUtils.getName(request.getParameter("name"))));

        renderState.uploadStarted(uploadIndex);
        try {
            UploadProviderFactory.getUploadProvider().handleUpload(conn, uploadSelect, uploadIndex, progress);
        } catch (Exception e) {
            if (uploadSelect.isUploadCanceled(uploadIndex)) {
                // we don't care
                return;
            }
            uploadSelect.notifyListener(new UploadFailEvent(uploadSelect, uploadIndex, e));
        } finally {
            renderState.uploadEnded(uploadIndex);
        }
    }
}