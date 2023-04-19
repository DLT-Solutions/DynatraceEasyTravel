package com.dynatrace.easytravel.frontend.rest.data;

/**
 *
 * @author Michal.Bakula
 *
 */
public class StoreBookingDTO {

	private Integer journeyId;
	private String username;
	private String creditcard;
	private Double amount;
	private String travellers;

	public StoreBookingDTO() { // NOSONAR - empty on purpose
	}

	public StoreBookingDTO(Integer journeyId, String username, String creditcard, Double amount, String travellers) {
		this.journeyId = journeyId;
		this.username = username;
		this.creditcard = creditcard;
		this.amount = amount;
		this.travellers = travellers;
	}

	public Integer getJourneyId() {
		return journeyId;
	}

	public void setJourneyId(Integer journeyId) {
		this.journeyId = journeyId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCreditcard() {
		return creditcard;
	}

	public void setCreditcard(String creditcard) {
		this.creditcard = creditcard;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getTravellers() {
		return travellers;
	}

	public void setTravellers(String travellers) {
		this.travellers = travellers;
	}
}
