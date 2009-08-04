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

import nextapp.echo.app.Component;
import nextapp.echo.app.util.Context;
import nextapp.echo.filetransfer.app.MultipleUploadSelect;
import nextapp.echo.filetransfer.app.UploadSelect;
import nextapp.echo.webcontainer.Connection;
import nextapp.echo.webcontainer.ContentType;
import nextapp.echo.webcontainer.ResourceRegistry;
import nextapp.echo.webcontainer.ServerMessage;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.WebContainerServlet;
import nextapp.echo.webcontainer.service.JavaScriptService;

/**
 * Synchronization peer for {@link MultipleUploadSelect} components.
 */
public class MultipleUploadSelectPeer extends AbstractUploadSelectPeer {

    private static final Service SERVICE = JavaScriptService.forResources("FileTransfer.MultipleUpload",
            new String[] { 
                "nextapp/echo/filetransfer/webcontainer/resource/Application.MultipleUploadSelect.js", 
                "nextapp/echo/filetransfer/webcontainer/resource/Sync.MultipleUploadSelect.js", 
                "nextapp/echo/filetransfer/webcontainer/resource/SWFUpload.js"
            });
    
    static {
        WebContainerServlet.getServiceRegistry().add(SERVICE);
        ResourceRegistry resources = WebContainerServlet.getResourceRegistry();
        resources.add("FileTransfer", "swfupload.swf", ContentType.APPLICATION_FLASH);
    }
    
    /**
     * Default constructor.
     */
    public MultipleUploadSelectPeer() {
        super();
        addRequiredComponentClass(UploadSelect.class);
    }
    
    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#getComponentClass()
     */
    public Class getComponentClass() {
        return MultipleUploadSelect.class;
    }
    
    /**
     * @see nextapp.echo.webcontainer.ComponentSynchronizePeer#getClientComponentType(boolean)
     */
    public String getClientComponentType(boolean mode) {
        return "FileTransfer.MultipleUploadSelect";
    }
    
    /**
     * @see nextapp.echo.filetransfer.webcontainer.AbstractUploadSelectPeer#getOutputProperty(nextapp.echo.app.util.Context, 
     *      nextapp.echo.app.Component, java.lang.String, int)
     */
    public Object getOutputProperty(Context context, Component component,
                String propertyName, int propertyIndex) {
        if ("receiver".equals(propertyName)) {
            String url = (String) super.getOutputProperty(context, component, propertyName, propertyIndex);
            return injectSessionId(context, url);
        } else {
            return super.getOutputProperty(context, component, propertyName, propertyIndex);
        }
    }

    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#init(
     *      nextapp.echo.app.util.Context, nextapp.echo.app.Component)
     */
    public void init(Context context, Component component) {
        super.init(context, component);
        ServerMessage serverMessage = (ServerMessage) context.get(ServerMessage.class);
        serverMessage.addLibrary(SERVICE.getId());
    }

    /**
     * Injects the session id into a URL, to work around Flash bug of not sending cookies (including session cookie).
     * 
     * @param context the rendering context
     * @param url the URL to modify
     * @return the modified URL
     */
    public String injectSessionId(Context context, String url) {
        Connection connection = (Connection) context.get(Connection.class);
            int questionIndex = url.indexOf("?");
            return url.substring(0, questionIndex) + ";jsessionid=" + connection.getRequest().getSession().getId() + 
                    url.substring(questionIndex);
    }
}
