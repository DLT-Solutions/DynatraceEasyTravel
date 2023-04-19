package com.dynatrace.easytravel.frontend.beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Runnable bean that uses the recommendation bean in order to create special
 * offers for the "Special Offers" site.
 *
 * @author cwat-bfellner
 *
 */
@ManagedBean
@SessionScoped
public class SpecialOffersBean extends RecommendationBean {

	public SpecialOffersBean() {
		super();
		amountOfJourneys = 6;
		headerText = "These are our best offers";
	}
}
