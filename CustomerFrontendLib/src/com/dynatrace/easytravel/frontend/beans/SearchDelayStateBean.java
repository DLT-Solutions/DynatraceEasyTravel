/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: SearchDelayBean.java
 * @date: 01.02.2011
 * @author: peter.lang
 */
package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 *
 * @author peter.lang
 */
@ManagedBean
@SessionScoped
public class SearchDelayStateBean implements Serializable {

	private static final Logger LOGGER = LoggerFactory.make();

	private static final SelectItem[] waitSelectItems = {
			new SelectItem(SearchDelayStrategyEnum.IDLE, "idle", "use waiting strategy idle waiting"),
			new SelectItem(SearchDelayStrategyEnum.BUSY, "busy - direct", "consume cpu time during waiting"),
			new SelectItem(SearchDelayStrategyEnum.BUSY_GETTER, "busy - indirect, call a getter",
					"use simple getter methods to consume cpu") };

	/**
	 * generated serialversionuid.
	 */
	private static final long serialVersionUID = 885922021574894842L;


	private int delaytime = 0;

	private String waitingStrategy = waitSelectItems[0].getValue().toString();

	/**
	 * @return the delaytime
	 */
	public int getDelaytime() {
		return delaytime;
	}

	/**
	 * @param delaytime the delaytime to set
	 */
	public void setDelaytime(int delaytime) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("setDelaytime = " + delaytime);
		}
		this.delaytime = delaytime;
	}

	/**
	 * @return the waitingStragetry
	 */
	public String getWaitingStrategy() {
		return waitingStrategy;
	}

	public SearchDelayStrategyEnum getWaitingStrategyEnum() {
		for (SelectItem curItem : waitSelectItems) {
			SearchDelayStrategyEnum itemStrategy = (SearchDelayStrategyEnum) curItem.getValue();
			if (waitingStrategy.equals(itemStrategy.toString())) {
				return itemStrategy;
			}
		}
		return SearchDelayStrategyEnum.NONE;
	}

	/**
	 * @param waitingStrategy the waitingStragetry to set
	 */
	public void setWaitingStrategy(String waitingStrategy) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("new waiting Strategy " + waitingStrategy);
		}
		this.waitingStrategy = waitingStrategy;
	}


	/**
	 * @return the selectItems
	 */
	public SelectItem[] getWaitSelectItems() {
		return waitSelectItems;
	}

}
