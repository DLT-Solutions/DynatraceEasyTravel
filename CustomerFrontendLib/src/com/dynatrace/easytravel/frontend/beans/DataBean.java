package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.dynatrace.easytravel.frontend.data.DataProvider;
import com.dynatrace.easytravel.frontend.data.DataProviderInterface;

@ManagedBean
@ApplicationScoped
public class DataBean implements Serializable {
	private static final long serialVersionUID = -8429774909906988030L;

	private static final DataProviderInterface DATA_PROVIDER = new DataProvider();

	public DataProviderInterface getDataProvider() {
		return DATA_PROVIDER;
	}

	private static final String COST_FORMAT = "$ {0,number,0.00}";

	public String getCostFormat() {
		return COST_FORMAT;
	}

	/*
	 * /// for testing dynamic things
	public int getCountBookins() {
		try {
			System.out.println(dataProvider.getDatabaseStatistics());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return new Random().nextInt(100);
	}
	//*/

	public String getCurrentViewId() {
		return FacesUtils.getCurrentViewId();
	}
}
