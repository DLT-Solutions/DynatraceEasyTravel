package com.dynatrace.diagnostics.uemload.mobile;

public enum Error{
	
	ERROR_1("aj",
			
			"java.lang.NumberFormatException: Invalid int: \"null\"", 
			
			"java.lang.NumberFormatException", 
			
			"java.lang.NumberFormatException: Invalid int: \"null\"\n\tat java.lang.Integer.invalidInt(Integer.java:138)\n\t"
			+ "at java.lang.Integer.parse(Integer.java:410)\n\tat java.lang.Integer.parseInt(Integer.java:367)\n\t"
			+ "at java.lang.Integer.parseInt(Integer.java:334)\n\tat com.dynatrace.uem.mobile.android.storeops.component.common.j.aj.MM(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.j.j.en(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.j.j.B(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.storage.view.activity.MainActivity$a.a(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.j.y.a(Unknown Source)\n"
	),
	
	ERROR_2("MimeException in Address.parse()",
			
			"org.apache.james.mime4j.field.address.ParseException: Encountered \">\" at line 1, column 2.\n"
			+ "Was expecting one of:\n \"@\" ...\n <DOTATOM> ...\n \"\\\"\" ...\n",
			
			"org.apache.james.mime4j.field.address.ParseException",
			
			"org.apache.james.mime4j.field.address.ParseException: Encountered \">\" at line 1, column 2.\n"
			+ "Was expecting one of:\n \"@\" ...\n <DOTATOM> ...\n \"\\\"\" ...\n \n\t"
			+ "at org.apache.james.mime4j.field.address.AddressListParser.generateParseException(AddressListParser.java:966)\n\t"
			+ "at org.apache.james.mime4j.field.address.AddressListParser.jj_consume_token(AddressListParser.java:848)\n\t"
			+ "at org.apache.james.mime4j.field.address.AddressListParser.local_part(AddressListParser.java:543)\n\t"
			+ "at org.apache.james.mime4j.field.address.AddressListParser.addr_spec(AddressListParser.java:502)\n\t"
			+ "at org.apache.james.mime4j.field.address.AddressListParser.angle_addr(AddressListParser.java:374)\n\t"
			+ "at org.apache.james.mime4j.field.address.AddressListParser.address(AddressListParser.java:172)\n\t"
			+ "at org.apache.james.mime4j.field.address.AddressListParser.address_list(AddressListParser.java:110)\n\t"
			+ "at org.apache.james.mime4j.field.address.AddressListParser.parseAddressList0(AddressListParser.java:85)\n\t"
			+ "at org.apache.james.mime4j.field.address.AddressListParser.parseAddressList(AddressListParser.java:38)\n"
	),
	
	ERROR_3("DHCPManager", 
			
			"java.lang.NumberFormatException: Invalid int: \"testuser\"",
			
			"java.lang.NumberFormatException",
			
			"java.lang.NumberFormatException: Invalid int: \"testuser\"\n\tat java.lang.Integer.invalidInt(Integer.java:138)\n\t"
			+ "at java.lang.Integer.parse(Integer.java:410)\n\tat java.lang.Integer.parseInt(Integer.java:367)\n\t"
			+ "at java.lang.Integer.parseInt(Integer.java:334)\n\t"
			+ "at com.dynatrace.uem.mobile.android.auth.dhcp.DHCPManager.getCurrentStore(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.storage.view.a.IZ(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.storage.view.a.IX(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.storage.MyStoreApp.onCreate(Unknown Source)\n\t"
			+ "at android.app.Instrumentation.callApplicationOnCreate(Instrumentation.java:1013)\n"
	),
	
	ERROR_4("SleepService Interrupted while awaiting latch\njava.lang.InterruptedException\n\t",
			
			"java.lang.InterruptedException",
			 
			"java.lang.InterruptedException",
			
			"java.lang.InterruptedException\n\t"
			+ "at java.util.concurrent.locks.AbstractQueuedSynchronizer.doAcquireSharedNanos(AbstractQueuedSynchronizer.java:1016)\n\t"
			+ "at java.util.concurrent.locks.AbstractQueuedSynchronizer.tryAcquireSharedNanos(AbstractQueuedSynchronizer.java:1304)\n\t"
			+ "at java.util.concurrent.CountDownLatch.await(CountDownLatch.java:248)\n\tat com.fsck.k9.service.SleepService.sleep(SleepService.java:51)\n\t"
			+ "at com.ruxit.mobile.controller.MessageReceiver.sleep(MessageReceiver.java:72)\n\t"
			+ "at com.ruxit.mobile.mail.store.imap.ImapFolderPusher$PushRunnable.run(ImapFolderPusher.java:211)\n\t"
			+ "at java.lang.Thread.run(Thread.java:818)\n"
	),
	
