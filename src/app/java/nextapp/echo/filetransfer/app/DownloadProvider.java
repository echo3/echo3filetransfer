package nextapp.echo.filetransfer.app;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface to be implemented by the class providing
 * the data to be downloaded.
 * @author sgodden
 *
 */
public interface DownloadProvider {

	/**
	 * Returns the content type, for example "text/plain".
	 * @return the content type.
	 */
	public String getContentType();

	/**
	 * Returns the file name, for example "my-file.txt".
	 * @return the file name.
	 */
	public String getFileName();

	/**
	 * Returns the size of the data to be downloaded.
	 * @return the size of the data to be downloaded.
	 */
	public int getSize();

	/**
	 * Writes the file data to the output stream.
	 * @param out the output stream to which the file data must be written.
	 * @throws IOException
	 */
	public void writeFile(OutputStream out) throws IOException;
}