package com.dynatrace.diagnostics.uemload.http.base;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.dynatrace.easytravel.constants.BaseConstants;

/**
 * Helps to create a HTML form.
 * 
 * @author stefan.moschinski
 */
public class UemLoadFormBuilder {
	
	private List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	
	public UemLoadFormBuilder add(String key, Object value) {
		formparams.add(new BasicNameValuePair(key, String.valueOf(value)));
		return this;
	}
	
	/**
	 * 
	 * @return the form parameters encode in UTF-8
	 * @throws UnsupportedEncodingException
	 * @author stefan.moschinski
	 */
	public HttpEntity getFormParms() throws UnsupportedEncodingException {
		return new UrlEncodedFormEntity(formparams, BaseConstants.UTF8);
	}
	
	
}