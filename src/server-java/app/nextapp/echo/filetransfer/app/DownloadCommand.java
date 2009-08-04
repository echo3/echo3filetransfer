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

package nextapp.echo.filetransfer.app;

import java.io.Serializable;

import nextapp.echo.app.ApplicationInstance;
import nextapp.echo.app.Command;
import nextapp.echo.app.RenderIdSupport;

/**
 * A command instructing the client to download file data from
 * a specified {@link DownloadProvider}.
 * <p>
 * <em>Note</em> - each instance of {@link DownloadCommand} may
 * only be used once.  This is necessary in order to avoid
 * memory leaks.  You cannot enqueue an instance of this command
 * more than once.  It will be gone the second time, and will
 * cause a server error.
 * </p>
 * <p>
 * Therefore, you must create a new instance of this class each
 * time your button is clicked to trigger the download.
 * </p>
 * @author sgodden
 *
 */
public class DownloadCommand 
implements Command, RenderIdSupport, Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private DownloadProvider provider;

    /**
     * Constructs a new download command.
     */
    public DownloadCommand() {
        this(null);
    }

    /**
     * Constructs a new download command, whose data will
     * be taken from the passed {@link DownloadProvider}.
     * 
     * @param provider the {@link DownloadProvider}
     */
    public DownloadCommand(DownloadProvider provider) {
        super();
        this.provider = provider;
    }

    /**
     * Returns the download provider.
     * 
     * @return the download provider
     */
    public DownloadProvider getProvider() {
        return provider;
    }

    /**
     * Returns the render id.
     * @return the render id
     */
    public String getRenderId() {
        if (id == null) {
            id = ApplicationInstance.generateSystemId();
        }
        return id;
    }

    /**
     * Sets the {@link DownloadProvider} from which to get the data.
     * 
     * @param newValue the {@link DownloadProvider} from which to get the data
     */
    public void setProvider(DownloadProvider newValue) {
        this.provider = newValue;
    }
}