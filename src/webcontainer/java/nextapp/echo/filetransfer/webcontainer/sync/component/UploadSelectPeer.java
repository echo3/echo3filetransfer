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

package nextapp.echo.filetransfer.webcontainer.sync.component;

import nextapp.echo.app.Component;
import nextapp.echo.app.util.Context;
import nextapp.echo.filetransfer.app.UploadSelect;
import nextapp.echo.filetransfer.webcontainer.service.CommonService;
import nextapp.echo.filetransfer.webcontainer.service.UploadProgressService;
import nextapp.echo.filetransfer.webcontainer.service.UploadReceiverService;
import nextapp.echo.filetransfer.webcontainer.service.UploadRenderState;
import nextapp.echo.webcontainer.AbstractComponentSynchronizePeer;
import nextapp.echo.webcontainer.ComponentSynchronizePeer;
import nextapp.echo.webcontainer.ServerMessage;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.UserInstance;
import nextapp.echo.webcontainer.WebContainerServlet;
import nextapp.echo.webcontainer.service.JavaScriptService;

/**
 * Synchronization peer for <code>UploadSelect</code>s.
 */
public class UploadSelectPeer extends AbstractComponentSynchronizePeer {

    private static final String PROPERTY_UPLOAD_INDEX = "uploadIndex";
    
    private static final Service UPLOAD_SELECT_SERVICE = JavaScriptService.forResources("EchoFileTransfer.UploadSelect",
            new String[] {  "nextapp/echo/filetransfer/webcontainer/resource/js/Application.UploadSelect.js",  
                            "nextapp/echo/filetransfer/webcontainer/resource/js/Render.UploadSelect.js"});
    
    static {
        UploadProgressService.install();
        WebContainerServlet.getServiceRegistry().add(UPLOAD_SELECT_SERVICE);
    }
    
    /**
     *
     */
    public UploadSelectPeer() {
		super();
		addOutputProperty(UploadSelect.UPLOAD_CANCELED_PROPERTY);
		addOutputProperty(PROPERTY_UPLOAD_INDEX);
	}
    
    /**
     * @see ComponentSynchronizePeer#getComponentClass()
     */
    public Class getComponentClass() {
        return UploadSelect.class;
    }
    
    /**
     * @see ComponentSynchronizePeer#getClientComponentType(boolean)
     */
    public String getClientComponentType(boolean shortType) {
        return "FileTransfer.UploadSelect";
    }
    
    /**
     * @see ComponentSynchronizePeer#init(Context, Component)
     */
    public void init(Context context, Component component) {
        super.init(context, component);
        UploadReceiverService.install();
        ServerMessage serverMessage = (ServerMessage) context.get(ServerMessage.class);
        serverMessage.addLibrary(CommonService.INSTANCE.getId());
        serverMessage.addLibrary(UPLOAD_SELECT_SERVICE.getId());
    }
    
    /**
     * @see ComponentSynchronizePeer#getOutputProperty(Context, Component, String, int)
     */
    public Object getOutputProperty(Context context, Component component, String propertyName, int propertyIndex) {
    	if (UploadSelect.UPLOAD_CANCELED_PROPERTY.equals(propertyName)) {
    		int[] canceledUploads = ((UploadSelect)component).getCanceledUploads();
    		StringBuffer buffer = new StringBuffer();
    		for (int i = 0; i < canceledUploads.length; i++) {
    			if (buffer.length() > 0) {
    				buffer.append(",");
    			}
				buffer.append(canceledUploads[i]);
    		}
    		return buffer.toString();
    	} else if (PROPERTY_UPLOAD_INDEX.equals(propertyName)) {
            UploadSelect uploadSelect = (UploadSelect) component;
            UserInstance userInstance = (UserInstance) context.get(UserInstance.class);
            UploadRenderState renderState = getRenderState(uploadSelect, userInstance);
            return new Integer(renderState.getMaxUploadIndex());
        }
    	return super.getOutputProperty(context, component, propertyName, propertyIndex);
    }
    
    /**
	 * Gets the render state for the given component. Synchronization is handled
	 * internally.
	 * 
	 * @param uploadSelect
	 * @param userInstance
	 * @return the render state, never <code>null</code>.
	 */
    public static UploadRenderState getRenderState(UploadSelect uploadSelect, UserInstance userInstance) {
    	UploadRenderState renderState;
        synchronized (uploadSelect) {
			renderState = (UploadRenderState)userInstance.getRenderState(uploadSelect);
			if (renderState == null) {
				renderState = new UploadRenderState();
				userInstance.setRenderState(uploadSelect, renderState);
			}
		}
        return renderState;
    }
}