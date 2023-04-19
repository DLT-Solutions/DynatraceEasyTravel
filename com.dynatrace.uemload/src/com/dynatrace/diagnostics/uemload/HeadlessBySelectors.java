package com.dynatrace.diagnostics.uemload;

import java.util.List;

import org.openqa.selenium.By;

public enum HeadlessBySelectors {
	AngularDestinationField(By.id("search:destination")),
	AngularFromDateField(By.id("search:fromdate")),
	AngularToDateField(By.id("search:todate")),
	AngularTravellersField(By.id("search:travellers")),
	AngularSearchButton(By.name("search:submit")),
	AngularSearchResult(By.id("search:result")),
	AngularSpecialOffersResult(By.id("specialOffers:result")),
	AngularClearButton(By.name("search:clear")),
	AngularBookNowButton(By.name("book:booknow")),
	AngularPayButton(By.name("payment:pay")),
	AngularPay2Button(By.name("payment:2pay")),
	AngularDateParagraph(By.name("payment:date")),
	AngularLogin(By.id("header:login")),
	AngularLogout(By.id("header:logout")),
	AngularSpecialOffers(By.id("header:specialoffers")),
	AngularContact(By.id("header:contact")),
	AngularSignUp(By.id("header:signup")),
	AngularContactFooter(By.id("footer:contact")),
	AngularTermsFooter(By.id("footer:terms")),
	AngularPolicyFooter(By.id("footer:policy")),
	AngularCreditCardFirst4Digits(By.id("creditCard:first4digits")),
	AngularCreditCardSecond4Digits(By.id("creditCard:second4digits")),
	AngularCreditCardThird4Digits(By.id("creditCard:third4digits")),
	AngularCreditCardFourth4Digits(By.id("creditCard:fourth4digits")),
	AngularCreditCardCVC(By.id("creditCard:cvc")),
	AngularCreditCardSubmitButton(By.name("creditCard:submit")),
	AngularBookingSummaryButton(By.id("summaryButton")),
	AngularSearchDestinationField(By.id("search:destination")),
	AngularSearchFromDateField(By.id("search:fromdate")),
	AngularSearchToDateField(By.id("search:todate")),
	AngularSearchTravellersField(By.id("search:travellers")),
	AngularLoginFormUsername(By.id("loginForm:username")),
	AngularLoginFormPassword(By.id("loginForm:password")),
	AngularLoginFormSubmit(By.id("loginForm:submit")),
	AngularSearchItem(By.className("item")),
	AngularTravelersDropdown(By.xpath("//select[@formcontrolname='travelers']")),
	AngularNavigationWhenMobile(By.xpath("//nav[@class='header-navigation']")),
	AngularBody(By.xpath("/html/body")),
	AngularLogo(By.className("logo")),
	AngularJourneyStats(By.className("journey-stats")),
	BlogLink(By.name("blogLink")),
	BlogLatrobePost(By.xpath("//a[contains(@href,'/blog/?p=96')]")),
	BlogArchive2013Link(By.xpath("//a[contains(@href,'/blog/?m=201307')]")),
	BlogItalyPost(By.xpath("//a[contains(@href,'/blog/?p=36')]")),
	AngularSlideshowArrowContainerNext(By.xpath("//div[@class='arrow-container next']")),
	AngularSignUpFirstName(By.xpath("//input[@formcontrolname='firstName']")),
	AngularSignUpLastName(By.xpath("//input[@formcontrolname='lastName']")),
	AngularSignUpEmail(By.xpath("//input[@formcontrolname='email']")),
	AngularSignUpConfirmEmail(By.xpath("//input[@formcontrolname='confirmEmail']")),
	AngularSignUpPassword(By.xpath("//input[@formcontrolname='password']")),
	AngularSignUpConfirmPassword(By.xpath("//input[@formcontrolname='confirmPassword']")),
	AngularSignUpButton(By.xpath("//button[text()='Sign Up']")),

	B2BHomeJourneys(By.cssSelector("#hs_container > div.hs_area.hs_area1")),
	B2BLoginFormUsername(By.id("UserName")),
	B2BLoginFormPassword(By.id("Password")),
	B2BLoginFormSubmit(By.id("loginButton")),
	B2BMenuJourneys(By.id("journeymenu")),
	B2BMenuLocations(By.id("locationmenu")),
	B2BMenuBookings(By.id("bookingmenu")),
	B2BMenuReports(By.id("reportmenu")),
	B2BMenuLogout(By.id("logoffmenu")),

