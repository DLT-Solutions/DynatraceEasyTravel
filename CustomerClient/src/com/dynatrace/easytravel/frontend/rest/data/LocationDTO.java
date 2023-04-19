package com.dynatrace.easytravel.frontend.rest.data;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class LocationDTO {
	
	private String name;

	public LocationDTO() { // NOSONAR - empty on purpose
	}

	public LocationDTO(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
