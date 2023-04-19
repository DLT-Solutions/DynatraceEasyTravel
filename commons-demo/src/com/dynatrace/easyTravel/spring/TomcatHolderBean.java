package com.dynatrace.easytravel.spring;

import org.apache.catalina.startup.Tomcat;

import com.dynatrace.easytravel.config.TomcatResourceReservation;

/**
 * Simple bean holding reference to embedded Tomcat. Used in WarDeploymentPlugin
 * @author cwpl-rpsciuk
 *
 */
public class TomcatHolderBean {
	/**
	 * Running tomcat instance
	 */
	private Tomcat tomcat;
	
	/**
	 * {@link TomcatResourceReservation} if present. Can be null.
	 */
	private TomcatResourceReservation reservation;

	public TomcatResourceReservation getReservation() {
		return reservation;
	}

	public void setReservation(TomcatResourceReservation reservation) {
		this.reservation = reservation;
	}

	public Tomcat getTomcat() {
		return tomcat;
	}

	public void setTomcat(Tomcat tomcat) {
		this.tomcat = tomcat;
	}
}
