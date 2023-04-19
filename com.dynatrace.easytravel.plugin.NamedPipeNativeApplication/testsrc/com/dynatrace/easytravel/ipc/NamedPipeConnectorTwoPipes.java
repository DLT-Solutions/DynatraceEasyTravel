package com.dynatrace.easytravel.ipc;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/*
 * See http://v01ver-howto.blogspot.com/2010/04/howto-use-named-pipes-to-communicate.html
 */
public class NamedPipeConnectorTwoPipes {

    private static Logger log = LoggerFactory.make();

	public static String sendAndReceiveTwoPipes(String pipeName, String toSend) throws IOException {
		// Connect to the pipe
		ReadPipe input = new ReadPipe("\\\\.\\pipe\\" + pipeName);
		WritePipe output = new WritePipe("\\\\.\\pipe\\" + pipeName);

		if(!output.connect(10000)) {
			throw new IOException("Could not open pipe for writing");
		}

		output.write(toSend);

		output.flush();

		if (!input.connect(10000)) {
			throw new IOException("Could not open pipe for reading");
		}

		String echoResponse = input.readLine();   //JLT-13736

		output.close();
		input.close();

		log.debug("Response: " + echoResponse);

		return echoResponse;
	}

	public static String sendAndReceive(String pipeName, String toSend) throws IOException {
		// Connect to the pipe
		RandomAccessFile pipe = new RandomAccessFile(
				"\\\\.\\pipe\\" + pipeName, "rw");

		// write to pipe
		pipe.write(toSend.getBytes("UTF-8"));

		// read response
		//String echoResponse = readLineUnicode(pipe);
		String echoResponse = pipe.readLine();
		log.debug("Response: " + echoResponse);

		pipe.close();

		return echoResponse;
	}

	public static String readLineUnicode(RandomAccessFile pipe) throws IOException {
		StringBuffer input = new StringBuffer();
		byte [] b = new byte[] { -1, -1 };
		boolean eol = false;

		while (!eol) {
			eol = !readTwoBytes(pipe, b);

			if(!eol) {
				String s = new String(b);
				assert(s.length() == 1);

				if(s.equals("\n")) {
					eol = true;
				} else if (s.equals("\r")) {
			    	eol = true;
			    	long cur = pipe.getFilePointer();
			    	eol = !readTwoBytes(pipe, b);
			    	s = new String(b);
			    	if(s.equals("\n")) {
			    	    pipe.seek(cur);
			    	}
				} else {
					input.append(s);
				}
		    }
		}

		if ((eol) && (input.length() == 0)) {
		    return null;
		}
		return input.toString();
	}

	private static boolean readTwoBytes(RandomAccessFile pipe, byte[] b) throws IOException {
		int i = (byte)pipe.read();
		if(i == -1) {
			return false;
		} else {
			b[0] = (byte)i;
			i = pipe.read();
			if(i == -1) {
				return false;
			}
		}
		b[1] = (byte)i;

		return true;
	}
}
