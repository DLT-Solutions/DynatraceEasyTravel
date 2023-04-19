package com.dynatrace.diagnostics.uemload.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;

public class GeoLocationsGenerator {

	public static void main(String[] args) throws Exception {
		/*
		 * This class can be used to generate as many ipv6 addressess as ipv4 addr amount in given file 
		 * You should check if lines amount match the one in your file
		 * sourceFilePath - f.e. "C:\\Users\\username\\.dynaTrace\\easyTravel 2.0.0\\easyTravel\\config\\geo.txt"
		 * finalFilePath - f.e. "C:\\Users\\username\\.dynaTrace\\easyTravel 2.0.0\\easyTravel\\config\\geo.txt"
		 * MasterFile can be downloaded from http://software77.net/geo-ip/ (IPv6 Range gz); Format is: "startRange-EndRange,countryCode"
		 */
		String sourceFilePath = "";
		String finalFilePath = "";
		String masterFilePath = "";
		
		generateGeoFile(sourceFilePath, finalFilePath, masterFilePath);
	}

	public static void generateGeoFile(String sourceFilePath, String finalFilePath, String masterFilePath) throws Exception {
		List<String> ipList = new LinkedList<String>();

		Map<String, String> countries = new HashMap<String, String>();
		countries.put("United States;North America;", "US");
		countries.put("Poland;Europe;", "PL");
		countries.put("Germany;Europe;", "DE");
		countries.put("Japan;Asia;", "JP");
		countries.put("Mexico;North America;", "MX");
		countries.put("Virgin Islands, U.S.;South America;", "VI");
		countries.put("China;Asia;", "CN");
		countries.put("United Kingdom;Europe;", "UK");
		countries.put("Korea, Republic of;Asia;", "KR");
		countries.put("Canada;North America;", "CA");
		countries.put("France;Europe;", "FR");
		countries.put("Italy;Europe;", "IT");
		countries.put("Australia;Oceania;", "AU");
		countries.put("Netherlands;Europe;", "NL");
		countries.put("Taiwan;Asia;", "TW");
		countries.put("Brazil;South America;", "BR");
		countries.put("India;Asia;", "IN");
		countries.put("Russian Federation;Europe;", "RU");
		countries.put("Switzerland;Europe;", "CH");
		countries.put("Spain;Europe;", "ES");
		countries.put("Sweden;Europe;", "SE");
		countries.put("Indonesia;Asia;", "ID");
		countries.put("Europe;Europe;", "EU");
		countries.put("Finland;Europe;", "FI");
		countries.put("Turkey;Europe;", "TR");
		countries.put("South Africa;Africa;", "ZA");
		countries.put("Argentina;South America;", "AR");
		countries.put("Norway;Europe;", "NO");
		countries.put("Hong Kong;Asia;", "HK");
		countries.put("Malaysia;Asia;", "MY");
		countries.put("Belgium;Europe;", "BE");
		countries.put("Austria;Europe;", "AT");
		countries.put("Denmark;Europe;", "DK");
		countries.put("Hungary;Europe;", "HU");
		countries.put("Romania;Europe;", "RO");
		countries.put("Colombia;South America;", "CO");
		countries.put("Thailand;Asia;", "TH");
		countries.put("Ukraine;Europe;", "UA");
		countries.put("Israel;Asia;", "IL");
		countries.put("Egypt;Africa;", "EG");
		countries.put("Greece;Europe;", "GR");
		countries.put("Chile;South America;", "CL");
		countries.put("Czech Republic;Europe;", "CZ");
		countries.put("New Zealand;Oceania;", "NZ");
		countries.put("Singapore;Asia;", "SG");
		countries.put("Portugal;Europe;", "PT");
		countries.put("United Arab Emirates;Asia;", "AE");
		countries.put("Saudi Arabia;Asia;", "SA");
		countries.put("Ireland;Europe;", "IE");
		countries.put("Bulgaria;Europe;", "BG");
		countries.put("Vietnam;Asia;", "VN");
		countries.put("Peru;South America;", "PE");
		countries.put("Iran, Islamic Republic of;Asia;", "IR");
		countries.put("Philippines;Asia;", "PH");
		countries.put("Morocco;Africa;", "MA");
		countries.put("Luxembourg;Europe;", "LU");
		countries.put("Pakistan;Asia;", "PK");
		countries.put("Lithuania;Europe;", "LT");
		countries.put("Slovenia;Europe;", "SI");
		countries.put("Slovakia;Europe;", "SK");
		countries.put("Serbia;Europe;", "RS");
		countries.put("Bangladesh;Asia;", "BD");
		countries.put("Netherlands Antilles;South America;", "AN");
		countries.put("Kuwait;Asia;", "KW");
		countries.put("Ecuador;South America;", "EC");
		countries.put("Algeria;Africa;", "DZ");
		countries.put("Costa Rica;South America;", "CR");
		countries.put("Ghana;Africa;", "GH");
		countries.put("Uruguay;South America;", "UY");
		countries.put("Jersey;Europe;", "JE");
		countries.put("Venezuela;South America;", "VE");
		countries.put("Qatar;Asia;", "QA");
		countries.put("Nigeria;Africa;", "NG");
		countries.put("Bahamas;South America;", "BS");
		countries.put("Kyrgyzstan;Asia;", "KG");
		countries.put("Bahrain;Asia;", "BH");
		countries.put("Puerto Rico;South America;", "PR");
		countries.put("Sierra Leone;Africa;", "SL");
		countries.put("Latvia;Europe;", "LV");
		countries.put("Panama;South America;", "PA");
		countries.put("Paraguay;South America;", "PY");
		countries.put("Lebanon;Asia;", "LB");
		countries.put("Barbados;South America;", "BB");
		countries.put("El Salvador;South America;", "SV");
		countries.put("Albania;Europe;", "AL");
		countries.put("Macau;Asia;", "MO");
		countries.put("Jordan;Asia;", "JO");
		countries.put("Moldova, Republic of;Europe;", "MD");
		countries.put("Montenegro;Europe;", "ME");
		countries.put("Guernsey;Europe;", "GG");
		countries.put("Croatia;Europe;", "HR");
		countries.put("Estonia;Europe;", "EE");
		countries.put("Tunisia;Africa;", "TN");
		countries.put("New Caledonia;Oceania;", "NC");
		countries.put("Isle of Man;Europe;", "IM");
		countries.put("Seychelles;Africa;", "SC");
		countries.put("Zambia;Africa;", "ZM");
		countries.put("Kazakhstan;Asia;", "KZ");
	
		int lines = countFileLines(sourceFilePath);
		int linesInMaster = countFileLines(masterFilePath);
		int linesCounter = 0;

		try (RandomAccessFile sourceFile = new RandomAccessFile(new File(sourceFilePath), "rw");
				RandomAccessFile finalFile = new RandomAccessFile(new File(finalFilePath), "rw");) {
			finalFile.setLength(0);

			while (linesCounter < lines) {
				String line = sourceFile.readLine();
				String[] elements = line.split(";");
				String countryAndContinent = elements[0] + ";" + elements[1] + ";";
				ipList.clear();

				String[] currentIps = elements[2].split(",");
				for (String ip : currentIps) {
					if (!ip.matches("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$")) {
						ipList.add(ip);
					}
				}

				if (countries.containsKey(countryAndContinent)) {
					int amountOfIpv6AddrToGenerate = ipList.size() / 10; // Currently
																			// IPv6
																			// covers
																			// 10%
																			// of
																			// addresses

					String ipRange = findRangeInMasterFileForCountry(countries.get(countryAndContinent), masterFilePath,
							linesInMaster);
					String ip = "";
					if (StringUtils.isNotBlank(ipRange)) {
						for (int i = 0; i < amountOfIpv6AddrToGenerate; i++) {
							ip = generateIp(ipRange);
							ipList.add(ip);
						}
					}
				}
				String joinedList = StringUtils.join(ipList, ",");
				finalFile.writeBytes(countryAndContinent + joinedList + "\n");
				linesCounter++;
			}
		}
	}

