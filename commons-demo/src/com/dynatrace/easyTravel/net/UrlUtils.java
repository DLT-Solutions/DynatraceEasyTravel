package com.dynatrace.easytravel.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.dynatrace.easytravel.util.EasySSLProtocolSocketFactory;
import com.dynatrace.easytravel.util.GeneralTrustManager;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.net.InetAddresses;


/**
 * Utility class for retrieving data from URLs and checking URL availability (incl. Proxy error handling).
 * Note: all methods fire GET request, except the retrieveDataPost() and retrieveRawDataPost() methods, that fire POST requests.
 * If you want to check whether a resources is available via POST, use checkConnect(), since
 * checkRead() will on a "POST resource" will receive HTTP 405 Method Not Allowed.
 * If you use one of the retrieveDataPost() and retrieveRawDataPost() methods, you have to specify
 * the Content-Type header along with the POST request body.
 */
public class UrlUtils {

    /**
     * Result constants for checkAvailability()
     *
     * @author cwat-pgrasboe
     */
    public enum Availability {
    	/**
    	 * Returned if connect has failed.
    	 * If a proxy is involed, this is also returned in case of ERR_CONNECT_FAIL.
    	 */
    	CONNECT_FAILED,

    	/**
    	 * Connection attempt ran into timeout (SocketTimeoutException in connection.connect()).
    	 */
    	CONNECT_TIMEOUT,

    	/**
    	 * Returned if connection attempt was successful.
    	 * i.e. using checkConnect() methods.
    	 */
    	CONNECT_OK,

    	/**
    	 * Read attempt ran into timeout (SocketTimeoutException in connection.getInputStream()).
    	 */
    	READ_TIMEOUT,

    	/**
    	 * Returned if read attempt was successful after successful connection,
    	 * i.e. using checkRead() methods.
    	 */
    	READ_OK,

    	/**
    	 * The given host is unknown (i.e. a UnknownHostException was caught, HTTP 404)
    	 * If a proxy is involed, this is also returned in case of ERR_DNS_FAIL.
    	 */
    	UNKNOWN_HOST,

    	/**
    	 * The host was found, but the path is unknown (FileNotFoundException HTTP 404)
    	 */
    	NOT_FOUND,

    	/**
    	 * Error on Server-Side i.e. any unexpected error condition (HTTP 4xx (except 404) or HTTP 5xx)
    	 * If a proxy is involved, a HTTP 504 Gateway Timeout might instead be covered by CONNECT_FAILED or UNKNOWN_HOST,
    	 * depending on the X-Squid-Error HTTP header field.
    	 */
    	SERVER_ERROR;

    	/**
    	 * Returns whether read or connection attempt was successful.
    	 *
    	 * @return true if read or connection attempt was successful.
    	 */
    	public boolean isOK() {
    		return this == CONNECT_OK || this == READ_OK;
    	}

    	/**
    	 * Returns whether a read or connection timeout happened.
    	 *
    	 * @return true if a read or connection timeout happened.
    	 */
    	public boolean timedOut() {
    		return this == CONNECT_TIMEOUT || this == READ_TIMEOUT;
    	}
    }

    private static final Logger log = Logger.getLogger(UrlUtils.class.getName());

	/**
	 * Default timeout for a check operation.
	 */
	private static final int DEFAULT_CHECK_TIMEOUT = 60 * 1000;

	/**
	 * Default timeout for a retrieve operation.
	 */
	private static final int DEFAULT_RETRIEVE_TIMEOUT = 60 * 1000;

	/*
	 * The first n bytes of data to print to FINE in retrieveRawData()
	 */
    private static final int REPORT_PEEK_COUNT = 200;

