/**
 *
 */
package com.dynatrace.easytravel.ipc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class WritePipe {

	private final String path;
	private FileOutputStream output;

	public WritePipe(String path) {
		this.path = path;
	}

	/**
	 * connect to pipe for writing, try until pipe exists
	 *
	 * @return
	 */
	public boolean connect(int timeoutInMillis) {
		if (output != null)
			return true;
		File f = new File(path);

		long before = System.currentTimeMillis();
		long end = before + timeoutInMillis;

		while (output == null && System.currentTimeMillis() < end) {
			try {
				output = new FileOutputStream(f);
			} catch (FileNotFoundException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		return output != null;
	}

	/**
	 * try once to connect to pipe for writing (for checking, if Reflector
	 * is already running)
	 *
	 * @return true, if connected successfully
	 */
	public boolean connectSingle() {
		if (output != null)
			return true;
		File f = new File(path);
		if (output == null) {
			try {
				output = new FileOutputStream(f);
			} catch (FileNotFoundException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Writes the string to the outputstream and then closes the stream.
	 *
	 * @return string read
	 */
	public void write(String message) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(output));
			writer.write(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//output = null;
	}
	
	public void flush() throws IOException {
		output.flush();
	}
	
	public void close() throws IOException {
		output.close();
	}
}
