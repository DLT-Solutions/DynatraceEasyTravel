package com.dynatrace.easytravel.components;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.json.JSONException;
import com.dynatrace.easytravel.json.JSONObject;
import com.dynatrace.easytravel.logging.LoggerFactory;

public class RemoteComponentManager extends AbstractComponentAccessManager {
	private static final Logger log = LoggerFactory.make();
	
	public RemoteComponentManager(String host, int port) {
		super("http://"+host+":"+port+BaseConstants.FSLASH + "PluginService");
	}



	@Override
	public void setComponent(String ip, String[] params) {
		try {

			String data = URLEncoder.encode("params", BaseConstants.UTF8) + "="
					+ URLEncoder.encode(JSONObject.valueToString(params), BaseConstants.UTF8);
			URI uri = new URI(this.getURI()+BaseConstants.FSLASH + "setComponent" + BaseConstants.FSLASH + ip);
			HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
			try {
				conn.setRequestMethod(BaseConstants.Http.Method.POST);
				conn.setRequestProperty(BaseConstants.Http.Headers.CONTENT_TYPE, "application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
				conn.setRequestProperty("Content-Language", "en-US");
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
				try {
					wr.writeBytes(data);
					wr.flush();
				} finally {
					wr.close();
				}

				readString(conn.getInputStream());
			} finally {
				conn.disconnect();
			}
		} catch (IOException | URISyntaxException | JSONException e) {
			log.error("There were problems setting component", e);
		}
	}


	@Override
	public void removeComponent(String ip) {
		try {
			readString(createURL("removeComponent", new String[] { URLEncoder.encode(ip, BaseConstants.UTF8) }));
		} catch (UnsupportedEncodingException e) {
			log.warn("Unable to remove component", e);
		}	
	}

	@Override
	public String[] getComponentsIPList(String type) {
		URL url = createURL("getComponentsIPList", new String[] { type });

		return jsonToStringArray(readString(url));
	}

}
