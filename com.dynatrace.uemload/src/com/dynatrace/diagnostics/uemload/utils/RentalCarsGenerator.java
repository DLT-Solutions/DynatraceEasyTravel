package com.dynatrace.diagnostics.uemload.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.util.ResourceFileReader;

/**
 *
 * @author Michal.Bakula
 *
 */
public class RentalCarsGenerator {

	private static final Logger LOGGER = Logger.getLogger(RentalCarsGenerator.class.getName());

	private Map<String, List<String>> locations = new HashMap<>();

	private static final String[] MANUFACTURER = {"Dynatrace RentalCars Inc", "Dynatrace SmartCars Inc"};
	private static final String[] MODEL = {"Ford Fiesta", "Ford Focus", "Volkswagen Golf", "Kia Rio", "Mazda3", "Honda Civic" };
	private static final String[] OS = {"APM OS 7", "APM OS 8", "APM OS X"};

	public RentalCarsGenerator() {
		InputStream is = null;
		BufferedReader in = null;
		try {
			is = ResourceFileReader.getInputStream(ResourceFileReader.GEO);
			in = new BufferedReader(new InputStreamReader(is));
			try {
				String line;
				while ((line = in.readLine()) != null) {
					String[] columns = line.split(";");
					String country = columns[0];
					String[] ips = columns[columns.length - 1].split(",");

					List<String> ipsInCountry = new ArrayList<String>();
					int noOfCars = (int) Math.ceil(ips.length/10.0);
					for(int i=0;i<noOfCars;i++) {
						ipsInCountry.add(ips[UemLoadUtils.randomInt(ips.length)]);
					}
					locations.put(country, ipsInCountry);
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
	}

	public void generateUserFile() {
		LOGGER.log(Level.INFO, "Creating files with Rental Cars.");

		PrintWriter writer = null;
		try {
			File file = new File(Directories.getConfigDir(), ResourceFileReader.RENTALCARS);
			file.getParentFile().mkdirs();

			long carId = 0;
			long lastDeviceId = 67101202L;
			try {
				writer = new PrintWriter(file);

				for(Map.Entry<String, List<String>> entry : locations.entrySet()) {
					String country = entry.getKey();
					List<String> ips = entry.getValue();
					for(String ip : ips) {
						lastDeviceId += carId++;
						String newLine = String.format("%s;%s;%s;%s;%s;%s", country, ip,
								MANUFACTURER[UemLoadUtils.randomInt(MANUFACTURER.length)],
								MODEL[UemLoadUtils.randomInt(MODEL.length)],
								OS[UemLoadUtils.randomInt(OS.length)],
								Long.toString(lastDeviceId));
						writer.println(newLine);
					}
				}
			} finally {
				if(writer != null) {
					writer.close();
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Cannot create Extended Users file.", e);
		}
	}
}
