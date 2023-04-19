package com.dynatrace.easytravel;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.xml.bind.DatatypeConverter;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.GeneralTrustManager;

public class JavascriptBootstrapAgent extends AbstractGenericPlugin {
	private String initCode;

	private static Logger log = LoggerFactory.make();	
	public static String getInitCode(String systemProfile, String applicationName){
        //trust all ssl certificates, used to get the jsagent from our rest api
        TrustManager[] trustAllCerts = new TrustManager[] { 
        	new GeneralTrustManager()
        };

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            log.warn(e.getMessage());
        } catch (KeyManagementException e) {
        	log.warn(e.getMessage());
        }
        StringBuilder initCode = new StringBuilder();
        URL url = null;
        try {
    	    final EasyTravelConfig config = EasyTravelConfig.read();
            url = new URL(String.format("%sapi/v1/profiles/%s/applications/%s/javascriptagent/initcode", config.dtServerWebURL, systemProfile, applicationName));

            String encoding = DatatypeConverter.printBase64Binary((config.dtServerUsername + ":" + config.dtServerPassword).getBytes());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            connection.connect();
            InputStream is = connection.getInputStream();
            if(connection.getResponseCode() != 200) {
                if(is == null) {
                    is = connection.getErrorStream();
                }
                initCode.append("<script>throw 'Initcode couldn\'t be loaded, error in REST adapter! request url was ");
                initCode.append(url.toString());
                initCode.append("';</script>");
            }

            Scanner s = new Scanner(is);
            s.useDelimiter("\\A"); //for details on this see http://stackoverflow.com/a/5445161/1567152
            while (s.hasNext()) {
                initCode.append(s.next());
            }
            s.close();
        } catch (IOException e) {
        	log.warn("Couldn't get JS Agent InitCode: " + e.getMessage());
            initCode.append("Error in JavascriptBootstrapAgent problem pattern: Couldn't read rest response for url '");
            initCode.append(url == null ? "null" : url.toString());
            initCode.append("', error: ");
            initCode.append(e.getMessage());
        } catch (NullPointerException e){
        	log.warn("Couldn't get JS Agent InitCode: " + e.getMessage());
            initCode.append("Error in JavascriptBootstrapAgent problem pattern: Couldn't read rest response for url '");
            initCode.append(url == null ? "null" : url.toString());
            initCode.append("', error: ");
            initCode.append(e.getMessage());
        }
        return initCode.toString();
	}
	public JavascriptBootstrapAgent(){
		super();
		initCode = getInitCode("easyTravel", "easyTravel%20portal");
	}
    @Override
    public String doExecute(String location, Object... context) {
    	if(DtVersionDetector.isDetectedVersionGreaterOrEqual(65)){
    		return initCode;
    	}
		return "<script type=\"text/javascript\" src=\"/dtagent_bootstrap.js?app=easyTravel+portal\"></script>";
    }
}
