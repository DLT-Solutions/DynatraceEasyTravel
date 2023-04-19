package com.dynatrace.easytravel.plugins;

import com.dynatrace.easytravel.ApacheInputStream;
import com.dynatrace.easytravel.Transfer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

public class SlowDownFilterTest {

	private static String inputString = "one, two, three";

	private final static int BUF_SIZE = 0x1000; // 4k

	private static InputStream apacheInputStream;
	private static ByteArrayOutputStream apacheOutputStream;


	@Before
	public void prepareApacheInputStream() {
		apacheInputStream = new ApacheInputStream(new ByteArrayInputStream(inputString.getBytes()));
		apacheOutputStream = new ByteArrayOutputStream(BUF_SIZE);
	}

	@Test
	public void transferedBytesTest() {

		long bytesToTransfer = inputString.getBytes().length;
		long transfered = 0;


		try {
			transfered = Transfer.performTransfer(apacheInputStream, apacheOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		assertEquals(bytesToTransfer, transfered);
	}

}
