/* 
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2008 NextApp, Inc.
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

package nextapp.echo.filetransfer.testapp.testscreen;

import java.util.TooManyListenersException;

import nextapp.echo.app.Extent;
import nextapp.echo.app.Column;
import nextapp.echo.app.SplitPane;
import nextapp.echo.app.event.ActionEvent;
import nextapp.echo.app.event.ActionListener;
import nextapp.echo.filetransfer.app.UploadSelect;
import nextapp.echo.filetransfer.app.event.UploadCancelEvent;
import nextapp.echo.filetransfer.app.event.UploadFailEvent;
import nextapp.echo.filetransfer.app.event.UploadFinishEvent;
import nextapp.echo.filetransfer.app.event.UploadListener;
import nextapp.echo.filetransfer.app.event.UploadProgressEvent;
import nextapp.echo.filetransfer.app.event.UploadStartEvent;
import nextapp.echo.filetransfer.testapp.ButtonColumn;
import nextapp.echo.filetransfer.testapp.FTLTestApp;
import nextapp.echo.filetransfer.testapp.StyleUtil;
/**
 * A test for handling of long-running server-interactions.
 */
public class UploadSelectTest extends SplitPane {
    
    UploadSelect uploadSelect;
    
    public UploadSelectTest() {
        super(SplitPane.ORIENTATION_HORIZONTAL, new Extent(250, Extent.PX));
        setStyleName("DefaultResizable");

        ButtonColumn controlsColumn = new ButtonColumn();
        controlsColumn.setStyleName("TestControlsColumn");
        add(controlsColumn);
        
        controlsColumn.addButton("Set foreground", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadSelect.setForeground(StyleUtil.randomColor());
            }
        });

        controlsColumn.addButton("Set background", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadSelect.setBackground(StyleUtil.randomColor());
            }
        });

        Column testColumn = new Column();
        add(testColumn);
        
        uploadSelect = new UploadSelect();
        try {
            uploadSelect.addUploadListener(new UploadListener(){

                public void fileUploadStarted(UploadStartEvent e) {
                    System.err.println("fileUploadStarted");
                    System.err.println(FTLTestApp.getApp());
                }
            
                public void fileUploadProgressed(UploadProgressEvent e) {
                    System.err.println("fileUploadProgressed");
                    System.err.println(FTLTestApp.getApp());
                }
            
                public void fileUploadFinished(UploadFinishEvent e) {
                    System.err.println("fileUploadFinished");
                    System.err.println(FTLTestApp.getApp());
                }
            
                public void fileUploadFailed(UploadFailEvent e) {
                    System.err.println("fileUploadFailed");
                    System.err.println(FTLTestApp.getApp());
                }
            
                public void fileUploadCanceled(UploadCancelEvent e) {
                    System.err.println("fileUploadCanceled");
                    System.err.println(FTLTestApp.getApp());
                }
            });
        } catch (TooManyListenersException ex) {
            throw new RuntimeException(ex);
        }
        testColumn.add(uploadSelect);
    }
}
