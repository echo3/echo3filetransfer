package nextapp.echo.filetransfer.webcontainer.sync.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nextapp.echo.app.Command;
import nextapp.echo.app.util.Context;
import nextapp.echo.filetransfer.app.DownloadCommand;
import nextapp.echo.filetransfer.webcontainer.service.DownloadService;
import nextapp.echo.webcontainer.AbstractCommandSynchronizePeer;
import nextapp.echo.webcontainer.ServerMessage;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.UserInstance;
import nextapp.echo.webcontainer.WebContainerServlet;
import nextapp.echo.webcontainer.service.JavaScriptService;

/**
 * Synchronize peer for {@link DownloadCommand}.
 * @author sgodden
 *
 */
public class DownloadCommandPeer extends AbstractCommandSynchronizePeer {

	private static final Map ID_TO_DOWNLOAD_MAP = Collections.synchronizedMap(new HashMap());

	private static final Service DOWNLOAD_SERVICE = JavaScriptService.forResource("Echo.Download",
			"nextapp/echo/filetransfer/webcontainer/resource/js/Download.js");

	static {
		DownloadService.install();
		WebContainerServlet.getServiceRegistry().add(DOWNLOAD_SERVICE);
	}

	public DownloadCommandPeer() {
		super();
		addProperty("uri", new AbstractCommandSynchronizePeer.PropertyPeer() {
			public Object getProperty(Context context, Command command) {
				DownloadCommand download = (DownloadCommand) command;
				if (download.isActive()) {
					download.setActive(false);
				}
				UserInstance userInstance = (UserInstance) context.get(UserInstance.class);
				String id = download.getRenderId();
				ID_TO_DOWNLOAD_MAP.put(id, download);
				String serviceUri = DownloadService.INSTANCE.createUri(userInstance, id);
				return serviceUri;
			}
		});
	}

	public void init(Context context) {
		super.init(context);
		ServerMessage serverMessage = (ServerMessage) context.get(ServerMessage.class);
		serverMessage.addLibrary(DOWNLOAD_SERVICE.getId());
	}

	public Class getCommandClass() {
		return DownloadCommand.class;
	}

	/**
	 * Accessor to internal Download Map for Download Service
	 * 
	 * @param id
	 * @return the requested Dounload object
	 */
	public static DownloadCommand getDownload(String id) {
		return (DownloadCommand) ID_TO_DOWNLOAD_MAP.get(id);
	}
}