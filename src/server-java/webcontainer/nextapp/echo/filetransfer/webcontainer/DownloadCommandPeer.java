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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nextapp.echo.app.Command;
import nextapp.echo.app.util.Context;
import nextapp.echo.filetransfer.app.DownloadCommand;
import nextapp.echo.webcontainer.AbstractCommandSynchronizePeer;
import nextapp.echo.webcontainer.ServerMessage;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.UserInstance;
import nextapp.echo.webcontainer.WebContainerServlet;
import nextapp.echo.webcontainer.service.JavaScriptService;

/**
 * Synchronize peer for {@link DownloadCommand}.
 * 
 * @author sgodden
 */
public class DownloadCommandPeer extends AbstractCommandSynchronizePeer {

    /**
     * Mapping between download render identifiers (Strings) and <code>DownloadComand</code>s. 
     */
    private static final Map ID_TO_DOWNLOAD_MAP = Collections.synchronizedMap(new HashMap());

    private static final Service DOWNLOAD_SERVICE = JavaScriptService.forResource("Echo.Download",
            "nextapp/echo/filetransfer/webcontainer/resource/Sync.DownloadCommand.js");

    static {
        DownloadService.install();
        WebContainerServlet.getServiceRegistry().add(DOWNLOAD_SERVICE);
    }

    /**
     * Creates a new <code>DownlaodCommandPeer</code>.
     */
    public DownloadCommandPeer() {
        super();
        addProperty("uri", new AbstractCommandSynchronizePeer.PropertyPeer() {
            public Object getProperty(Context context, Command command) {
                DownloadCommand download = (DownloadCommand) command;
                UserInstance userInstance = (UserInstance) context.get(UserInstance.class);
                String id = download.getRenderId();
                ID_TO_DOWNLOAD_MAP.put(id, download);
                String serviceUri = DownloadService.getInstance().createUri(userInstance, id);
                return serviceUri;
            }
        });
    }

    /**
     * @see nextapp.echo.webcontainer.CommandSynchronizePeer#getCommandClass()
     */
    public Class getCommandClass() {
        return DownloadCommand.class;
    }

    /**
     * Returns the {@link DownloadCommand} having the passed id, and removes
     * it from the internal map.
     * <p>
     * This means that a particular download command cannot be re-used. A new
     * download command must be created every time, e.g. each time your download
     * button is clicked.
     * </p>
     * This is necessary to prevent memory leaks.
     * 
     * @param id the download id.
     * @return the {@link DownloadCommand} instance.
     */
    public static DownloadCommand getAndRemoveDownload(String id) {
        return (DownloadCommand) ID_TO_DOWNLOAD_MAP.remove(id);
    }

    /**
     * @see nextapp.echo.webcontainer.AbstractCommandSynchronizePeer#init(nextapp.echo.app.util.Context)
     */
    public void init(Context context) {
        super.init(context);
        ServerMessage serverMessage = (ServerMessage) context.get(ServerMessage.class);
        serverMessage.addLibrary(DOWNLOAD_SERVICE.getId());
    }
}