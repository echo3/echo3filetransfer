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
public class DownloadCommand implements Command, RenderIdSupport, Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private boolean active;
    private DownloadProvider provider;

    /**
     * Constructs a new download command.
     */
    public DownloadCommand() {
        this(null, false);
    }

    /**
     * Constructs a new download command, whose data will
     * be taken from the passed {@link DownloadProvider}.
     * <p>
     * FIXME - the active parameter seems pointless and unused - 
     * check with Tod to see if it can be removed.
     * </p>
     * @param provider the provider from which to get the data.
     * @param active whether the download is active.
     */
    public DownloadCommand(DownloadProvider provider, boolean active) {
        super();
        this.provider = provider;
        this.active = active;
    }

    /**
     * Returns the download provider.
     * @return the download provider.
     */
    public DownloadProvider getProvider() {
        return provider;
    }

    /**
     * Returns whether the download is active.
     * @return whether the download is active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether the download is active.
     * @param newValue whether the download is active.
     */
    public void setActive(boolean newValue) {
        this.active = newValue;
    }

    /**
     * Sets the download provider from which to get the data.
     * @param newValue the download provider from which to get the data.
     */
    public void setProvider(DownloadProvider newValue) {
        this.provider = newValue;
    }

    /**
     * Returns the render id.
     * @return the render id.
     */
    public String getRenderId() {
        if (id == null) {
            id = ApplicationInstance.generateSystemId();
        }
        return id;
    }
}