package com.dynatrace.easytravel.frontend.rest.data;

import java.util.Calendar;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class JourneyDTO {
	
	private int id;
	private String name;
	private Calendar fromDate;
	private Calendar toDate;
	private String start;
	private String destination;
	private String tenant;
	private double amount;
	private JourneyImageDTO images;
	private String averageTotal;
	
	public JourneyDTO() { // NOSONAR - empty on purpose
	}
	
	public JourneyDTO(int id, String name, Calendar fromDate, Calendar toDate, String start, String destination,
			String tenant, double amount, JourneyImageDTO images, String averageTotal) {
		super();
		this.id = id;
		this.name = name;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.start = start;
		this.destination = destination;
		this.tenant = tenant;
		this.amount = amount;
		this.images = images;
		this.averageTotal = averageTotal;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Calendar getFromDate() {
		return fromDate;
	}

	public void setFromDate(Calendar fromDate) {
		this.fromDate = fromDate;
	}

	public Calendar getToDate() {
		return toDate;
	}

	public void setToDate(Calendar toDate) {
		this.toDate = toDate;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public JourneyImageDTO getImages() {
		return images;
	}

	public void setImages(JourneyImageDTO images) {
		this.images = images;
	}

	public String getAverageTotal() {
		return averageTotal;
	}

	public void setAverageTotal(String averageTotal) {
		this.averageTotal = averageTotal;
	}
}
