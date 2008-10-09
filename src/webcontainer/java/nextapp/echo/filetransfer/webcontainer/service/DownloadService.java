package nextapp.echo.filetransfer.webcontainer.service;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;


import nextapp.echo.filetransfer.app.DownloadCommand;
import nextapp.echo.filetransfer.app.DownloadProvider;
import nextapp.echo.filetransfer.webcontainer.sync.component.DownloadCommandPeer;
import nextapp.echo.webcontainer.Connection;
import nextapp.echo.webcontainer.ContentType;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.UserInstance;
import nextapp.echo.webcontainer.WebContainerServlet;

/**
 * Services requests to download files.
 * @see DownloadCommand
 * @author sgodden
 *
 */
public class DownloadService implements Service {

	private static final String SERVICE_ID = "Echo.RemoteClient.CommandExec.Download";
	private static final String PARAMETER_DOWNLOAD_UID = "duid";
	private static final String[] URL_PARAMETERS = new String[] { PARAMETER_DOWNLOAD_UID };
	public static final DownloadService INSTANCE = new DownloadService();

	/**
	 * Installs the service in the registry.
	 */
	public static void install() {
		WebContainerServlet.getServiceRegistry().add(INSTANCE);
	}

	/** 
	 * Don't instantiate externally 
	 */
	private DownloadService() {
	}

	/**
	 * Creates a URI from which to download the file.
	 * @param userInstance the user instance of the user downloading.
	 * @param downloadId the id of the download command.
	 * @return the download URI.
	 */
	public String createUri(UserInstance userInstance, String downloadId) {
		return userInstance.getServiceUri(this, URL_PARAMETERS, new String[] { downloadId });
	}

	/**
	 * Returns the service id.
	 */
	public String getId() {
		return SERVICE_ID;
	}

	/**
	 * Returns the service version.
	 */
	public int getVersion() {
		return DO_NOT_CACHE;
	}

	/**
	 * Handles a service request.
	 * @param conn the connection.
	 */
	public void service(Connection conn) throws IOException {
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
		DownloadCommand download = DownloadCommandPeer.getDownload(downloadId);

		if (download == null) {
			serviceBadRequest(conn, "Download UID is not valid.");
			return;
		}
		service(conn, download);
	}

	/**
	 * Internal processing to handle the download request.
	 * @param conn the connection.
	 * @param download the download command.
	 * @throws IOException
	 */
	private void service(Connection conn, DownloadCommand download) throws IOException {
		OutputStream out = conn.getOutputStream();
		DownloadProvider provider = download.getProvider();
		HttpServletResponse response = conn.getResponse();

		if (provider.getFileName() == null) {
			response.setHeader("Content-Disposition", "attachment");
		} else {
			response.setHeader("Content-Disposition", "attachment; filename=\"" + provider.getFileName() + "\"");
		}
		if (provider.getSize() > 0) {
			response.setIntHeader("Content-Length", provider.getSize());
		}
		String contentType = provider.getContentType();
		if (contentType == null) {
			response.setContentType("application/octet-stream");
		} else {
			response.setContentType(provider.getContentType());
		}
		provider.writeFile(out);
	}

	/**
	 * Sets the response status indicating that a bad request
	 * was made to this service.
	 * @param conn the connection.
	 * @param message the error message.
	 */
	private void serviceBadRequest(Connection conn, String message) {
		conn.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
		conn.setContentType(ContentType.TEXT_PLAIN);
		conn.getWriter().write(message);
	}
}