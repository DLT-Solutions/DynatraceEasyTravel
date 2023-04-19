package com.dynatrace.diagnostics.uemload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.google.common.base.Joiner;

/**
*
* @author cwat-slexow
*
*/
public class JavaScriptErrorAction {

	private static final String SEPERATOR = "|";
	private int depth;
	private String errorMessage;
	private String file;
	private String stackTrace;
	private String userAction;
	private String code;
	private int line = -1;
	private int column = -1;
	private long time;

	private boolean newFormat = false;

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public long getStartTime() {
		return time;
	}
	public void setStartTime(long startTime) {
		this.time = startTime;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
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

	public boolean isNewFormat() {
		return newFormat;
	}

	public void setUserAction(String userAction) {
		this.userAction = userAction;
	}

	public String getUserAction() {
		return userAction;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setNewFormat(boolean newFormat) {
		this.newFormat = newFormat;
	}

	public String toString(int actionId) {
		if (isNewFormat()) {
			return toStringNew(actionId);
		}
		return toStringOld(actionId);
	}

	public String toStringOld(int actionId) {
		StringBuilder builder = new StringBuilder();

		long userActionStartTime = time;
		long userActionEndTime = time + 10;

		builder.append(depth);
		builder.append(SEPERATOR);
		builder.append(actionId);
		builder.append(SEPERATOR);
		builder.append(JavaScriptAgent.actionEscape(errorMessage));
		builder.append(SEPERATOR);
		builder.append("_error_");
		builder.append(SEPERATOR);
		builder.append("-");
		builder.append(SEPERATOR);
		builder.append(userActionStartTime);
		builder.append(SEPERATOR);
		builder.append(userActionEndTime);
		builder.append(SEPERATOR);
		builder.append("-1");
		return builder.toString();
	}

	public String toStringNew(int actionId) {
		StringBuilder builder = new StringBuilder();

		long userActionStartTime = time;
		long userActionEndTime = time + 10;

		builder.append(depth);
		builder.append(SEPERATOR);
		builder.append(actionId);
		builder.append(SEPERATOR);
		builder.append(JavaScriptAgent.actionEscape(errorMessage));
		builder.append(SEPERATOR);
		builder.append("_error_");
		builder.append(SEPERATOR);
		builder.append("-");
		builder.append(SEPERATOR);
		builder.append(userActionStartTime);
		builder.append(SEPERATOR);
		builder.append(userActionEndTime);
		builder.append(SEPERATOR);
		builder.append("-1");

		if (file != null && !file.isEmpty()) {
			builder.append(",");
			builder.append(depth + 1);
			builder.append(SEPERATOR);
			builder.append(++actionId);
			builder.append(SEPERATOR);
			builder.append(JavaScriptAgent.actionEscape(buildLocation()));
			builder.append(SEPERATOR);
			builder.append("_location_");
			builder.append(SEPERATOR);
			builder.append("-");
			builder.append(SEPERATOR);
			builder.append(userActionStartTime);
			builder.append(SEPERATOR);
			builder.append(userActionEndTime);
			builder.append(SEPERATOR);
			builder.append("-1");
		}

		if (stackTrace != null && !stackTrace.isEmpty()) {
			builder.append(",");
			builder.append(depth + 1);
			builder.append(SEPERATOR);
			builder.append(++actionId);
			builder.append(SEPERATOR);
			builder.append(JavaScriptAgent.actionEscape(stackTrace));
			builder.append(SEPERATOR);
			builder.append("_stack_");
			builder.append(SEPERATOR);
			builder.append("-");
			builder.append(SEPERATOR);
			builder.append(userActionStartTime);
			builder.append(SEPERATOR);
			builder.append(userActionEndTime);
			builder.append(SEPERATOR);
			builder.append("-1");
		}

		if (code != null && !code.isEmpty()) {
			builder.append(",");
			builder.append(depth + 1);
			builder.append(SEPERATOR);
			builder.append(++actionId);
			builder.append(SEPERATOR);
			builder.append(JavaScriptAgent.actionEscape(code));
			builder.append(SEPERATOR);
			builder.append("_code_");
			builder.append(SEPERATOR);
			builder.append("-");
			builder.append(SEPERATOR);
			builder.append(userActionStartTime);
			builder.append(SEPERATOR);
			builder.append(userActionEndTime);
			builder.append(SEPERATOR);
			builder.append("-1");
		}

		if (userAction != null && !userAction.isEmpty()) {
			builder.append(",");
			builder.append(depth + 1);
			builder.append(SEPERATOR);
			builder.append(++actionId);
			builder.append(SEPERATOR);
			builder.append(JavaScriptAgent.actionEscape(userAction));
			builder.append(SEPERATOR);
			builder.append("_useraction_");
			builder.append(SEPERATOR);
			builder.append("-");
			builder.append(SEPERATOR);
			builder.append(userActionStartTime);
			builder.append(SEPERATOR);
			builder.append(userActionEndTime);
			builder.append(SEPERATOR);
			builder.append("-1");
		}

		return builder.toString();
	}

	private String buildLocation() {
		StringBuilder location = new StringBuilder();
		location.append(file);
		if (line >= 0) {
			location.append("|");
			location.append(line);
		}
		if (column >= 0) {
			location.append("|");
			location.append(column);
		}
		return location.toString();
	}

	public class UserActionConfig {
		String actionDisplayName;
		String actionType;
		String actionInfo;
	}

	public Collection<? extends String> getActionsWithActionIdPlaceholders() {
		List<String> actions = new ArrayList<String>();

		if (DtVersionDetector.isAPM()) {
			String errorAction = Joiner.on(BaseConstants.PIPE).join(2, "<actionId>", JavaScriptAgent.actionEscape(errorMessage), "_error_", BaseConstants.MINUS, "<starttime>", "<endtime>", -1);
			actions.add(errorAction);

			if (getFile() != null && !getFile().isEmpty()) {
				String locationAction = Joiner.on(BaseConstants.PIPE).join(3, "<actionId>", JavaScriptAgent.actionEscape(buildLocation()), "_location_", BaseConstants.MINUS, "<starttime>", "<endtime>", -1);
				actions.add(locationAction);
			}
			if (getStackTrace() != null && !getStackTrace().isEmpty()) {
				String stackTraceAction = Joiner.on(BaseConstants.PIPE).join(3, "<actionId>", JavaScriptAgent.actionEscape(getStackTrace()), "_stack_", BaseConstants.MINUS, "<starttime>", "<endtime>", -1);
				actions.add(stackTraceAction);
			}
			if (getCode() != null && !getCode().isEmpty()) {
				String codeAction = Joiner.on(BaseConstants.PIPE).join(3, "<actionId>", getCode(), "_code_", BaseConstants.MINUS, "<starttime>", "<endtime>", -1);
				actions.add(codeAction);
			}
			if (getUserAction() != null && !getUserAction().isEmpty()) {
				String userActionAction = Joiner.on(BaseConstants.PIPE).join(3, "<actionId>", JavaScriptAgent.actionEscape(userAction), "_useraction_", BaseConstants.MINUS, "<starttime>", "<endtime>", -1);
				actions.add(userActionAction);
			}
		} else {
			String errorAction = Joiner.on(BaseConstants.PIPE).join(2, "<actionId>", JavaScriptAgent.actionEscape(errorMessage), "_error_", BaseConstants.MINUS, "<starttime>", "<endtime>", -1);
			actions.add(errorAction);
		}
		return actions;
	}

}