    /*
     * Prepare connection by setting connectTimeout and readTimeout to timeout,
     * doOutput to false and
     * doInot to true.
     * Throws IllegalArgumentException on zero (infinite) timeout.
     */
    private static void prepareConnection(HttpURLConnection connection, int timeout) {
    	if (timeout == 0) {
    		throw new IllegalArgumentException("Zero (infinite) timeouts not permitted");
    	}
        connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);
		connection.setDoInput(true); // whether we want to read from the connection
        connection.setDoOutput(false); // whether we want to write to the connection
        connection.setInstanceFollowRedirects(true);
    }

    /*
     * Write POST reqeust header and body
     */
    private static void writePostRequest(URLConnection connection, String postRequestBody, String contentType) throws IOException {
        connection.setDoOutput(true); // whether we want to write to the connection

    	if (contentType != null) {
    		connection.setRequestProperty("Content-Type", contentType);
    	}
    	// Note: Content-Length is set implicitly by URLConnection
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		try {
			out.writeBytes(postRequestBody);
		} finally {
			out.close();
		}
    }

	/**
	 * Download data from an URL.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @return The resulting data, e.g. a HTML string.
	 * @throws IOException
	 */
	public static String retrieveData(String sUrl) throws IOException {
		return retrieveStringInternal(sUrl, /*encoding*/ null, DEFAULT_RETRIEVE_TIMEOUT);
	}

	/**
	 * Download data from an URL.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @param timeout The timeout in milliseconds that is used for both connection timeout and read timeout.
	 * @return The resulting data, e.g. a HTML string.
	 * @throws IOException
	 */
	public static String retrieveData(String sUrl, int timeout) throws IOException {
		return retrieveStringInternal(sUrl, /*encoding*/ null, timeout);
	}

	/**
	 * Download data from an URL, if necessary converting from a character encoding.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @param encoding An encoding, e.g. UTF-8, ISO-8859-15. Can be null.
	 * @return The resulting data, e.g. a HTML string.
	 * @throws IOException
	 */
	public static String retrieveData(String sUrl, String encoding) throws IOException {
		return retrieveStringInternal(sUrl, encoding, DEFAULT_RETRIEVE_TIMEOUT);
	}

	/**
	 * Download data from an URL with a POST request, if necessary converting from a character encoding.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @param encoding An encoding, e.g. UTF-8, ISO-8859-15. Can be null.
	 * @param postRequestBody the body of the POST request, e.g. request parameters; must not be null
	 * @param contentType the content-type of the POST request; may be null
	 * @return The resulting data, e.g. a HTML string.
	 * @throws IOException
	 */
	public static String retrieveDataPost(String sUrl, String encoding, String postRequestBody, String contentType) throws IOException {
		return retrieveStringInternalPost(sUrl, encoding, postRequestBody, contentType, DEFAULT_RETRIEVE_TIMEOUT);
	}

	/**
	 * Download data from an URL, if necessary converting from a character encoding.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @param encoding An encoding, e.g. UTF-8, ISO-8859-15. Can be null.
	 * @param timeout The timeout in milliseconds that is used for both connection timeout and read timeout.
	 *
	 * @return The resulting data, e.g. a HTML string.
	 *
	 * @throws IOException
	 */
	public static String retrieveData(String sUrl, String encoding, int timeout) throws IOException {
		return retrieveStringInternal(sUrl, encoding, timeout);
	}

	/**
	 * Download data from an URL with a POST request, if necessary converting from a character encoding.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @param encoding An encoding, e.g. UTF-8, ISO-8859-15. Can be null.
	 * @param postRequestBody the body of the POST request, e.g. request parameters; must not be null
	 * @param contentType the content-type of the POST request; may be null
	 * @param timeout The timeout in milliseconds that is used for both connection timeout and read timeout.
	 * @return
	 * @throws IOException
	 */
	public static String retrieveDataPost(String sUrl, String encoding, String postRequestBody, String contentType, int timeout) throws IOException {
		return retrieveStringInternalPost(sUrl, encoding, postRequestBody, contentType, timeout);
	}

	/**
	 * Download data from an URL as raw bytes.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @return The resulting data as raw bytes
	 * @throws IOException
	 */
	public static byte[] retrieveRawData(String sUrl) throws IOException {
		return retrieveRawInternal(sUrl, DEFAULT_RETRIEVE_TIMEOUT);
	}

	/**
	 * Download data from an URL as raw bytes.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @param timeout The timeout in milliseconds that is used for both connection timeout and read timeout.
	 * @return The resulting data as raw bytes
	 * @throws IOException
	 */
	public static byte[] retrieveRawData(String sUrl, int timeout) throws IOException {
		return retrieveRawInternal(sUrl, timeout);
	}

	/**
	 * Download data from an URL with a POST request as raw bytes.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @param postRequestBody the body of the POST request, e.g. request parameters; must not be null
	 * @param contentType the content-type of the POST request; may be null
	 * @return The resulting data as raw bytes
	 * @throws IOException
	 */
	public static byte[] retrieveRawDataPost(String sUrl, String postRequestBody, String contentType) throws IOException {
		return retrieveRawInternalPost(sUrl, postRequestBody, contentType, DEFAULT_RETRIEVE_TIMEOUT);
	}

	/**
	 * Download data from an URL with a POST request as raw bytes.
	 *
	 * @param sUrl The full URL used to download the content.
	 * @param postRequestBody the body of the POST request, e.g. request parameters; must not be null
	 * @param contentType the content-type of the POST request; may be null
	 * @param timeout The timeout in milliseconds that is used for both connection timeout and read timeout.
	 * @return The resulting data as raw bytes
	 * @throws IOException
	 */
	public static byte[] retrieveRawDataPost(String sUrl, String postRequestBody, String contentType, int timeout) throws IOException {
		return retrieveRawInternalPost(sUrl, postRequestBody, contentType, timeout);
	}

	private static String retrieveStringInternalPost(String sUrl, String encoding, String postRequestBody, String contentType, int timeout) throws IOException {
		byte[] rawData = retrieveRawInternalPost(sUrl, postRequestBody, contentType, timeout);
		return encoding != null ? new String(rawData, encoding) : new String(rawData);
	}

	private static String retrieveStringInternal(String sUrl, String encoding, int timeout) throws IOException {
		byte[] rawData = retrieveRawInternal(sUrl, timeout);
		return encoding != null ? new String(rawData, encoding) : new String(rawData);
	}

	private static byte[] retrieveRawInternal(String sUrl, int timeout) throws IOException {
		return doRetrieve(sUrl, /*postRequestBody*/ null, /*contentType*/ null, timeout);
	}

	private static byte[] retrieveRawInternalPost(String sUrl, String postRequestBody, String contentType, int timeout) throws IOException {
		if (postRequestBody == null) {
			throw new IllegalArgumentException("POST request body must not be null");
		}
		return doRetrieve(sUrl, postRequestBody, contentType, timeout);
	}

	private static byte[] doRetrieve(String sUrl, String postRequestBody, String contentType, int timeout) throws IOException {
		URL url = new URL(sUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		try {
			prepareConnection(connection, timeout);
			if (postRequestBody != null) {
				writePostRequest(connection, postRequestBody, contentType);
			}
			connection.connect();
			int responseCode = connection.getResponseCode();
			if (!responseCodeValid(responseCode)) {
				String message = TextUtils.merge("Error {0} returned while retrieving response for url {1}, response message: {2}", responseCode, url, connection.getResponseMessage());
				log.warning(message);
				throw new IOException(message);
			}

			// actually read the contents, even if we are not using it to simulate a full download of the data
			ByteArrayOutputStream memStream = new ByteArrayOutputStream(connection.getContentLength() == -1 ? 40000 : connection.getContentLength());
			InputStream in = connection.getInputStream();
			try {
				byte buf[] = new byte[4096];
				int len;
				while ((len = in.read(buf)) > 0) {
					memStream.write(buf, 0, len);
				}
			} finally {
				in.close();
				// omitting futile memStream.close()
			}

			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, TextUtils.merge("Received data, size: {0} ({1}) first bytes: {2}", memStream.size(), connection.getContentLength(),
						replaceInvalidChars(new String(memStream.toByteArray(), 0, Math.min(memStream.size(), REPORT_PEEK_COUNT)))));
			}

			return memStream.toByteArray();
		} finally {
        	if (log.isLoggable(Level.FINE)) log.log(Level.FINE, TextUtils.merge("Retrieved URL: {0}, header fields: {1}", url, connection.getHeaderFields()));
			connection.disconnect();
		}
	}

	/*
	 * helper the decide if response is is considered valid for retrieveData.
	 */
	private static boolean responseCodeValid(int responseCode) {
		return responseCode / 100 == HttpURLConnection.HTTP_OK / 100;
	}

	/*
	 * helper for logging binary content
	 */
	private static String replaceInvalidChars(String substring) {
		StringBuilder builder = new StringBuilder();
		for(char c : substring.toCharArray()) {
			if(c < 32) {
				builder.append('.');
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}

    /**
     * Checks if a service is available. Opens a socket connection to host port to
     * check for the availability.
     *
     * @param host host name or ip address to check
     * @param port tcp port of service
     * @return true if a successful socket connection to the service could be established
     * @author cwat-plang
     */
    public static boolean checkServiceAvailability(String host, int port) {
    	Socket socket = null;
    	boolean reachable = false;
    	try {
    		socket = new Socket(host, port);
    		reachable = true;
    	} catch (UnknownHostException e) {
    		if (log.isLoggable(Level.FINE)) {
    			log.log(Level.FINE, "checkServiceAvailability(): Host is unknown " + e.getMessage(), e);
    		}
		} catch (IOException e) {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "checkServiceAvailability(): Unexpected IOException catched " + e.getMessage(), e);
			}
		} finally {
    		IOUtils.closeQuietly(socket);
    	}
    	return reachable;
    }

    /**
     * Check availability of the specified destination URL by merely attempting a connection.
     * Timeout setting defaults to DEFAULT_CHECK_TIMEOUT.
     *
     * @param destinationUrl the destination URL
     * @return Availability, i.e. CONNECT_OK on success
     * @throws IllegalArgumentException if the destination URL is invalid
     * @author cwat-pgrasboe
     */
    public static Availability checkConnect(String destinationUrl) {
    	return checkConnect(destinationUrl, DEFAULT_CHECK_TIMEOUT);
    }

    /**
     * Check availability of the specified destination URL by merely attempting a connection.
     *
     * @param destinationUrl the destination URL
     * @param timeout the connection timeout
     * @return Availability, i.e. CONNECT_OK on success
     * @throws IllegalArgumentException if the destination URL is invalid
     * @author cwat-pgrasboe
     */
    public static Availability checkConnect(String destinationUrl, int timeout) {
    	return checkAvailability(destinationUrl, timeout, /*readFromConnection*/ false, /*headers*/ null);
    }

    /**
     * Check availability of the specified destination URL by attempting a connection
     * and attempting to read from it.
     * Timeout setting defaults to DEFAULT_CHECK_TIMEOUT.
     *
     * @param destinationUrl the destination URL
     * @return Availability, i.e. READ_OK on success
     * @throws IllegalArgumentException if the destination URL is invalid
     * @author cwat-pgrasboe
     */
    public static Availability checkRead(String destinationUrl) {
    	return checkRead(destinationUrl, DEFAULT_CHECK_TIMEOUT);
    }

    /**
     * Check availability of the specified destination URL by attempting a connection
     * and attempting to read from it.
     *
     * @param destinationUrl the destination URL
     * @param timeout the connection and read timeout
     * @return Availability, i.e. READ_OK on success
     * @throws IllegalArgumentException if the destination URL is invalid
     * @author cwat-pgrasboe
     */
    public static Availability checkRead(String destinationUrl, int timeout) {
    	return checkAvailability(destinationUrl, timeout, /*readFromConnection*/ true, /*headers*/ null);
    }

    /**
     * Check availability of the specified destination URL by attempting a connection
     * and check if required headers are present.
     *
     * @param destinationUrl the destination URL
     * @param requiredHeaders the map of headers to check
     * @return Availability, i.e. READ_OK on success
     * @throws IllegalArgumentException if the destination URL is invalid
     * @author cwat-pgrasboe, cwpl-kkulikow
     */

    public static Availability checkHeaders(String destinationUrl, Map<String,String> headers) {
    	return checkAvailability(destinationUrl, DEFAULT_CHECK_TIMEOUT, /*readFromConnection*/ false, /*headers*/ headers);
    }

    /**
     * Check if the HTTP resource specified by the destination URL is available.
     *
     * @param destinationUrl the destination URL to check for availability
     * @param timeout timeout setting (connectTimeout and readTimeout)
     * @param readFromConnection whether to attempt to actually read from the connection's input stream.
     * @param requiredHeaders a map of headers, which must be contained in server response headers
     * For proxy situations, this is needed.
     * @return OK if the resource is available, or an error constant.
     *
     * @throws IllegalArgumentException if the destination URL is invalid
     * @author cwat-pgrasboe
     */


    private static Availability checkAvailability(String destinationUrl, int timeout, boolean readFromConnection, Map<String, String> requiredHeaders) {
        URL url;
		try {
			url = new URL(destinationUrl);
		} catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid destination URL", e);
		}
		Availability result = Availability.CONNECT_FAILED;
    	HttpURLConnection connection = null;
        try {
	        connection = (HttpURLConnection) url.openConnection();
			prepareConnection(connection, timeout);

            /* if connecting is not possible this will throw a connection refused exception */
    		result  = Availability.CONNECT_TIMEOUT;
            connection.connect();
    		result  = Availability.CONNECT_OK;


    		if(requiredHeaders != null){
    	        String responseValue;
    			for(Map.Entry<String, String> entry : requiredHeaders.entrySet()){
    				responseValue = connection.getHeaderField(entry.getKey());
    				if(!StringUtils.containsIgnoreCase(responseValue, entry.getValue())){
    					result = Availability.CONNECT_FAILED;
    					break;
    				}
    			}
    		}

            if (readFromConnection) {
	    		/* now, open a stream which fires a request. This is nececessary to see if the resource is actually
	             * available, otherwise connect is OK but the Proxy didn't even try to fetch the resource.
	             */
	            result = Availability.READ_TIMEOUT;
	            InputStream in = null;
	            try {
	            	in = connection.getInputStream();
	            } finally {
	            	if (in != null) {
	            		in.close();
	            	}
	            }
	            result = Availability.READ_OK;
            }

        } catch (SocketTimeoutException e) {
        	if (log.isLoggable(Level.FINE)) log.log(Level.FINE, "check", e);
        	// the result is already set to CONNECT_TIMEOUT or READ_TIMEOUT
        } catch (UnknownHostException e) {
        	if (log.isLoggable(Level.FINE)) log.log(Level.FINE, "check", e);
        	result = Availability.UNKNOWN_HOST;
        } catch (FileNotFoundException e) { // i.e. the host was found but the path is invalid
        	if (log.isLoggable(Level.FINE)) log.log(Level.FINE, "check", e);
        	result = Availability.NOT_FOUND;
        } catch (IOException e) {
        	if (log.isLoggable(Level.FINE)) log.log(Level.FINE, "check", e);
        	result = Availability.SERVER_ERROR;
        	String proxyError = null;
        	if (connection != null && (proxyError = connection.getHeaderField("X-Squid-Error")) != null) {
	        	if (proxyError.startsWith("ERR_DNS_FAIL")) {
	        		result = Availability.UNKNOWN_HOST;
	        	} else if (proxyError.startsWith("ERR_CONNECT_FAIL")) {
	        		result = Availability.CONNECT_FAILED;
	        	}
        	}
        } finally {
        	if (log.isLoggable(Level.FINE)) log.log(Level.FINE, TextUtils.merge("Checked URL: {0}, result: {1}, header fields: {2}", url, result, (connection != null ? connection.getHeaderFields() : "<no connection>")));
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

	/**
	 * Resolves the given address to an IP address, returns the given address if it is already representing an IP address
	 *
	 * @param address
	 * @return the given address as an IP address or the given address if it is already representing an IP address
	 * @throws UnknownHostException
	 * @author stefan.moschinski
	 */
	public static String resolveAddress(String address) throws UnknownHostException {
		return InetAddresses.isInetAddress(address) ? address : InetAddress.getByName(address).getHostAddress();
	}

	public static boolean isInternetUrl(String host) {
		if(host.endsWith(".dynatrace.vmta") ||
				host.endsWith(".dynatrace.local") ||
				host.endsWith(".cpwr.corp")) {
			return false;
		}

		String validChar = "[-a-zA-Z0-9+&@#/%?=~_|!:,;]+";
		String point = "\\.";
		String validUrl = validChar + point + validChar + point + validChar;

		Matcher matcher = Pattern.compile(validUrl).matcher(host.trim());
		return matcher.find();
	}

	private static void setupHttpsProtocolForSelfsignedCerts() {
		EasySSLProtocolSocketFactory factory = new EasySSLProtocolSocketFactory();
		Protocol.unregisterProtocol("https");
		Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) factory, 443));
	}

	private static void setupDefaultSSLSocketFactory()  {
		SSLContext context;
		try {
			context = SSLContext.getInstance("TLS");
			SSLSessionContext sessionContext = context.getServerSessionContext();
			sessionContext.setSessionTimeout(0);
			context.init(null, new TrustManager[] { new GeneralTrustManager() }, new SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.SEVERE, "Problem while getting instance of SSLContext", e);
		} catch (KeyManagementException e) {
			log.log(Level.SEVERE, "Problem while initializing the SSLContext", e);
		}
	}

	private static void setupDefaultHostnameVerifier() {
		HostnameVerifier hv = new HostnameVerifier() {
			@Override
			public boolean verify(String urlHostName, SSLSession session) {
				return true;
			}
		};

		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	public static void trustAllHttpsCertificates() {
		setupHttpsProtocolForSelfsignedCerts();
		setupDefaultSSLSocketFactory();
		setupDefaultHostnameVerifier();
	}

	public static HttpClientBuilder trustAllHttpsCertificates(HttpClientBuilder clientBuilder) {
		SSLContext context;
		try {
			context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { new GeneralTrustManager() }, new SecureRandom());

			HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
			SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory (context, allowAllHosts);

			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
		            .register("http", PlainConnectionSocketFactory.getSocketFactory())
		            .register("https", connectionSocketFactory)
		            .build();
			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			connectionManager.setDefaultMaxPerRoute(100);
			connectionManager.setMaxTotal(200);
			clientBuilder.setConnectionManager(connectionManager);
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.SEVERE, "Problem while getting instance of SSLContext", e);
		} catch (KeyManagementException e) {
			log.log(Level.SEVERE, "Problem while initializing the SSLContext", e);
		}

		return clientBuilder;
	}
}
