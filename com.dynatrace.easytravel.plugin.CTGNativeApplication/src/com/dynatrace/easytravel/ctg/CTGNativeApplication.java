package com.dynatrace.easytravel.ctg;

import java.io.IOException;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.ipc.NativeApplication;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPlugin;
import com.ibm.ctg.client.ECIRequest;
import com.ibm.ctg.client.JavaGateway;

public class CTGNativeApplication extends AbstractPlugin implements
		NativeApplication {
	private static final Logger log = LoggerFactory.make();

	private static final String charset = "IBM037";

	private String sHost;
	private int iPort;
	private String sProg;
	private String sUid;
	private String sPwd;
	private String sTran;
	private String sCics;

	@Override
	public void setChannel(String channel) {
	}

	@Override
	public String sendAndReceive(String creditCard) throws IOException {
		final EasyTravelConfig conf = EasyTravelConfig.read();

        sHost = conf.ctgHostName;
        iPort = conf.ctgPort;
        sProg = conf.ctgProgram;
        sUid = conf.ctgUserId;
        sPwd = conf.ctgPassword;
        sTran = conf.ctgTransaction;
        sCics = conf.ctgServer;

    	boolean connected = connect();
		if (connected) {
	        byte sCommarea[] = creditCard.getBytes(charset);
	        ECIRequest req= new ECIRequest(ECIRequest.ECI_SYNC,
	        		sCics, sUid, sPwd, sProg, sTran, sCommarea, 100, ECIRequest.ECI_NO_EXTEND, ECIRequest.ECI_LUW_NEW);
	        gateway.flow(req);
	        String result;
	        if (req.getRc() == 0) {
	        	result = new String(sCommarea, charset);
	        } else {
	        	result = INCORRECT;
		        log.info("Rc:" + req.getRc() +
		                " Abd:"+req.Abend_Code+
		                " CCs:"+req.CicsClientStatus+
		                " CSs:"+req.CicsServerStatus+
		                " Msg:"+req.getMessageId());
	        }
			disonnect();
			return result;
		}
		return INCORRECT;
	}

	private JavaGateway gateway;

	public boolean connect() {
		try {
	        gateway = new JavaGateway(sHost,iPort);
			return true;
		} catch (IOException e) {
			log.warn(e.getMessage());
			log.info("Exception details", e);
			return false;
		}
	}

	private void disonnect() {
		try {
			gateway.close();
		} catch (IOException e) {
			log.warn(e.getMessage());
			log.info("Exception details", e);
		}
	}
}
