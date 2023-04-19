package com.dynatrace.diagnostics.uemload.headless;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.dynatrace.easytravel.util.TextUtils;
import com.google.gson.Gson;

public class ChromeDevToolsConnection {
	private static final Logger LOGGER = Logger.getLogger(ChromeDevToolsConnection.class.getName());
	private static final Gson gson = new Gson();
	private final String host;
	private final int port;
	private final ChromeDevToolsComands chromeCommands = new ChromeDevToolsComands();	
	
	private ClientManager client;
    private Session session;
    private final AtomicInteger commandId = new AtomicInteger(0);
	
	public ChromeDevToolsConnection(int debugPort) {
		this.host = "localhost";
		this.port = debugPort;
	}
		
	public synchronized void startConnection() throws DeploymentException, IOException, URISyntaxException {
		String webSocketUrl = getWebSocketUrl();
		LOGGER.log(Level.FINE, TextUtils.merge("ChromeDevToolsConnection for host {0}:{1} users URL: {2}", host, port, webSocketUrl));		
		
		client = ClientManager.createClient();
        session = client.connectToServer(WsClientEndpoint.class, new URI(webSocketUrl));
        session.setMaxIdleTimeout(0);
	}
	
    @SuppressWarnings("unchecked")
	private String getWebSocketUrl() throws IOException {
        HttpURLConnection urlConnection;

        URL url = new URL(String.format("http://%s:%d/json", host, port));
        urlConnection = (HttpURLConnection) url.openConnection();

        Map<String,String>[] targets;        
        try (InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream()) ){
            targets = gson.fromJson(reader, Map[].class);
        }
        return Arrays.stream(targets)
                .filter(jsonMap -> jsonMap.get("type").equals("page"))
                .map(jsonMap -> jsonMap.get("webSocketDebuggerUrl"))
                .findFirst().get();
    }
		
	public void closeConnection() throws IOException {
		checkSession();
		session.close();
	}
	
    public void enableNetwork() throws IOException {
        executeCommand(chromeCommands.getNetworkEnableCommand(getCommandId()));
    }
        
    
    public void disableNetwork() throws IOException {
        executeCommand(chromeCommands.getNetworkDisableCommand(getCommandId()));
    }
    	
	public void setUserAgent(String userAgent) throws IOException {
		executeCommand(chromeCommands.getUserAgentOverrideCommand(userAgent, getCommandId()));		
	}
		
	public void setLocation(String location) throws IOException {
        Map<String,String> headers = new HashMap<>();
        headers.put("x-forwarded-for", location);
        executeCommand(chromeCommands.getExtraHeadersCommnad(headers, getCommandId()));
	}
	
	public void getCookies() throws IOException {
		executeCommand(chromeCommands.getGetCookiesCommand(getCommandId()));
	}
	
	public void clearCookies() throws IOException {
		executeCommand(chromeCommands.getClearCookiesCommand(getCommandId()));
	}
	
	public void setVisitorIdCookie(String visitorId) throws IOException {
		executeCommand(chromeCommands.getSetVisitorCookieCommand(visitorId, getCommandId()));
	}
	
	private synchronized void checkSession() {
		if(session == null) {
			throw new IllegalStateException();
		}
	}
	
    private synchronized void executeCommand(String command) throws IOException {
    	checkSession();
        session.getBasicRemote().sendText(command);
    }
	       
	
    private int getCommandId() {
    	return commandId.getAndIncrement();
    }
}