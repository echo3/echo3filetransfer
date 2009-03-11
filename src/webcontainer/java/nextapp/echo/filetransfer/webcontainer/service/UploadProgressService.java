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

import nextapp.echo.filetransfer.app.UploadProgress;
import nextapp.echo.filetransfer.app.UploadSelect;
import nextapp.echo.filetransfer.app.event.UploadProgressEvent;
import nextapp.echo.filetransfer.webcontainer.sync.component.UploadSelectPeer;
import nextapp.echo.webcontainer.Connection;
import nextapp.echo.webcontainer.ContentType;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.WebContainerServlet;

/**
 * Provides information about file upload progress.
 */
public class UploadProgressService extends BaseUploadService {

    private static final Service INSTANCE = new UploadProgressService();

    static {
        WebContainerServlet.getServiceRegistry().add(INSTANCE);
    }

    private UploadProgressService() {
        super();
    }

    /**
     * Installs this service.
     */
    public static void install() {
        // Do nothing, simply ensure static directives are executed.
    }
    
    /**
     * @see Service#getId()
     */
    public String getId() {
        return "EchoFileTransfer.UploadProgressService";
    }

    /**
     * @see Service#getVersion()
     */
    public int getVersion() {
        return DO_NOT_CACHE;
    }

    public void service(Connection conn, UploadSelect uploadSelect, int uploadIndex)
    throws IOException {
        UploadRenderState renderState = UploadSelectPeer.getRenderState(uploadSelect, conn.getUserInstance());
        UploadProgress progress = renderState.getProgress(uploadIndex);

        StringBuffer buff = new StringBuffer("<?xml version=\"1.0\"?>");
        buff.append("<p>");
        if (progress != null && progress.getBytesRead() > 0) {
            if (!renderState.isUploadEnded(uploadIndex)) {
                uploadSelect.notifyListener(new UploadProgressEvent(uploadSelect, uploadIndex, progress));
            }
            buff.append("<r>").append(progress.getBytesRead()).append("</r>");
            buff.append("<cl>").append(progress.getContentLength()).append("</cl>");
            buff.append("<pc>").append(progress.getPercentCompleted()).append("</pc>");
            buff.append("<tr>").append(progress.getTransferRate()).append("</tr>");
            buff.append("<tl>").append(progress.getEstimatedTimeLeft()).append("</tl>");
        }
        buff.append("</p>");

        conn.setContentType(ContentType.TEXT_HTML);
        conn.getWriter().write(buff.toString());
    }
}