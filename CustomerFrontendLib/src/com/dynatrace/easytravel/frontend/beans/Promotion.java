package com.dynatrace.easytravel.frontend.beans;

import com.dynatrace.easytravel.frontend.data.JourneyDO;

public class Promotion {

	private String location;

	private String imageSrc;

	private JourneyDO journey;

	public Promotion(String location) {
		this.location = location;
	}

	public Promotion(String location, String imageSrc) {
		this.location = location;
		this.imageSrc = imageSrc;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getImageSrc() {
		return imageSrc;
	}

	public void setImageSrc(String imageSrc) {
		this.imageSrc = imageSrc;
	}

	public JourneyDO getJourney() {
		return journey;
	}

	public void setJourney(JourneyDO journey) {
		this.journey = journey;
	}
}
