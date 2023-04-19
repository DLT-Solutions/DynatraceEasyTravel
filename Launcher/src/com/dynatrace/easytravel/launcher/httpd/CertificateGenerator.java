package com.dynatrace.easytravel.launcher.httpd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;

public class CertificateGenerator {
	private static final Logger log = LoggerFactory.make();
	private static final boolean disableCertificateGeneration = EasyTravelConfig
			.read().disableApacheCertificateGeneration;
	private static String installDir = Directories.getInstallDir().getAbsolutePath() + File.separator;
	private static String configDir = Directories.getConfigDir().getAbsolutePath() + File.separator;
	private static String openSSLDir = installDir + "openssl" + File.separator;
	private static String scriptsDir = installDir + ApacheHttpdUtils.APACHE_VERSION + File.separator + "scripts" + File.separator;
	private static String openSSLConfigFile = "openssl-easytravel.cnf";
	private static String openSSLConfigFileTemplate = "openssl-easytravel.cnf.template";
	private static String easyTravelSanCertFile = "easytravel-san.crt";
	private static String easyTravelSanKeyFile = "easytravel-san.key";
	private static String easyTravelSanCertReqFile = "easytravel-san.req";
	private static String CACertFile = "CA.cer";
	private static String CAKeyFile = "CA.key";
	private static String CASerialFile = "easytravel-CAserial";

	public static void generateCertificate() {
		if (!disableCertificateGeneration) {

			restoreTemplate(scriptsDir + openSSLConfigFileTemplate, configDir + openSSLConfigFile);
			replaceStringInFile(configDir + openSSLConfigFile, "DNS.1 =", "DNS.1 = " + getSslHostAddress());

				String quote ="";
				String openSSLPath = "openssl";
				if (SystemUtils.IS_OS_WINDOWS) {
					openSSLPath = openSSLDir + openSSLPath + ".exe";
					quote ="\"";
				}
				
				executeCommand(openSSLPath + "|genrsa|-out|" + quote + configDir + easyTravelSanKeyFile +quote + "|2048");
				executeCommand(openSSLPath + "|req|-config|" + quote + configDir + openSSLConfigFile +quote + "|-new|-out|"
						+ quote + configDir + easyTravelSanCertReqFile +quote + "|-key|" +quote + configDir+ easyTravelSanKeyFile +quote);
				executeCommand(openSSLPath + "|x509|-req|-in|" +quote + configDir + easyTravelSanCertReqFile + quote +"|-out|"
						+ quote + configDir + easyTravelSanCertFile + quote + "|-CAkey|" + quote + scriptsDir + CAKeyFile + quote +"|-CA|"
						+ quote + scriptsDir + CACertFile +quote + "|-days|365|-CAcreateserial|-CAserial|" + quote +configDir + CASerialFile
						+ quote + "|-extensions|v3_req|-extfile|" + quote + configDir + openSSLConfigFile + quote);
				
		} else {
			log.error("Apache Certificate Generation is currently disabled.");
		}
	}
	private static void replaceStringInFile(String configFile, String textToReplace, String replacement) {
		try {
			String input = "";
			File config = new File(configFile);
			input = FileUtils.readFileToString(config);
			input = input.replace(textToReplace, replacement);

			FileUtils.writeStringToFile(config, input);

		} catch (Exception e) {
			log.error("Problem reading/writing OpenSSL config file.", e);
		}

	}

	private static void restoreTemplate(String templateFileName, String newFileName) {
		File newFile = new File(newFileName);
		File templateFile = new File(templateFileName);
		try {
			FileUtils.copyFile(templateFile, newFile);
		} catch (IOException e) {
			log.error("Problem while restoring template file.", e);
		}

	}

	public static String executeCommand(String command) {
		log.info("COMMAND: "+command);
		StringBuilder outputBuilder = new StringBuilder();
		String parameters[] = command.split("\\|");
		final ProcessBuilder pb;
		pb = new ProcessBuilder(parameters);

		if (SystemUtils.IS_OS_WINDOWS) {
			Map<String, String> env = pb.environment();
			env.put("OPENSSL_CONF", configDir + openSSLConfigFile);
			env.put("RANDFILE", configDir + ".rnd");
		}
		pb.redirectErrorStream(true);
		BufferedReader inStreamReader = null;
		try {
			Process process = pb.start();
			inStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			do {
				line = inStreamReader.readLine();
				outputBuilder.append(line).append("\n");
			} while (line != null);
			
			
		} catch (IOException e) {
			log.error("Couldn't execute command.", e);
			return "ERROR";
		} finally {
			try{
				if(inStreamReader != null){
					inStreamReader.close();
				}			
			} catch(IOException e){
				log.error("Couldn't close stream reader.", e);
			}
		}
		log.info("OpenSSL: "+outputBuilder.toString());
		return outputBuilder.toString();
	}

	public static String getSslHostAddress() {
		EasyTravelConfig config = EasyTravelConfig.read();
		String host = config.apacheWebServerSslHost;

		if (host == null || host.trim().equals(BaseConstants.EMPTY_STRING)) {
			host = getLocalHostAddress();
		}
		return host;
	}

	private static String getLocalHostAddress() {
		try {
			InetAddress addr = InetAddress.getLocalHost(); // NOSONAR - we don't
															// care too much
															// about multi-home
															// machines here
			return addr.getHostAddress();
		} catch (UnknownHostException e) {
			log.error("Cannot get localhost", e);
			return "localhost";
		}
	}

	public static String inspectCertificate() {
		String openSSLPath = "openssl";
		String quote ="";
		if (SystemUtils.IS_OS_WINDOWS) {
			openSSLPath = openSSLDir + openSSLPath + ".exe";
			quote ="\"";
		}
		String output = "";
			output = CertificateGenerator
					.executeCommand(openSSLPath + "|x509|-in|" + quote + configDir + "easytravel-san.crt"+quote+"|-text|-noout");
		return output;
	}
}
