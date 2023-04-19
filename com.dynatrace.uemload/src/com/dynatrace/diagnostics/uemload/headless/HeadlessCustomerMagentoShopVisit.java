package com.dynatrace.diagnostics.uemload.headless;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessCustomerMagentoShopVisit extends HeadlessVisit {

	private String magentoUrl;
	
	public HeadlessCustomerMagentoShopVisit(String host, String magentoUrl) {
		super(host);
		this.magentoUrl = magentoUrl;
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		actions.add(new HeadlessGetAction(magentoUrl));
		actions.add(new HeadlessClickAction(By.linkText("Sale")));
		int m = UemLoadUtils.randomInt(4);
		for(int i = 0; i < m; i++) {
			actions.add(new HeadlessClickAction(By.xpath("//a[contains(@href, '" + getRandomFilter() + "=')")));
		}
		actions.add(new HeadlessClickAction(By.linkText("Women")));
		int n = UemLoadUtils.randomInt(3) + 1;
		for(int i = 0; i < n; i++) {
			actions.add(new HeadlessClickAction(By.xpath("//a[contains(@href, '" + getRandomWomenPage() + "')")));
			m = UemLoadUtils.randomInt(4);
			for(int j = 0; j < m; j++) {
				actions.add(new HeadlessClickAction(By.xpath("//a[contains(@href, '" + getRandomFilter() + "'=)")));
			}
		}
		return actions.toArray(new Action[actions.size()]);
	}
	
	private String getRandomFilter() {
		switch(UemLoadUtils.randomInt(5)) {
			case 0: return "size";
			case 1: return "price";
			case 2: return "color";
			case 3: return "occasion";
			default: return "apparel_type";
		}
	}
	
	private String getRandomWomenPage() {
		switch(UemLoadUtils.randomInt(4)) {
			case 0: return "new-arrivals.html";
			case 1: return "tops-blouses.html";
			case 2: return "pants-denim.html";
			default: return "dresses-skirts.html";
		}
	}

	@Override
	public String getVisitName() {
		return VisitNames.HEADLESS_MAGENTO_SHOP_VISIT;
	}

}
