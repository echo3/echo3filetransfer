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

package nextapp.echo.filetransfer.webcontainer.impl;

import java.util.Iterator;

import nextapp.echo.filetransfer.app.UploadProgress;
import nextapp.echo.filetransfer.app.UploadSelect;
import nextapp.echo.filetransfer.app.UploadSizeLimitExceededException;
import nextapp.echo.filetransfer.app.event.UploadFailEvent;
import nextapp.echo.filetransfer.app.event.UploadFinishEvent;
import nextapp.echo.filetransfer.webcontainer.UploadSPI;
import nextapp.echo.webcontainer.Connection;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

/**
 * <code>UploadSPI</code> implementation that uses the Jakarta Commons FileUpload library.
 * <p>
 * See http://jakarta.apache.org/commons/fileupload for details.
 */
public class JakartaCommonsFileUploadProvider extends AbstractFileUploadProvider {

    /**
     * @see UploadSPI#handleUpload(Connection, UploadSelect, int, UploadProgress)
     */
    public void handleUpload(Connection conn, UploadSelect uploadSelect, int uploadIndex, UploadProgress progress) throws Exception {
        DiskFileItemFactory itemFactory = new DiskFileItemFactory();
        itemFactory.setRepository(getDiskCacheLocation());
        itemFactory.setSizeThreshold(getMemoryCacheThreshold());

        ServletFileUpload upload = new ServletFileUpload(itemFactory);
        upload.setProgressListener(new UploadProgressListener(progress));
        if (getFileUploadSizeLimit() != NO_SIZE_LIMIT) {
            upload.setSizeMax(getFileUploadSizeLimit());
        }

        try {
            Iterator iter = upload.parseRequest(conn.getRequest()).iterator();
            if (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (!uploadSelect.isUploadCanceled(uploadIndex)) {
                    uploadSelect.notifyListener(new UploadFinishEvent(uploadSelect, uploadIndex, FilenameUtils.getName(item
                            .getName()), item.getInputStream(), item.getSize(), item.getContentType()));
                }
            } else {
                uploadSelect.notifyListener(new UploadFailEvent(uploadSelect, uploadIndex));
            }
        } catch (SizeLimitExceededException e) {
            uploadSelect.notifyListener(new UploadFailEvent(uploadSelect, uploadIndex, new UploadSizeLimitExceededException(e)));
        } catch (FileSizeLimitExceededException e) {
            uploadSelect.notifyListener(new UploadFailEvent(uploadSelect, uploadIndex, new UploadSizeLimitExceededException(e)));
        }
    }

    private static final class UploadProgressListener implements ProgressListener {
        private final UploadProgress progress;

        private UploadProgressListener(UploadProgress progress) {
            this.progress = progress;
        }

        public void update(long pBytesRead, long pContentLength, int pItems) {
            progress.setBytesRead(pBytesRead);
        }
    }
}