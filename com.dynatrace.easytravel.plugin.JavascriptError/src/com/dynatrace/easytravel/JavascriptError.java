package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class JavascriptError extends AbstractGenericPlugin {

    @Override
    public String doExecute(String location, Object... context) {
        return "jQuery('label').click(function() { " +
        		    "var elem = jQuery(this); " +
    		        "if (!elem.hasClass('__hover__anim')) { " +
    		            "var col = elem.css('color'); " +
    		            "elem.addClass('__hover__anim'); " +
    		            "elem.animate({color: 'black'}, 10, " +
    		                "function () { " +
    		                    "elem.animate({color: col}, 200, function() { " +
    		                        "elem.removeClass('__hover__anim'); " +
    		                    "}); " +
    		                "} " +
    		            "); " +
    		            "return jQuery('#' + elem.attr('for')).val() != ''; " +
    		        "} " +
        		"}); ";
    }
}
