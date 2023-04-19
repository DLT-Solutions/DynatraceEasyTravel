package com.dynatrace.easytravel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility methods for copying data from one stream to another
 * 
 * @author cwat-rpilz
 *
 */
final class IO {

	/**
	 * Copies the contents of an input stream to a file.<br />
	 * If the file already exists it will be overwritten.
	 * 
	 * @param in the {@link InputStream} to read data from
	 * @param dest the {@link File} to store the read data into
	 * @throws IOException
	 * 
	 * @author cwat-rpilz
	 */
	static void copy(InputStream in, File dest) throws IOException {
		if (dest == null) {
			throw new NullPointerException("destination file must not be null");
		}
		dest = dest.getAbsoluteFile().getCanonicalFile();
		FileOutputStream fos = new FileOutputStream(dest);
		try {
			copy(in, fos);
		} finally {
			fos.close();
		}
	}
	
	/**
	 * Copies the contents of an input stream to an output stream
	 *  
	 * @param in the {@link InputStream} to read data from
	 * @param out the {@link OutputStream} to send the data read to
	 * @throws IOException in case either read or write operations fail at some point
	 * 
	 * @author cwat-rpilz
	 */
	static void copy(InputStream in, OutputStream out) throws IOException {
		if (in == null || out == null) {
			throw new NullPointerException("neither input nor output stream must be null");
		}
		byte[] buf = new byte[4*1024*1024];
		int i = 0;
		while((i = in.read(buf)) != -1) {
			out.write(buf, 0, i);
		}
	}
}
