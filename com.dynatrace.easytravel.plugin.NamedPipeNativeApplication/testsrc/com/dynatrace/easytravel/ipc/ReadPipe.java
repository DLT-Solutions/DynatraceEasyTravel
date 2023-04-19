package com.dynatrace.easytravel.ipc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author magdalena.schatzl
 *
 */
public class ReadPipe {

	private final String path;
	private FileInputStream input;

	public ReadPipe(String path) {
		this.path = path;
	}

	/**
	 * connect to pipe for reading, try until pipe exists
	 *
	 * @return
	 */
	public boolean connect(int timeoutInMillis) {
		if (input != null)
			return true;
		File f = new File(path);

		long before = System.currentTimeMillis();
		long end = before + timeoutInMillis;

		while (input == null && System.currentTimeMillis() < end) {
			try {
				input = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		return input != null;
	}

	/**
	 * try once to connect to pipe for reading (for checking, if Reflector
	 * is already running)
	 *
	 * @return true, if connected successfully
	 */
	public boolean connectSingle() {
		if (input != null)
			return true;
		File f = new File(path);
		if (input == null) {
			try {
				input = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Reads one line from the inputstream and closes the connection.
	 *
	 * @return string read
	 */
	public String readLine() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));

		String s = "";
		try {
			s = reader.readLine();
//			reader.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
//		input = null;
		return s;
	}
	
	public void close() throws IOException {
		input.close();
	}
}
