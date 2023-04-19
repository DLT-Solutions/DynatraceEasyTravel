package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop;

/**
 * 
 * @author Michal.Bakula
 *
 */
public enum Product {
	BEANIE("Beanie", "beanie/", Category.ACCESSORIES),
	BELT("Belt", "belt/", Category.ACCESSORIES),
	CAP("Cap", "cap/", Category.ACCESSORIES),
	SUNGLASSES("Sunglasses", "sunglasses/", Category.ACCESSORIES),
	BEANIE_WITH_LOGO("Beanie with Logo", "beanie-with-logo/", Category.ACCESSORIES),
	LOGO_COLLECTION("Logo Collection", "logo-collection/", Category.CLOTHING),
	WORDPRESS_PENNANT("WordPress Pennant", "wordpress-pennant/", Category.DECOR),
	HOODIE("Hoodie", "hoodie/", Category.HOODIES),
	HOODIE_WITH_LOGO("Hoodie with Logo", "hoodie-with-logo/", Category.HOODIES),
	HOODIE_WITH_ZIPPER("Hoodie with Zipper", "hoodie-with-zipper/", Category.HOODIES),
	ALBUM("Album", "album/", Category.MUSIC),
	SINGLE("Single", "single/", Category.MUSIC),
	V_NECK_T_SHIRT("V-Neck T-Shirt", "v-neck-t-shirt/", Category.TSHIRTS),
	T_SHIRT("T-Shirt", "t-shirt/", Category.TSHIRTS),
	LONG_SLEEVE_TEE("Long Sleeve Tee", "long-sleeve-tee/", Category.TSHIRTS),
	POLO("Polo", "polo/", Category.TSHIRTS),
	T_SHIRT_WITH_LOGO("T-Shirt with Logo", "t-shirt-with-logo/", Category.TSHIRTS)
	;
	
	private String name;
	private String path;
	private Category category;
	
	private Product(String name, String path, Category category) {
		this.name = name;
		this.path = path;
		this.category = category;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public Category getCategory() {
		return category;
	}
}
