package com.dynatrace.easytravel.frontend.rest.data;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class UserAuthenticationDTO {
	
	private UserDTO user;
	
	public UserAuthenticationDTO() { // NOSONAR - empty on purpose
	}
	
	public UserAuthenticationDTO(UserDTO user) {
		this.user = user;
	}

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}

}
