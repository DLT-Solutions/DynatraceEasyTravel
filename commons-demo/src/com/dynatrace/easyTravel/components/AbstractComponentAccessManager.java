package com.dynatrace.easytravel.components;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.json.JSONArray;
import com.dynatrace.easytravel.json.JSONException;

public abstract class AbstractComponentAccessManager implements ComponentManagerProxy  {
	private String uri;
	
	public AbstractComponentAccessManager(String uri){
		this.uri = uri;
	}
	
	public String getURI(){
		return uri;
	}
	
	protected static String readString(URL url) {
		try {
			return readString(url.openStream());
		} catch (IOException e) {
			throw new RuntimeException("While reading from URL: " + url, e);
		}
	}

	protected static String readString(InputStream in) throws IOException {
		if (in != null) {
			try {
				return IOUtils.toString(in);
			} finally {
				in.close();
			}
		}
		return null;
	}

	protected URL createURL(String method) {
		return createURL(method, null);
	}

	protected URL createURL(String method, String[] pathParams) {
		StringBuilder sb = new StringBuilder(uri).append(BaseConstants.FSLASH).append(method);
		if (pathParams != null) {
			for (String param : pathParams) {
				sb.append(BaseConstants.FSLASH).append(param);
			}
		}
		try {
			return new URL(sb.toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
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
}
