package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.dynatrace.easytravel.util.ResourceFileReader;
import org.junit.Test;

public class GeoReaderTest {

	@Test
	public void testReadGeo() {
		InputStream is = null;
		BufferedReader in = null;
		int cnt =0;
		try {
			is = ResourceFileReader.getInputStream(ResourceFileReader.GEO);
			in = new BufferedReader(new InputStreamReader(is));
			try {
				String line;
				while ((line = in.readLine()) != null) {
					String[] columns = line.split(";");
					String country = columns[0];
					String[] ips = columns[columns.length - 1].split(",");
					for (int i=0; i<ips.length; i++) {

						String ip = ips[i];
						// does it end in .0
						if (ip.endsWith(".0")) {
							cnt++;
							System.out.println( country + " " + ips[i]);
						}
					}

				}
			} finally {
				if(is != null)
					is.close();
				if(in != null)
					in.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println( "number of IPs ending in .0 [" + cnt  + "]");
		assertTrue( "Number of IPs ending in .0 [" + cnt +"]", cnt==0);
	}


}
