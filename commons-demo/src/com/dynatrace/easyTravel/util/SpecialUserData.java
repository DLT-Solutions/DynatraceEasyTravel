package com.dynatrace.easytravel.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.config.Directories;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SpecialUserData {
	private static final Logger LOGGER = Logger.getLogger(SpecialUserData.class.getName());
	
	private static SpecialUserData instance;
	
	public List<SpecialUserDataRow> usedUsers; 
	
	public int WEEKLY_USER_DAY = Calendar.WEDNESDAY;
	
	private SpecialUserData() {
		readConfigFile();
	}
	
	public static SpecialUserData getInstance() {
		if (instance == null) {
			synchronized (SpecialUserData.class) {
				if (instance == null) {
					instance = new SpecialUserData();
				}
			}
		}
		
		return instance;
	}
	
	public SpecialUserDataRow getUserRow(String visitorInfo) {
		return usedUsers.stream().filter(uu -> uu.visitorInfo.compareToIgnoreCase(visitorInfo) == 0).findFirst().orElse(null);
	}
	
	public void generateConfigFile() {
		generateConfigFile(usedUsers, ResourceFileReader.SPECIALUSERSDATA);
	}

	public void generateConfigFile(List<SpecialUserDataRow> users, String fileName) {
		LOGGER.log(Level.INFO, "Creating " + fileName + " file.");

		File file = new File(Directories.getConfigDir(), fileName);
		
		if (file.exists()) {
			LOGGER.log(Level.INFO, "File was already present - removing it and saving a new copy.");
			file.delete();
			file = new File(Directories.getConfigDir(), fileName);
		}

		file.getParentFile().mkdirs();

		try (PrintWriter writer = new PrintWriter(file)) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			writer.println(mapper.writeValueAsString(users));
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.WARNING, "Can't create config file for special users - file not found", e);
		} catch (JsonProcessingException e) {
			LOGGER.log(Level.WARNING, "Can't create config file for special users - json parsing problem", e);
		} 
	}
	
	private void readConfigFile() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			File file = new File(Directories.getConfigDir(), ResourceFileReader.SPECIALUSERSDATA);

			if (file.exists()) {
				usedUsers = mapper.readValue(file, new TypeReference<ArrayList<SpecialUserDataRow>>() {});
			}
			else {
				usedUsers = new ArrayList<SpecialUserDataRow>();
			}
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Problem parsing json configuration", e);
		}
	}
	
	@TestOnly
	public void setWeeklyUserDay(int day) {
		this.WEEKLY_USER_DAY = day;
	}
}