	CustomerIceFormCreditCardType(By.id("iceform:creditCardType")),
	CustomerIceFormCredidCardNumber(By.id("iceform:creditCardNumber")),
	CustomerIceFormCreditCardOwner(By.id("iceform:creditCardOwner")),
	CustomerIceFormCreditCardExpirationMonth(By.id("iceform:expirationMonth")),
	CustomerIceFormCreditCardExpirationYear(By.id("iceform:expirationYear")),
	CustomerIceFormCVCVerification(By.id("iceform:verificationNumber")),
	CustomerIceFormPaymentNext(By.id("iceform:bookPaymentNext")),
	CustomerIceFormBookFinish(By.id("iceform:bookFinishFinish")),
	CustomerIceFormDestination(By.id("iceform:destination")),
	CustomerIceFormDateFrom(By.id("iceform:fromDate")),
	CustomerIceFormDateTo(By.id("iceform:toDate")),
	CustomerIceFormSearch(By.id("iceform:search")),
	CustomerWeatherForecast(By.linkText("Weather forecast")),
	CustomerPHPPlugin(By.xpath("//a[img/@class='iceGphImg etResultsImage downshadow']")),
	CustomerPHPPLuginAlternative(By.xpath("//a[img/@class='iceGphImg etResultsImage']")),
	CustomerLoginFormLoginLink(By.id("loginForm:loginLink")),
	CustomerLoginFormUsername(By.id("loginForm:username")),
	CustomerLoginFormPassword(By.id("loginForm:password")),
	CustomerLoginFormSubmit(By.id("loginForm:loginSubmit")),
	CustomerLoginFormLogoutLink(By.id("loginForm:logoutLink")),
	CustomerBookNowButton(By.linkText("Book Now")),
	CustomerReviewNextButton(By.id("iceform:bookReviewNext")),
	CustomerReviewNewSearchButton(By.id("iceform:bookReviewNewSearch")),
	CustomerPaymentBackButton(By.id("iceform:bookPaymentBack")),
	CustomerClearLink(By.linkText("[Clear]")),
	CustomerAboutLink(By.linkText("About")),
	CustomerContactLink(By.linkText("Contact")),
	CustomerTermOfUseLink(By.linkText("Terms of Use")),
	CustomerPrivacyPolicyLink(By.linkText("Privacy Policy")),

	OnlineBoutiqueAddToCartButton(By.cssSelector(
			"body > main > div.h-product.container > div > div.product-info.col-md-5 > div > form > button")),
	OnlineBoutiqueViewCartButton(By.xpath("//a[contains(@href, '/cart')]")),
	OnlineBoutiqueKeepBrowsingButton(By.xpath("//form/a[contains(@href, '/')]")),
	OnlineBoutiquePlaceYourOrderButton(By.cssSelector("#btn_submit")),
	OnlineBoutiqueMainPageLink(By.xpath("//a[contains(@href, '/')]")),
	OnlineBoutiqueQuantitySelect(By.xpath("//select[@id='quantity']")),

	OnlineBoutiqueProductLinkSunglasses(By.xpath("//a[contains(@href, '/product/OLJCESPC7Z')]")),
	OnlineBoutiqueProductLinkTankTop(By.xpath("//a[contains(@href, '/product/66VCHSJNUP')]")),
	OnlineBoutiqueProductLinkWatch(By.xpath("//a[contains(@href, '/product/1YMWWN1N4O')]")),
	OnlineBoutiqueProductLinkLoafers(By.xpath("//a[contains(@href, '/product/L9ECAV7KIM')]")),
	OnlineBoutiqueProductLinkHairdryer(By.xpath("//a[contains(@href, '/product/2ZYFJ3GM2N')]")),
	OnlineBoutiqueProductLinkCandleHolder(By.xpath("//a[contains(@href, '/product/0PUK6V6EV0')]")),
	OnlineBoutiqueProductLinkSaltnPepperShakers(By.xpath("//a[contains(@href, '/product/LS4PSXUNUM')]")),
	OnlineBoutiqueProductLinkBambooGlassJar(By.xpath("//a[contains(@href, '/product/9SIQT8TOJO')]")),
	OnlineBoutiqueProductLinkMug(By.xpath("//a[contains(@href, '/product/6E92ZMYYFZ')]")),
	;

	private By by;

	private HeadlessBySelectors(By by) {
		this.by = by;
	}

	public By get() {
		return by;
	}

	public static List<HeadlessBySelectors> getOnlineBoutiqueProducts(){
		return List.of(
			OnlineBoutiqueProductLinkSunglasses,
			OnlineBoutiqueProductLinkTankTop,
			OnlineBoutiqueProductLinkWatch,
			OnlineBoutiqueProductLinkLoafers,
			OnlineBoutiqueProductLinkHairdryer,
			OnlineBoutiqueProductLinkCandleHolder,
			OnlineBoutiqueProductLinkSaltnPepperShakers,
			OnlineBoutiqueProductLinkBambooGlassJar,
			OnlineBoutiqueProductLinkMug);
	}

}
