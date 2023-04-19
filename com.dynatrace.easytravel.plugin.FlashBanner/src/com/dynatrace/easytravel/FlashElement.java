package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;

/**
 * This plugin adds a flash banner to the footer
 *
 * @author cwat-moehler
 */
public abstract class FlashElement extends AbstractPagePlugin {

    // injected by the PluginServlet
    public boolean showFooter = true;

    @Override
    public String getHeadInjection() {
        String dynatraceAPI = "<script type=\"text/javascript\" src=\"" + getPath("dtagentApi", "js") + "\"></script>\n";
        String swfObject = "<script type=\"text/javascript\" src=\"" + getPath("swfobject", "js") + "\"></script>\n";

        String swfScript =
              "\n\n"
            + "<script type=\"text/javascript\">\n"
            + "   // For version detection, set to min. required Flash Player version, or 0 (or 0.0.0), for no version detection. \n"
            + "  var swfVersionStr = \"11.1.0\"; \n"
            + "  var xiSwfUrlStr = \"\";\n"
            + "  var xiSwfUrlStr = \"\";\n"
            + "  var flashvars = { };\n"
            + "  flashvars.contentsource = \"/plugins/" + getFlashFileName() + "/swf/bannercontent.json\";\n"
            + "  var params = {};\n"
            + "  params.quality = \"high\";\n"
            + "  params.bgcolor = \"#e2dddd\";\n"
            + "  params.allowscriptaccess = \"sameDomain\";\n"
            + "  params.allowfullscreen = \"true\";\n"
            + "  var attributes = {};\n"
            + "  attributes.id = \"FlashBanner\";\n"
            + "  attributes.name = \"FlashBanner\";\n"
            + "  attributes.align = \"middle\";\n"
            + "  swfobject.embedSWF(\"" + getSWFPath() + "\", \"flashContent\",\"860\", \"70\", swfVersionStr, xiSwfUrlStr, flashvars, params, attributes);\n"
            + "  // JavaScript enabled so display the flashContent div in case it is not replaced with a swf object.\n"
            + "  swfobject.createCSS(\"#flashContent\", \"display:block;text-align:left;\");\n"
            + "</script>\n\n";

        return dynatraceAPI + swfObject + swfScript;
    }

    @Override
    public String getFooter() {
        if (!showFooter) {
            return null;
        }

        String flashContentPlaceholder =
              "\n\n"
            + "<div id=\"flashContent\" style=\"padding-bottom: 20px\">\n"
            + " 	<p>To view this page ensure that Adobe Flash Player version 11.1.0 or greater is installed.</p>\n"
            + "     <script type=\"text/javascript\">\n"
            + "       var pageHost = ((document.location.protocol == \"https:\") ? \"https://\" : \"http://\");\n"
            + "       document.write(\"<a href='http://www.adobe.com/go/getflashplayer'><img src='\" + pageHost + \"www.adobe.com/images/shared/download_buttons/get_flash_player.gif' alt='Get Adobe Flash player' /></a>\" );\n"
            + "     </script>\n"
            + "</div>\n"
            + "<div style=\"padding-bottom: 10px;width:100%; height: 0px;\"></div>\n\n";


        String noScriptTag =
            "<noscript>\n" +
            "  <object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" width=\"860\" height=\"70\" id=\"Main\" name=\"Main17 ddda\">\n" +
            "    <param name=\"movie\" value=\"" + getSWFPath() + "\" /><param name=\"quality\" value=\"high\" /><param name=\"bgcolor\" value=\"#e2dddd\" /><param name=\"allowScriptAccess\" value=\"sameDomain\" /><param name=\"allowFullScreen\" value=\"true\" />\n" +
            "    <!--[if !IE]>-->\n" +
            "      <object type=\"application/x-shockwave-flash\" data=\"" + getSWFPath() + "\" width=\"860\" height=\"70\">\n" +
            "        <param name=\"quality\" value=\"high\" /><param name=\"bgcolor\" value=\"#e2dddd\" /><param name=\"allowScriptAccess\" value=\"sameDomain\" /><param name=\"allowFullScreen\" value=\"true\" />\n" +
            "   <!--<![endif]-->\n" +
            "   <!--[if gte IE 6]>--><p>Either scripts and active content are not permitted to run or Adobe Flash Player version 11.1.0 or greater is not installed.</p><!--<![endif]-->\n" +
            "       <a href=\"http://www.adobe.com/go/getflashplayer\">\n" +
            "         <img src=\"http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif\" alt=\"Get Adobe Flash Player\" />\n" +
            "       </a>" +
            "    <!--[if !IE]>-->\n"+
            "      </object>\n" +
            "    <!--<![endif]-->\n" +
            "  </object>\n" +
            "</noscript>\n\n";

        return flashContentPlaceholder + noScriptTag;
    }

    protected abstract String getFlashFileName();

    protected String getResourceFolder() {
    	return getFlashFileName();
    }

    private String getSWFPath() {
        return getPath(getFlashFileName(), "swf");
    }

    private String getPath(String filename, String filetype) {
    	return getPath(filename, filetype, filetype);
    }

    private String getPath(String filename, String filetype, String foldername) {
    	return "/plugins/" + getName() + "/" + foldername + "/" + filename + "." + filetype;
    }

    @Override
    public String toString() {
        return "FlashBanner [showFooter=" + showFooter + "]";
    }
}
