package com.dynatrace.easytravel.frontend.rest.data;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class UserDTO {

    private String firstName;
    private String lastName;
    private String email; // equals user name
    private String password;
	private String state;
	private String city;
	private String street;
	private String door;
	private String phone;
	private String loyaltyStatus;
	
	public UserDTO() { // NOSONAR - empty on purpose
	}
	
	public UserDTO(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public UserDTO(String firstName, String lastName, String email, String password) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
	}

	public UserDTO(String firstName, String lastName, String email, String password, String state,
			String city, String street, String door, String phone, String loyaltyStatus) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.state = state;
		this.city = city;
		this.street = street;
		this.door = door;
		this.phone = phone;
		this.loyaltyStatus = loyaltyStatus;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getDoor() {
		return door;
	}

	public void setDoor(String door) {
		this.door = door;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLoyaltyStatus() {
		return loyaltyStatus;
	}

	public void setLoyaltyStatus(String loyaltyStatus) {
		this.loyaltyStatus = loyaltyStatus;
	}
}
