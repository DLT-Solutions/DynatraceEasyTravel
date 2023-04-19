package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.SyntheticEndVisitAction;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravel.VisitLength;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasytravelStartPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop.Category;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop.WordPressShopCategoryPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop.WordPressShopBlogPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop.WordPressShopHomePage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop.WordPressShopProductPage;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;
import com.google.common.collect.Lists;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class WordPressShopVisit implements Visit {
	private final String host;
	private final VisitLength length;
	private static final RandomSet<Category> categories = createCategories();
	
	public WordPressShopVisit(String host, VisitLength length) {
		this.host = host;
		this.length = length;
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		CustomerSession session = EasyTravel.createCustomerSession(host, user, location);
		List<Action> actions = Lists.newArrayList();
		
		addInitActions(session, actions);
		addBlogLoad(session, actions);		
		int noOfSearches = (VisitLength.SHORT.equals(length)) ? UemLoadUtils.randomInt(2) + 1 : UemLoadUtils.randomInt(4) + 3;
		for(int i=0;i<noOfSearches;i++) {
			addSearch(session, actions, categories.getNext());
		}
		
		if(location.isRuxitSynthetic()) {
			actions.add(new SyntheticEndVisitAction());
		}
		return actions.toArray(new Action[actions.size()]);
	}
	
	private static RandomSet<Category> createCategories() {
		RandomSet<Category> rs = new RandomSet<>();
		rs.add(Category.ACCESSORIES, 5);
		rs.add(Category.CLOTHING, 1);
		rs.add(Category.DECOR, 1);
		rs.add(Category.HOODIES, 3);
		rs.add(Category.MUSIC, 2);
		rs.add(Category.TSHIRTS, 5);
		return rs;
	}
	
	private void addInitActions(CustomerSession session, List<Action> actions) {
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.INIT));
		if(Math.random() < 0.1)
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.LOGIN));
	}
	
	private void addBlogLoad(CustomerSession session, List<Action> actions) {
		actions.add(new WordPressShopHomePage(session));
		actions.add(new WordPressShopBlogPage(session));
	}
	
	private void addSearch(CustomerSession session, List<Action> actions, Category category) {
		actions.add(new WordPressShopHomePage(session));
		actions.add(new WordPressShopCategoryPage(category.getPageType(), session));
		actions.add(new WordPressShopProductPage(session, category.getProducts()));
	}

	@Override
	public String getVisitName() {
		return VisitNames.WORDPRESS;
	}

}
