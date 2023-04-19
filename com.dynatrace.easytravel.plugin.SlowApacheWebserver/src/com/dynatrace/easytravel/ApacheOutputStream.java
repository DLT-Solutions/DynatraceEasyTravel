package com.dynatrace.easytravel;

import java.io.IOException;
import java.io.OutputStream;

public class ApacheOutputStream extends OutputStream {

	private OutputStream out;

	public ApacheOutputStream() {
		this.out = System.out;
	}

	public ApacheOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void close() throws IOException {
		out.close();
	}
}
