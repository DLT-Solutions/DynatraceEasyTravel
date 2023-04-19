package com.dynatrace.diagnostics.uemload.iot.car;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.iot.IotDevice;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;
import com.dynatrace.diagnostics.uemload.utils.RentalCarsGenerator;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.openkit.AgentTechnologyType;
import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;

/**
 * @author Michal.Bakula
 */
public class RentalCarParams extends OpenKitParams {

	private static final Logger LOGGER = Logger.getLogger(RentalCarParams.class.getName());

	private static boolean regenAttemptFlag = false;

	public RentalCarParams() {
		setAgentType(AgentTechnologyType.JAVA);
	}

	public static List<RentalCarParams> getRentalCars() {
		try {
			return getRentalCars(ResourceFileReader.getInputStream(ResourceFileReader.RENTALCARS));
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Cannot read file with Rental Cars.", e);
		}
		return Collections.emptyList();
	}

	public static List<RentalCarParams> getRentalCars(InputStream inputStream) {
		List<RentalCarParams> rentalCars = Collections.emptyList();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, BaseConstants.UTF8));

			try {
				rentalCars = new ArrayList<>();
				String line;
				while ((line = br.readLine()) != null) {
					String[] tokens = line.split(";");

					RentalCarParams car = new RentalCarParams();
					car.setCountry(tokens[0]);
					car.setIp(tokens[1]);
					car.setManufacturer(tokens[2]);
					car.setModel(tokens[3]);
					car.setOs(tokens[4]);
					car.setAppVersion(IotDevice.getRandomAppVersion());
					car.setDeviceId(Long.parseLong(tokens[5]));
					car.setDataCollectionLevel(DataCollectionLevel.USER_BEHAVIOR);
					car.setCrashReportingLevel(CrashReportingLevel.OPT_IN_CRASHES);

					rentalCars.add(car);
				}
			} finally {
				br.close();
				inputStream.close();
			}
		} catch (Exception e) {
			if (!regenAttemptFlag) {
				regenAttemptFlag = true;
				LOGGER.log(Level.SEVERE,
						String.format(
								"Cannot read %s file. File probably corrupted or outdated. Attepting to make a backup and generate new one.",
								ResourceFileReader.RENTALCARS),
						e);
				RentalCarsGenerator generator = new RentalCarsGenerator();
				generator.generateUserFile();
				rentalCars = getRentalCars();
			} else {
				LOGGER.log(Level.SEVERE, String.format("Error occured even after regeneration of %s file.",
						ResourceFileReader.RENTALCARS), e);
			}
		}

		return rentalCars;
	}
}
