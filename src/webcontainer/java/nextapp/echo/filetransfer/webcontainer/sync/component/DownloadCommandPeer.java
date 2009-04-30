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
 * 
 * @author sgodden
 */
public class DownloadCommandPeer extends AbstractCommandSynchronizePeer {

    /**
     * Mapping between download render identifiers (Strings) and <code>DownloadComand</code>s. 
     */
    private static final Map ID_TO_DOWNLOAD_MAP = Collections.synchronizedMap(new HashMap());

    private static final Service DOWNLOAD_SERVICE = JavaScriptService.forResource("Echo.Download",
            "nextapp/echo/filetransfer/webcontainer/resource/js/Download.js");

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
                if (download.isActive()) {
                    download.setActive(false);
                }
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