package com.dynatrace.easytravel.launcher.procedures;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * Tomcat can be configured to be stopped remotely via a socket. This class sends a shutdown command
 * to the Tomcat instance that has to be stopped.
 *
 * @author martin.wurzinger
 */
public class TomcatShutdownCommand {

    private static final Logger LOGGER = Logger.getLogger(TomcatShutdownCommand.class.getName());

    private final InetAddress address;
    private final int port;
    private final String shutdownMessage;

    public TomcatShutdownCommand(InetAddress address, int port) {
        this(address, port, BaseConstants.TOMCAT_SHUTDOWN);
    }

    public TomcatShutdownCommand(InetAddress address, int port, String shutdownMessage) {
        if (address == null) {
            throw new IllegalArgumentException("No address defined.");
        }
        this.address = address;

        this.port = port;

        this.shutdownMessage = shutdownMessage;
        if (shutdownMessage == null || shutdownMessage.isEmpty()) {
            throw new IllegalArgumentException("No shutdown message defined.");
        }
    }

    public Feedback execute() {

        try {
            Socket socket = new Socket(address, port);
            try {
            	byte[] buffer = shutdownMessage.getBytes();

	            OutputStream outStream = socket.getOutputStream();
	            try {
		            outStream.write(buffer, 0, buffer.length);
		            outStream.flush();
		            
		            LOGGER.info(TextUtils.merge("Shutdown command successfully sent to ''{0}:{1}''.", address.getHostName(), Integer.toString(port)));
		            
		            return Feedback.Neutral;
	            } finally {
	            	IOUtils.closeQuietly(outStream);
	            }
            } finally {
            	socket.close();
            }
        } catch (UnknownHostException e) {
            LOGGER.log(Level.SEVERE, TextUtils.merge("Unable to send shutdown command to ''{0}:{1}''. Tomcat host cannot be found.", address.getHostName(), Integer.toString(port)), e);
        } catch (ConnectException e) {
        	// this exception usually indicates that the host was not available any more
        	// we handle specially with the stacktrace only logged for FINE
        	LOGGER.log(Level.WARNING, TextUtils.merge("Unable to send shutdown command to ''{0}:{1}'', Tomcat did not respond any more: " + e.getMessage(), address.getHostName(), Integer.toString(port)));
            if (LOGGER.isLoggable(Level.FINE)) {
            	LOGGER.log(Level.FINE, "Unable to send shutdown command", e);
            }
        } catch (SocketException e) {
        	// depending on the error message this might be something that is ok during shutdown
        	if(e.getMessage().contains("Connection reset by peer")) {
	        	// this exception usually indicates that the host was not available any more
	        	// we handle specially with the stacktrace only logged for FINE
	        	LOGGER.log(Level.WARNING, TextUtils.merge("Unable to send shutdown command to ''{0}:{1}'', Tomcat did not respond any more: " + e.getMessage(), address.getHostName(), Integer.toString(port)));
	            if (LOGGER.isLoggable(Level.FINE)) {
	            	LOGGER.log(Level.FINE, "Unable to send shutdown command", e);
	            }
        	} else {
                LOGGER.log(Level.SEVERE, TextUtils.merge("Unable to send shutdown command to Tomcat at ''{0}:{1}''.", address.getHostName(), Integer.toString(port)), e);
        	}
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, TextUtils.merge("Unable to send shutdown command to Tomcat at ''{0}:{1}''.", address.getHostName(), Integer.toString(port)), e);
        }

        return Feedback.Failure;
    }
}
