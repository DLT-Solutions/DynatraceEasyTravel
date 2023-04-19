package com.dynatrace.easytravel;

import java.io.*;

public class ApacheInputStream extends InputStream {

	private InputStream is;

	public ApacheInputStream() {
		this.is = System.in;
	}

	public ApacheInputStream(InputStream is) {
		this.is = is;
	}

	@Override
	public int read() throws IOException {
		return is.read();
	}

	@Override
	public void close() throws IOException {
		is.close();
	}
}
