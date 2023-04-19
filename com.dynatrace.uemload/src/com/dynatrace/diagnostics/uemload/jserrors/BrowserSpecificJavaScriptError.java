package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.easytravel.util.DtVersionDetector;

public class BrowserSpecificJavaScriptError {

	private String message;
	private String file;
	private int line = -1;
	private int column = -1;
	private String stackTrace;
	private String userAction;
	private String code;

	public BrowserSpecificJavaScriptError(String message) {
		this(message, "", -1, -1, "", null);
	}

	public BrowserSpecificJavaScriptError(String message, String file, int line, int column) {
		this(message, file, line, column, null, null, null);
	}

	public BrowserSpecificJavaScriptError(String message, String file, int line, int column, String stackTrace) {
		this(message, file, line, column, stackTrace, null);
	}

	public BrowserSpecificJavaScriptError(String message, String file, int line, int column, String stackTrace, String userAction) {
		this(message, file, line, column, stackTrace, userAction, null);
	}

	public BrowserSpecificJavaScriptError(String message, String file, int line, int column, String stackTrace, String userAction, String code) {
		this.message = message;
		this.file = file;
		this.line = line;
		this.column = column;
		this.stackTrace = stackTrace;
		this.userAction = userAction;
		this.code = code;
	}

	public String getMessage() {
		if (DtVersionDetector.isAPM()) {
			return message;
		}
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(file);
		messageBuilder.append("[");
		messageBuilder.append(line);
		messageBuilder.append("]");
		if (code != null) {
			messageBuilder.append("Code: ");
			messageBuilder.append(code);
			messageBuilder.append(" ");
		}
		messageBuilder.append(message);

		if (userAction != null) {
			messageBuilder.append(", user action: ");
			messageBuilder.append(userAction);
		}

		return messageBuilder.toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getUserAction() {
		return userAction;
	}

	public void setUserAction(String userAction) {
		this.userAction = userAction;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLocationString() {
		StringBuilder location = new StringBuilder();
		location.append(file);
		if (line >= 0) {
			location.append("^p");
			location.append(line);
		}
		if (column >= 0) {
			location.append("^p");
			location.append(column);
		}
		return location.toString();
	}
}



