package com.dynatrace.easytravel;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class Transfer {

	private Transfer() {
	}

	/**
	 * Transfering InputStream to OutputStream
	 *
	 * @param inputStream
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	public static long performTransfer(InputStream inputStream, OutputStream outputStream) throws IOException {
		final int BUF_SIZE = 0x1000; // 4K

		byte[] buf = new byte[BUF_SIZE];
		long total = 0;
		while (true) {
			int r = inputStream.read(buf);
			if (r == -1) {
				break;
			}
			outputStream.write(buf, 0, r);
			total += r;
		}
		return total;
	}





}
