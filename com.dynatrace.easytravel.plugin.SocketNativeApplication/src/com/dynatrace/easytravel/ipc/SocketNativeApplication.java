package com.dynatrace.easytravel.ipc;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPlugin;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.oneagent.sdk.api.OutgoingRemoteCallTracer;
import com.dynatrace.oneagent.sdk.api.enums.ChannelType;

import ch.qos.logback.classic.Logger;

/*
 * See http://v01ver-howto.blogspot.com/2010/04/howto-use-named-pipes-to-communicate.html
 */
public class SocketNativeApplication extends AbstractPlugin implements NativeApplication {

	private static final Logger log = LoggerFactory.make();

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.NATIVEAPP);

	@Override
	public String sendAndReceive(String toSend) throws IOException {

		plugins.execute(PluginConstants.NATIVEAPP_SENDANDRECEIVE, toSend);
		
		EasyTravelConfig CONFIG = EasyTravelConfig.read();
		final int SOCKET_PORT = CONFIG.creditCardAuthorizationSocketPort;
		String host = CONFIG.creditCardAuthorizationHost;
		if (host == null) {
			host = "localhost";
		}

		if(TaggingAdkOneAgentSdkUtils.isTaggingAdkActive()) {
			return tagCallToCCA(toSend, host, SOCKET_PORT);
		} else if (TaggingAdkOneAgentSdkUtils.isOneAgentSdkActive()) {
			return traceCallToCCA(toSend, host, SOCKET_PORT);
		} else {
			return send(toSend, "", host, SOCKET_PORT);
		}
	}

	@Override
	public void setChannel(String channel) {
		// dummy, not used here
	}

	public static /* synchronized */ String send(String toSend, String strTag, String host, int port)
			throws UnknownHostException, IOException {
		// Connect to socket
		Socket socket = new Socket(host, port);
		try {
			byte[] tag = strTag.getBytes("UTF-8");
			// TODO: should we use UTF-16 and convert correctly on the other
			// side?
			byte[] data = toSend.getBytes("UTF-8");
			byte[] send;

			if (tag.length > 0) {
				send = new byte[tag.length + 1 + data.length];
				System.arraycopy(tag, 0, send, 0, tag.length);
				send[tag.length] = '|'; // delimiter between tag and actual data
				System.arraycopy(data, 0, send, tag.length + 1, data.length);
				log.info("Sending tag and data '" + new String(send) + "' via socket '" + port + "'");
			} else {
				send = new byte[data.length];
				System.arraycopy(data, 0, send, 0, data.length);
				log.info("Sending data '" + new String(send) + "' via socket '" + port + "'");
			}

			// write to socket
			IOUtils.write(send, socket.getOutputStream());

			// read response
			byte[] recBuffer = new byte[512];
			int count = socket.getInputStream().read(recBuffer);

			// cut off one trailing nul-termination-byte
			if (count > 0 && recBuffer[count - 1] == 0) {
				count--;
			}

			String echoResponse = new String(recBuffer, 0, count);
			log.info("Response(" + count + "): " + echoResponse);
			return echoResponse;
		} finally {
			socket.close();
		}
	}
	
	
	/**
	 * @author Michal.Bakula
	 * @param toSend
	 * @param host
	 * @param port
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private static String tagCallToCCA(String toSend, String host, int port) throws UnknownHostException, IOException {
		String strTag = TaggingAdkOneAgentSdkUtils.getTaggingAdkInstance().getTagAsString();
		TaggingAdkOneAgentSdkUtils.getTaggingAdkInstance().linkClientPurePath(false);
		log.info("Tagging.getTagAsString: tag='" + strTag + "'");
		return send(toSend, strTag, host, port);
	}
	
	/**
	 * @author Michal.Bakula
	 * @param toSend
	 * @param host
	 * @param port
	 * @return
	 */
	private static String traceCallToCCA(String toSend, String host, int port) {
		OutgoingRemoteCallTracer externalOutgoingRemoteCall = TaggingAdkOneAgentSdkUtils.getOneAgentAdkInstance().traceOutgoingRemoteCall(
				"validateCreditCard", "CreditCardValidation", "proto://easyTravel/CreditCardService",
				ChannelType.TCP_IP, String.format("%s:%d", host, port));
		externalOutgoingRemoteCall.start();
		try {
			String strTag = externalOutgoingRemoteCall.getDynatraceStringTag();
			log.info("OneAgentSDK.getDynatraceStringTag(): tag='" + strTag + "'");
			return send(toSend, strTag, host, port);
		} catch (Exception e) {
			externalOutgoingRemoteCall.error(e);
			return NativeApplication.FAILED + " - OneAgent SDK remote call failed with exception:\n" + e.getMessage();
		} finally {
			externalOutgoingRemoteCall.end();
		}
	}
}