	ERROR_5("AccessibilityEnableUtils",
			
			"android.content.ActivityNotFoundException: Unable to find explicit activity class "
			+ "{com.dynatrace.uem.mobile/com.dynatrace.uem.mobile.MainActivity}; have you declared this activity in your AndroidManifest.xml?",
			
			"android.content.ActivityNotFoundException",
			
			"android.content.ActivityNotFoundException: Unable to find explicit activity class {com.dynatrace.uem.mobile/com.dynatrace.uem.mobile.MainActivity}; "
			+ "have you declared this activity in your AndroidManifest.xml?\n\t"
			+ "at android.app.Instrumentation.checkStartActivityResult(Instrumentation.java:1777)\n\t"
			+ "at android.app.Instrumentation.execStartActivity(Instrumentation.java:1501)\n\t"
			+ "at android.app.ContextImpl.startActivity(ContextImpl.java:1262)\n\t"
			+ "at android.app.ContextImpl.startActivity(ContextImpl.java:1244)\n\t"
			+ "at android.content.ContextWrapper.startActivity(ContextWrapper.java:323)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.j.a.d(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.j.a.e(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.j.a$a.handleMessage(Unknown Source)\n\t"
			+ "at android.os.Handler.dispatchMessage(Handler.java:102)\n"
	),
	
	ERROR_6("Push error for INBOX\njava.net.SocketException: recvfrom failed: ETIMEDOUT (Connection timed out)\n\t",
			
			"java.net.SocketException: recvfrom failed: ETIMEDOUT (Connection timed out)",
			
			"java.net.SocketException",
			
			"java.net.SocketException: recvfrom failed: ETIMEDOUT (Connection timed out)\n\t"
			+ "at libcore.io.IoBridge.maybeThrowAfterRecvfrom(IoBridge.java:592)\n\t"
			+ "at libcore.io.IoBridge.recvfrom(IoBridge.java:556)\n\t"
			+ "at java.net.PlainSocketImpl.read(PlainSocketImpl.java:485)\n\t"
			+ "at java.net.PlainSocketImpl.access$000(PlainSocketImpl.java:37)\n\t"
			+ "at java.net.PlainSocketImpl$PlainSocketInputStream.read(PlainSocketImpl.java:237)\n\t"
			+ "at java.io.InputStream.read(InputStream.java:162)\n\t"
			+ "at java.io.BufferedInputStream.fillbuf(BufferedInputStream.java:149)\n\t"
			+ "at java.io.BufferedInputStream.read(BufferedInputStream.java:234)\n\t"
			+ "at com.ruxit.mobile.mail.filter.CustomFilterInputStream.peek(CustomFilterInputStream.java:36)\n"				
	),
	
	ERROR_7("com.dynatrace.uem.mobile.android.store.component.common.e.c",
		
			"com.google.android.gms.auth.GoogleAuthException: UNREGISTERED_ON_API_CONSOLE",
			
			"com.google.android.gms.auth.GoogleAuthException",
			
			"com.google.android.gms.auth.GoogleAuthException: UNREGISTERED_ON_API_CONSOLE\n\t"
			+ "at com.google.android.gms.auth.f.h(Unknown Source)\n\t"
			+ "at com.google.android.gms.auth.e.a(Unknown Source)\n\t"
			+ "at com.google.android.gms.auth.e.b(Unknown Source)\n\t"
			+ "at com.google.android.gms.auth.e.a(Unknown Source)\n\t"
			+ "at com.google.android.gms.auth.e.a(Unknown Source)\n\t"
			+ "at com.google.android.gms.auth.a.a(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.e.c.a(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.e.c.a(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.e.c.fetchFromService(Unknown Source)\n"
	),
	
	ERROR_8("-", 
			
			"android.content.pm.PackageManager$NameNotFoundException: com.dynatrace.uem.mobile.android.plugin.x",
			
			"android.content.pm.PackageManager$NameNotFoundException",
			
			"android.content.pm.PackageManager$NameNotFoundException: com.dynatrace.uem.mobile.android.plugin.x\n\t"
			+ "at android.app.ApplicationPackageManager.getPackageInfo(ApplicationPackageManager.java:138)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.d.m.J(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.store.component.common.d.a.be(Unknown Source)\n\t"
			+ "at com.dynatrace.uem.mobile.android.storage.view.activity.MainActivity.onCreate(Unknown Source)\n\t"
			+ "at android.app.Activity.performCreate(Activity.java:6308)\n\t"
			+ "at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1108)\n\t"
			+ "at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2369)\n\t"
			+ "at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2476)\n\t"
			+ "at android.app.ActivityThread.access$900(ActivityThread.java:150)\n"
	),
	
	ERROR_9("Got exception while closing for exception for Store inbox:INBOX/Thread-548\n",
			
			"java.lang.NullPointerException: Attempt to invoke virtual method 'void com.ruxit.mobile.mail.store.imap.ImapConnection.close()' on a null object reference",
			
			"java.lang.NullPointerException",
			
			"java.lang.NullPointerException: Attempt to invoke virtual method 'void com.ruxit.mobile.mail.store.imap.ImapConnection.close()' on a null object reference\n\t"
			+ "at com.ruxit.mobile.mail.store.imap.ImapFolderPusher$PushRunnable.reacquireWakeLockAndCleanUp(ImapFolderPusher.java:252)\n\t"
			+ "at com.ruxit.mobile.mail.store.imap.ImapFolderPusher$PushRunnable.run(ImapFolderPusher.java:203)\n\t"
			+ "at java.lang.Thread.run(Thread.java:818)\n"
	); 
	
	private final String name;
	private final String value;
	private final String reason;
	private final String stacktrace;
	
	Error(String name, String reason, String value, String stacktrace) {
		this.name = name;
		this.reason = reason;
		this.value = value;
		this.stacktrace = stacktrace;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getReason() {
		return reason;
	}
	
	public String getStacktrace() {
		return stacktrace;
	}		
}