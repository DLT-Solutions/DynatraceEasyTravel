package com.dynatrace.diagnostics.uemload.headless;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class ChromeDevToolsComands {
    
	private static final Gson gson = new Gson();
	private static final Logger LOGGER = Logger.getLogger(ChromeDevToolsComands.class.getName());
	
	public String getExtraHeadersCommnad(Map<String,String> headers, int commandId) {
        Map<String,Map<String,String>> params = new HashMap<>();
        params.put("headers", headers);
        return getJsonCommand("Network.setExtraHTTPHeaders", params, commandId);
    }
	
    public String getUserAgentOverrideCommand(String userAgent, int commandId) {
        Map<String,Object> params = new HashMap<>();
        params.put("userAgent", userAgent);
        return getJsonCommand("Network.setUserAgentOverride", params, commandId);
    }

    public String getNetworkEnableCommand(int commandId) {
        return getJsonCommand("Network.enable", commandId);
    }
    
    public String getNetworkDisableCommand(int commandId) {
        return getJsonCommand("Network.disable", commandId);
    }
    
    public String getGetCookiesCommand(int commandId) {
    	return getJsonCommand("Network.getAllCookies", commandId);
    }
    
    public String getClearCookiesCommand(int commandId) {
    	return getJsonCommand("Network.clearBrowserCookies", commandId);
    }
    
    public String getSetVisitorCookieCommand(String visitorId, int commandId) {
    	Map<String,Object> params = new HashMap<>();
    	params.put("name", "rxVisitor");
    	params.put("value", visitorId);
    	params.put("domain",".dynatracelabs.com");
    	params.put("path","/");
    	return getJsonCommand("Network.setCookie", params, commandId);
    }
   
    private String getJsonCommand(String command, int id) {
        return getJsonCommand(command, Collections.emptyMap(), id);
    }

    private String getJsonCommand(String command, Map<String,? extends Object> params, int id) {
        String json = gson.toJson(getCommand(command, params, id));
        LOGGER.fine(json);
        return json;
    }
    
    private Map<String,Object> getCommand(String command, Map<String,? extends Object> params, int id) {
        Map<String,Object> res = new HashMap<>();
        res.put("params", params);
        res.put("method", command);
        res.put("id", id);
        return res;
    }
}
