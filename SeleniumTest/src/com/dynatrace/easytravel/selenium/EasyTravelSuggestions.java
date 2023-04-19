package com.dynatrace.easytravel.selenium;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EasyTravelSuggestions extends PageObjectBase {

	private List<WebElement> suggestions = null;

	public EasyTravelSuggestions(WebDriver driver) throws Exception {
		super(driver);

		// now we need to find the suggestions
		suggestions = new ArrayList<WebElement>();
		List<WebElement> spans = waitForElements(By.tagName("span"));
		for(WebElement span : spans) {
			if(span.getAttribute("id").startsWith("iceform:destination:"))
				suggestions.add(span);
		}
	}

	public int getNoOfSuggestions() {
		return suggestions.size();
	}

	public List<String> getSuggestions() {
		List<String> suggestionTexts = new ArrayList<String>();
		for(WebElement suggElement : suggestions) {
			suggestionTexts.add(suggElement.getText());
		}

		return suggestionTexts;
	}

	public EasyTravelSearchResult selectSuggestion(int index) throws Exception {
		suggestions.get(index).click();

		// how long to wait for the search result to be back?
		java.lang.Thread.sleep(2000);

		return new EasyTravelSearchResult(driver);
	}
}