	private static String generateIp(String range) {
		char[] possibilities = "0123456789abcdef".toCharArray();

		String limit = range.substring(range.indexOf('-') + 1, range.length());

		char[] a = limit.toCharArray();

		for (int i = 15; i < a.length; i++) {
			if (a[i] == 'f') {

				for (int j = 0; j < possibilities.length; j++) {
					a[i] = possibilities[ThreadLocalRandom.current().nextInt(0, 16)];
				}
			}
		}

		String ip = new String(a);
		return ip;
	}

	private static String findRangeInMasterFileForCountry(String country, String masterFilePath, int lines)
			throws IOException {
		int linesCounter = 0;
		String ipRange = "";
		try (RandomAccessFile masterFile = new RandomAccessFile(new File(masterFilePath), "r");) {

			while (linesCounter < lines) {
				String masterLine = masterFile.readLine();

				if (!masterLine.startsWith("#")) {

					String countryShort = masterLine.substring(masterLine.indexOf(',') + 1,
							(masterLine.indexOf(',') + 3));
					if (countryShort.equals(country)) {

						ipRange = masterLine.substring(0, (masterLine.indexOf(',')));
						break;
					}
				}
				linesCounter++;
			}
		}
		return ipRange;
	}
	
	private static int countFileLines(String filePath) throws IOException {
		int lines = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath));) {
			while (reader.readLine() != null)
				lines++;
		}
		return lines;
	}
}
