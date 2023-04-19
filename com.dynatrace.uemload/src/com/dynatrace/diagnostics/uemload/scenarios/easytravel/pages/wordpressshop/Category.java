package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;

/**
 * 
 * @author Michal.Bakula
 *
 */
public enum Category {
	ACCESSORIES(EtPageType.WORDPRESS_SHOP_ACCESSORIES, new Product[] { Product.BEANIE, Product.BELT, Product.CAP, Product.SUNGLASSES, Product.BEANIE }),
	CLOTHING(EtPageType.WORDPRESS_SHOP_CLOTHING, new Product[] { Product.LOGO_COLLECTION }),
	DECOR(EtPageType.WORDPRESS_SHOP_DECOR, new Product[] { Product.WORDPRESS_PENNANT }),
	HOODIES(EtPageType.WORDPRESS_SHOP_HOODIES, new Product[] { Product.HOODIE, Product.HOODIE_WITH_LOGO, Product.HOODIE_WITH_ZIPPER }),
	MUSIC(EtPageType.WORDPRESS_SHOP_MUSIC, new Product[] { Product.ALBUM, Product.SINGLE }),
	TSHIRTS(EtPageType.WORDPRESS_SHOP_TSHIRTS, new Product[] { Product.V_NECK_T_SHIRT, Product.T_SHIRT, Product.LONG_SLEEVE_TEE, Product.POLO, Product.T_SHIRT_WITH_LOGO })
	;
	
	private EtPageType page;
	private Product[] products;
	
	private Category(EtPageType page, Product[] products) {
		this.page = page;
		this.products = products;
	}
	
	public EtPageType getPageType() {
		return page;
	}
	
	public Product[] getProducts() {
		return products;
	}
}
