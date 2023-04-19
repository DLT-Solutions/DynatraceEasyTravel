/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PortReservation.java
 * @date: 06.08.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.config;



/**
 *
 * @author stefan.moschinski
 */
public abstract class ResourceReservation {

	private volatile boolean released = false;

	/**
	 * Releases the previously reserved port
	 * 
	 * @author stefan.moschinski
	 */
	public abstract void release();

	/**
	 * 
	 * @return <code>true</code> if the reservation has been released, <code>false</code> otherwise
	 * @author stefan.moschinski
	 */
	protected boolean isReleased() {
		return released;
	}

	/**
	 * Sets the reservation state to released; that is, the reservation is not longer needed
	 * 
	 * @author stefan.moschinski
	 */
	protected void setReleased() {
		this.released = true;
	}

}
