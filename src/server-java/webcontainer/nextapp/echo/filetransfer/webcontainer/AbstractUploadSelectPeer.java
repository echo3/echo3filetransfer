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
import nextapp.echo.filetransfer.app.AbstractUploadSelect;
import nextapp.echo.filetransfer.model.Upload;
import nextapp.echo.filetransfer.model.UploadProcess;
import nextapp.echo.filetransfer.receiver.UploadProcessManager;
import nextapp.echo.filetransfer.receiver.UploadProcessor;
import nextapp.echo.filetransfer.receiver.UploadProcessorFactory;
import nextapp.echo.webcontainer.AbstractComponentSynchronizePeer;
import nextapp.echo.webcontainer.Connection;
import nextapp.echo.webcontainer.ResourceRegistry;
import nextapp.echo.webcontainer.UserInstance;
import nextapp.echo.webcontainer.WebContainerServlet;

/**
 * Abstract base synchronization peer for upload components.
 */
public abstract class AbstractUploadSelectPeer extends AbstractComponentSynchronizePeer {
    
    /**
     * Additional parameter names sent to receiver servlet (used to specify component id).
     */
    private static final String[] RECEIVER_PARAMETERS = new String[]{ "cid" };
    
    static {
        WebContainerServlet.getServiceRegistry().add(UploadReceiverService.INSTANCE);
        ResourceRegistry resources = WebContainerServlet.getResourceRegistry();
        resources.addPackage("FileTransfer", "nextapp/echo/filetransfer/webcontainer/resource/");
    }
    
    /**
     * Default constructor.
     */
    public AbstractUploadSelectPeer() {
        super();
        addOutputProperty("receiver");
        addOutputProperty("monitor");
        addEvent(new EventPeer(AbstractUploadSelect.INPUT_UPLOAD_COMPLETE, AbstractUploadSelect.UPLOAD_LISTENERS_CHANGED_PROPERTY) {
            
            /**
             * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer.EventPeer#processEvent(
             *      nextapp.echo.app.util.Context, nextapp.echo.app.Component, java.lang.Object)
             */
            public void processEvent(Context context, Component component, Object eventData) {
                Connection conn = (Connection) context.get(Connection.class);
                String processId = (String) eventData;
                UploadProcess uploadProcess = UploadProcessManager.remove(conn.getRequest(), processId);
                Upload[] uploads = uploadProcess.getUploads();
                super.processEvent(context, component, uploads);
                uploadProcess.dispose();
            }

            /**
             * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer.EventPeer#hasListeners(
             *      nextapp.echo.app.util.Context, nextapp.echo.app.Component)
             */
            public boolean hasListeners(Context context, Component c) {
                return true;
            }
        });
        addEvent(new EventPeer(AbstractUploadSelect.INPUT_UPLOAD_SEND, AbstractUploadSelect.UPLOAD_LISTENERS_CHANGED_PROPERTY) {
            /**
             * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer.EventPeer#processEvent(
             *      nextapp.echo.app.util.Context, nextapp.echo.app.Component, java.lang.Object)
             */
            public void processEvent(Context context, Component component, Object eventData) {
                Connection conn = (Connection) context.get(Connection.class);
                super.processEvent(context, component, eventData);
            }

            /**
             * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer.EventPeer#hasListeners(
             *      nextapp.echo.app.util.Context, nextapp.echo.app.Component)
             */
            public boolean hasListeners(Context context, Component c) {
                return true;
            }
        });
    }

    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#getEventDataClass(java.lang.String)
     */
    public Class getEventDataClass(String eventType) {
        return String.class;
    }

    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#getOutputProperty(nextapp.echo.app.util.Context, 
     *      nextapp.echo.app.Component, java.lang.String, int)
     */
    public Object getOutputProperty(Context context, Component component,
            String propertyName, int propertyIndex) {
        if ("receiver".equals(propertyName)) {
            UserInstance userInstance = (UserInstance) context.get(UserInstance.class);
            return userInstance.getServiceUri(UploadReceiverService.INSTANCE, RECEIVER_PARAMETERS, 
                    new String[]{ userInstance.getClientRenderId(component)});
        } else if ("monitor".equals(propertyName)) {
            UserInstance userInstance = (UserInstance) context.get(UserInstance.class);
            return userInstance.getServiceUri(UploadMonitorService.INSTANCE);
        } else {
            return super.getOutputProperty(context, component, propertyName, propertyIndex);
        }
    }
    
    /**
     * Returns the {@link UploadProcessor} which should be used for the specified upload component
     * Default implementation retrieves value from {@link UploadProcessorFactory}.
     * This method is intended to be overridden when necessary.
     * 
     * @param component the component
     * @return the {@link UploadProcessor}
     */
    public UploadProcessor getUploadProcessor(AbstractUploadSelect component) {
        return UploadProcessorFactory.getUploadProcessor();
    }

    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#init(nextapp.echo.app.util.Context, 
     *      nextapp.echo.app.Component)
     */
    public void init(Context context, Component component) {
        super.init(context, component);
        UploadReceiverService.install();
    }
}