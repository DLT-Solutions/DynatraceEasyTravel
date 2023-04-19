package com.dynatrace.diagnostics.uemload.headless;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;

@ClientEndpoint
public class WsClientEndpoint {
	 @OnMessage
	 public void onMessage(String message) {
	 }
}