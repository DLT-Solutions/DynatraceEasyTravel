package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

/**
 * Demo implementation of a js error seen at chip.de
 * 
 * @author cwat-moehler
 */
public class ChipDeInspiredError1 extends JavaScriptError {

	public ChipDeInspiredError1(String currentPageUrl) {

		super(currentPageUrl);
		setFile("<domain>//javascript/pkg/vendors/jquery-1.10.2.min.js");
		setLine(1);
		setDefaultMessage("Unexpected end of input");

		
		String stackTrace = "SyntaxError: Unexpected end of input\r\n"
			+ "   at Object.parse (native)\n"
			+ "   at Function.ct.extend.parseJSON (<domain>/javascript/pkg/vendors/jquery-1.10.2.min.js:1:15166)\n"
			+ "   at require.callback (<domain>/Store/cart/cart.jsp?dcs_action=additemtocart&url_catalog_ref_id=BOM0020-TFRGR-XL&url_product_id=BOM0020&url_quantity=1:80:22)\n"
			+ "   at Object.b.execCb (<domain>/javascript/pkg/common/bootstrap.js:1:28752)\n"
			+ "   at Object.x.check (<domain>/javascript/pkg/common/bootstrap.js:1:22988)\n"
			+ "   at Object.<anonymous> (<domain>/javascript/pkg/common/bootstrap.js:1:25253)\n"
			+ "   at <domain>/javascript/pkg/common/bootstrap.js:1:17248\n"
			+ "   at <domain>/javascript/pkg/common/bootstrap.js:1:25637\n"
			+ "   at each (<domain>/javascript/pkg/common/bootstrap.js:1:16774)\n"
			+ "   at Object.x.emit (<domain>/javascript/pkg/common/bootstrap.js:1:25605)";
		
		setStackTrace(BrowserFamily.Chrome, stackTrace);
		setStackTrace(BrowserFamily.Opera, stackTrace);
		setStackTrace(BrowserFamily.Firefox, stackTrace);
	}


}
