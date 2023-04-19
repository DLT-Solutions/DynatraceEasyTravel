package com.dynatrace.easytravel.frontend.rest.data;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class LoginUserDTO {
	
	private String username;
	private String password;
	
	public LoginUserDTO() { // NOSONAR - empty on purpose
	}
	
	public LoginUserDTO(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
