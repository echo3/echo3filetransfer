package nextapp.echo.filetransfer.app;

import java.io.Serializable;

/**
 * An abstract implementation of {@link DownloadProvider}.
 * @author sgodden
 */
public abstract class AbstractDownloadProvider implements DownloadProvider, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Returns a <code>null</code> file name.
	 * @return <code>null</code>
	 */
	public String getFileName() {
		return null;
	}

	/**
	 * Returns -1 as the file data length.
	 * @return -1
	 */
	public long getSize() {
		return -1;
	}
}