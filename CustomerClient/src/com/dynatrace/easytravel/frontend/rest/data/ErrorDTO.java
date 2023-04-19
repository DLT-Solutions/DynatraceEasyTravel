package com.dynatrace.easytravel.frontend.rest.data;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class ErrorDTO {

	private String error;
	
	public ErrorDTO() { // NOSONAR - empty on purpose
	}
	
	public ErrorDTO(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
