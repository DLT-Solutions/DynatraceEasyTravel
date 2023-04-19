package com.dynatrace.easytravel.rest.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DateParam {

	private SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private Date date;
	
	public DateParam(String input) throws WebApplicationException {
		try {
	      date = simpleDateFormatter.parse(input);
	    }
	    catch (ParseException e) {
	    	throw new WebApplicationException(Response.Status.BAD_REQUEST);
	    }
	}
	
	public Date getDate() {
		return date;
	}
	
}
