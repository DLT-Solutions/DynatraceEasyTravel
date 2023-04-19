package com.dynatrace.easytravel.ipc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.SystemUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
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
public class NamedPipeNativeApplication extends AbstractPlugin implements NativeApplication {

	private static final Logger log = LoggerFactory.make();

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.NATIVEAPP);

	// Initialize dynaTrace Tagging ADK or OneAgent SDK support to send the purepath-id over the wire

	String channel;

	@Override
	public void setChannel(String channel) {
		this.channel = channel;
	}

	@Override
	public String sendAndReceive(String toSend) throws IOException {
		if (channel == null) {
			throw new IOException("Need to have a channel in order to send/receive via Named Pipe.");
		}

		plugins.execute(PluginConstants.NATIVEAPP_SENDANDRECEIVE, toSend);

		if(TaggingAdkOneAgentSdkUtils.isTaggingAdkActive()) {
			return tagCallToCCA(toSend, channel);
		} else if (TaggingAdkOneAgentSdkUtils.isOneAgentSdkActive()) {
			return traceCallToCCA(toSend, channel);
		} else {
			return send(toSend, channel, "");
		}
	}

	/*
	 * philipp.grasboeck JLT-59890:
	 * Consider making this method synchronized again since the NamedPipe communication has troubles
	 * under high concurrency;
	 * For now, this is not needed, since the high concurrency load testing now use
	 * SocketNativeApplication under Linux.
	 */
	public static /* synchronized */ String send(String toSend, String channel, String strTag)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		// Connect to the pipe
		RandomAccessFile writepipe = getWritepipe(channel);
		try {
			final RandomAccessFile readpipe = getReadpipe(channel, writepipe);
			try {
				byte[] tag = strTag.getBytes("UTF-8");
				// TODO: should we use UTF-16 and convert correctly on the other
				// side?
				byte[] data = toSend.getBytes("UTF-8");
				byte[] send;

				if (tag.length > 0) {
					send = new byte[tag.length + 1 + data.length];
					System.arraycopy(tag, 0, send, 0, tag.length);
					send[tag.length] = '|'; // delimiter between tag and actual
											// data
					System.arraycopy(data, 0, send, tag.length + 1, data.length);
					log.info("Sending tag and data '" + new String(send) + "' via Named Pipe '" + channel + "'");
				} else {
					send = new byte[data.length];
					System.arraycopy(data, 0, send, 0, data.length);
					log.info("Sending data '" + new String(send) + "' via Named Pipe '" + channel + "'");
				}

				// write to pipe
				writepipe.write(send);

				// read response
				String echoResponse = readpipe.readLine();
				log.info("Response: " + echoResponse);
				return echoResponse;
			} finally {
				readpipe.close();
			}
		} finally {
			writepipe.close();
		}
	}
	
	private static RandomAccessFile getWritepipe(String channel) throws FileNotFoundException {
		RandomAccessFile writepipe;
		while (true) {
			try {
				writepipe = new RandomAccessFile(BaseConstants.PIPE_PREFIX + channel, "rw");
				break;
			} catch (FileNotFoundException e) {
				// JLT-41827/JLT-34718: Retry named pipe communication when we get "All pipe instances are busy" on Windows
				if (e.getMessage().contains("All pipe instances are busy")) {
					log.warn("Could not get pipe, trying again: " + e.getMessage());
					continue;
				}
				throw e;
			}
		}
		return writepipe;
	}
	
	private static RandomAccessFile getReadpipe(String channel, RandomAccessFile writepipe) throws FileNotFoundException {
		return (SystemUtils.IS_OS_WINDOWS) ? writepipe : new RandomAccessFile(BaseConstants.PIPE_PREFIX + channel + "Back", "r"); 
	}
	
	/**
	 * @author Michal.Bakula
	 * @param toSend
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	private static String tagCallToCCA(String toSend, String channel) throws IOException {
		String strTag = TaggingAdkOneAgentSdkUtils.getTaggingAdkInstance().getTagAsString();
		TaggingAdkOneAgentSdkUtils.getTaggingAdkInstance().linkClientPurePath(false);
		log.info("Tagging.getTagAsString: tag='" + strTag + "'");
		return send(toSend, channel, strTag);
	}
	
	
	/**
	 * @author Michal.Bakula
	 * @param toSend
	 * @param channel
	 * @return
	 */
	private static String traceCallToCCA(String toSend, String channel) {
		OutgoingRemoteCallTracer externalOutgoingRemoteCall = TaggingAdkOneAgentSdkUtils.getOneAgentAdkInstance().traceOutgoingRemoteCall(
				"validateCreditCard", "CreditCardValidation", "proto://easyTravel/CreditCardService",
				ChannelType.NAMED_PIPE, channel);
		externalOutgoingRemoteCall.start();
		try {
			String strTag = externalOutgoingRemoteCall.getDynatraceStringTag();
			log.info("OneAgentSDK.getDynatraceStringTag(): tag='" + strTag + "'");
			return send(toSend, channel, strTag);
		} catch (Exception e) {
			externalOutgoingRemoteCall.error(e);
			return NativeApplication.FAILED + " - OneAgent SDK remote call failed with exception: " + e.getMessage();
		} finally {
			externalOutgoingRemoteCall.end();
		}
	}
}