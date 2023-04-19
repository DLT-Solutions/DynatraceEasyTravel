package com.dynatrace.easytravel.frontend.rest.data;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class CardValidationDTO {
	
	private Boolean valid;
	
	public CardValidationDTO() { // NOSONAR - empty on purpose
	}
	
	public CardValidationDTO(Boolean valid) {
		this.valid = valid;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

}
