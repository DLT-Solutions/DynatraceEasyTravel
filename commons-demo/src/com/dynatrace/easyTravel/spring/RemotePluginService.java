package com.dynatrace.easytravel.spring;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.json.JSONArray;
import com.dynatrace.easytravel.json.JSONException;
import com.dynatrace.easytravel.json.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Used by the {@link PluginStateManager} in order to call the PluginService remotely
 *
 * @author cwat-rpilz
 *
 */
public class RemotePluginService implements PluginStateProxy {

	private static final Logger log = Logger.getLogger(RemotePluginService.class.getName());

	private static final String CONTEXT = BaseConstants.FSLASH + "PluginService";

	private final ClientConfig clientConfig = new DefaultClientConfig();
    private final Client client = Client.create(clientConfig);

	private final String host;
	private final int port;

	/**
	 * c'tor
	 *
	 * @param host the host to contact remotely
	 * @param port the port the configured host is listening on
	 */
	public RemotePluginService(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private URL createURL(String method) {
		return createURL(method, null);
	}

	private URL createURL(String method, String[] pathParams) {
		StringBuilder sb = new StringBuilder(CONTEXT).append(BaseConstants.FSLASH).append(method);
		if (pathParams != null) {
			for (String param : pathParams) {
				sb.append(BaseConstants.FSLASH).append(param);
			}
		}
		try {
			return new URL(BaseConstants.Http.PROTOCOL, host, port, sb.toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private static String readString(URL url) {
		try {
			return readString(url.openStream());
		} catch (IOException e) {
			throw new RuntimeException("While reading from URL: " + url, e);
		}
	}

	private static String readString(InputStream in) throws IOException {
		if (in != null) {
			try {
				return IOUtils.toString(in);
			} finally {
				in.close();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dynatrace.easytravel.spring.PluginStateProxy#getAllPluginNames()
	 */
	@Override
	public String[] getAllPluginNames() {
		return jsonToStringArray(readString(createURL("getAllPluginNames")));
	}

	public static String[] jsonToStringArray(String json) {
		String[] result = null;
		try {
			JSONArray jsonArray = new JSONArray(json);
			int len = jsonArray.length();
			result = new String[len];
			for (int i = 0; i < len; i++) {
				result[i] = jsonArray.getString(i);
			}
			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.dynatrace.easytravel.spring.PluginStateProxy#getEnabledPluginNames()
	 */
	@Override
	public String[] getEnabledPluginNames() {
		return jsonToStringArray(readString(createURL("getEnabledPluginNames")));
	}

	/*
	 * (non-Javadoc)
	 * @see com.dynatrace.easytravel.spring.PluginStateProxy#getEnabledPluginNamesForHost()
	 */
	@Override
	public String[] getEnabledPluginNamesForHost(String host) {
		return jsonToStringArray(readString(createURL("getEnabledPluginNamesForHost", new String[] { host })));
	}

	/*
	 * (non-Javadoc)
	 * @see com.dynatrace.easytravel.spring.PluginStateProxy#getAllPlugins()
	 */
	@Override
	public String[] getAllPlugins() {
		return jsonToStringArray(readString(createURL("getAllPlugins")));
	}

	/*
	 * (non-Javadoc)
	 * @see com.dynatrace.easytravel.spring.PluginStateProxy#getEnabledPlugins()
	 */
	@Override
	public String[] getEnabledPlugins() {
		return jsonToStringArray(readString(createURL("getEnabledPlugins")));
	}

	/*
	 * (non-Javadoc)
	 * @see com.dynatrace.easytravel.spring.PluginStateProxy#getEnabledPlugins()
	 */
	@Override
	public String[] getEnabledPluginsForHost(String host) {
		return jsonToStringArray(readString(createURL("getEnabledPluginsForHost", new String[] { host })));
	}

	/*
	 * (non-Javadoc)
	 * @see com.dynatrace.easytravel.spring.PluginStateProxy#registerPlugins(java.lang.String[])
	 */
	@Override
	public void registerPlugins(String[] pluginData) {
		log.info("Registering " + pluginData.length + " plugins on remote plugin service at " + host + ":" + port);
		for (String pluginDataEntry : pluginData) {
			String[] singlePluginData = new String[] { pluginDataEntry };
			try {

				String data = URLEncoder.encode("pluginData", BaseConstants.UTF8) + "=" + URLEncoder.encode(JSONObject.valueToString(singlePluginData), BaseConstants.UTF8);

				URL url = new URL("http://" + host + ":" + port + CONTEXT + BaseConstants.FSLASH + "registerPlugins");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				try {
					conn.setRequestMethod(BaseConstants.Http.Method.POST);
					conn.setRequestProperty(BaseConstants.Http.Headers.CONTENT_TYPE, "application/x-www-form-urlencoded");
					conn.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
					conn.setRequestProperty("Content-Language", "en-US");
					conn.setUseCaches (false);
					conn.setDoInput(true);
					conn.setDoOutput(true);

					DataOutputStream wr = new DataOutputStream (conn.getOutputStream ());
					try {
						wr.writeBytes(data);
						wr.flush ();
					} finally {
						wr.close ();
					}

					String response = readString(conn.getInputStream());
					log.fine("Having response: " + response);
				} finally {
					conn.disconnect();
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "Unable to register plugins remotely", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.dynatrace.easytravel.spring.PluginStateProxy#setPluginEnabled(java.lang.String, boolean)
	 */
	@Override
	public void setPluginEnabled(String name, boolean enabled) {
		try {
			readString(
					createURL(
							"setPluginEnabled",
							new String[] {
									URLEncoder.encode(name, BaseConstants.UTF8),
									Boolean.valueOf(enabled).toString()
							}
					)
			);
		} catch (UnsupportedEncodingException e) {
			log.log(Level.WARNING, "Unable to enable plugin", e);
		}
	}

    @Override
    public void setPluginHosts(String name, String[] hosts) {
		try {
			String data = URLEncoder.encode("hosts", BaseConstants.UTF8) + "=" + URLEncoder.encode(JSONObject.valueToString(hosts), BaseConstants.UTF8);

			URI uri = new URI("http", null, host, port, CONTEXT + BaseConstants.FSLASH + "setPluginHosts" + BaseConstants.FSLASH + name, null, null);
			HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
			try {
				conn.setRequestMethod(BaseConstants.Http.Method.POST);
				conn.setRequestProperty(BaseConstants.Http.Headers.CONTENT_TYPE, "application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
				conn.setRequestProperty("Content-Language", "en-US");
				conn.setUseCaches (false);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				DataOutputStream wr = new DataOutputStream (conn.getOutputStream ());
				try {
					wr.writeBytes(data);
					wr.flush ();
				} finally {
					wr.close ();
				}

				readString(conn.getInputStream());
			} finally {
				conn.disconnect();
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Unable to register plugins remotely at " + host + ":" + port, e);
		}
    }

	@Override
	public void setPluginTemplateConfiguration(String configuration) {
		URL uri = createURL("setPluginTemplateConfiguration");

		try {
			WebResource webResource = client.resource(uri.toString());
			ClientResponse clientResponse = webResource
					.type("application/json")
					.post(ClientResponse.class, configuration);

			if (clientResponse.getStatus() != 200) {
				log.severe("sendConfig failed. PostBody: " + configuration + ". Reason: " + clientResponse.getEntity(String.class));
			}
			else {
				log.info("sendConfig successful.");
			}
		}
		catch(Exception e) {
			log.log(Level.SEVERE, "setPluginTemplateConfiguration failed in RemotePluginService", e);
		}
	}
}
