package com.dynatrace.easytravel.frontend.beans;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;


class ValidationUtils {

	private static final String FORM_PREFIX = "iceform:";

	static void addError(String field, String message) {
		FacesContext.getCurrentInstance().addMessage(FORM_PREFIX + field, new FacesMessage(message));
	}

	static boolean hasErrors() {
		return FacesContext.getCurrentInstance().getMessages().hasNext();
	}
}
