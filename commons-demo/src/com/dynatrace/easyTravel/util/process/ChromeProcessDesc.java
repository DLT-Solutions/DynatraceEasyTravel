package com.dynatrace.easytravel.util.process;

import java.util.Optional;

import com.google.common.base.Strings;

public class ChromeProcessDesc {
	private final String pid;
	private final Optional<String> extensionDir;
	private final Optional<String> parentPid;
		
	public ChromeProcessDesc(String pid, String parentPid, String extensionDir) {
		this.pid = pid;
		this.extensionDir = Optional.ofNullable(Strings.emptyToNull(extensionDir));
		this.parentPid = Optional.ofNullable(Strings.emptyToNull(parentPid));
	}

	public String getPid() {
		return pid;
	}

	public Optional<String> getExtensionDir() {
		return extensionDir;
	}	
	
	public Optional<String> getParentPid() {
		return parentPid;
	}

	@Override
	public String toString() {
		return "ChromeProcessDesc [pid=" + pid + ", extensionDir=" + extensionDir.orElse("NA") + ", parentPid=" + parentPid.orElse("NA") + "]";		
	}
		
}
