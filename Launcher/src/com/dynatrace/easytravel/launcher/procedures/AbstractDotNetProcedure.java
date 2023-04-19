package com.dynatrace.easytravel.launcher.procedures;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractNativeProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.BlockingInputStream;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.LogFileStream;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

/**
 * @author anita.engleder
 */
public abstract class AbstractDotNetProcedure extends AbstractNativeProcedure {

	private static final Logger LOGGER = LoggerFactory.make();
	public static int IIS_REQUEST_TIMEOUT_MS = (int) TimeUnit.MINUTES.toMillis(5); // we use a high Timeout for IIS request,
// hence starting w3wp worker processes can take very long.

	protected BlockingInputStream blockingInputStream;
	

	private static final List<String> PROCESS_WHITELIST = Lists.newArrayList("w3wp.exe", "svchost.exe",
			"can not obtain ownership information");

	protected AbstractDotNetProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);

		// LOGGER.info("COR_ENABLE_PROFILING: " + System.getenv("COR_ENABLE_PROFILING"));
		LOGGER.info("COR_PROFILER: " + System.getenv("COR_PROFILER"));
	}

	/**
	 * Used for testing only!
	 *
	 * @param timeout
	 * @author cwat-dstadler
	 */
	protected static void setISSRequestTimeout(int timeout) {
		IIS_REQUEST_TIMEOUT_MS = timeout;
	}

	@Override
	public boolean isOperatingCheckSupported() { // NOPMD
		return true;
	}

	@Override
	public boolean isOperating() {
		if (!isRunning()) {
			return false;
		}
		return UrlUtils.checkConnect(getURI()).isOK();

	}

	@Override
	public boolean isRunning() {
		if (isRunningOnLocalIIS()) {
			return UrlUtils.checkConnect(getURI()).isOK();
		}
		return process.isRunning();
	}

	public abstract boolean isRunningOnLocalIIS();

	@Override
	public Feedback stop() {
		LOGGER.debug("Stopping procedures. Stopping DotNetProcedure");
		if (this.isRunningOnLocalIIS()) {
			LOGGER.debug("Stopping procedures. DotNetProcedure runs on local IIS - does not need stopping");
			return Feedback.Success;
		}

		blockingInputStream.unblock();

		if (!waitUntilNotRunning()) {
			LOGGER.debug("Stopping procedures. DotNetProcedure stopped");
			return Feedback.Success;
		}
		return super.stop();
	}

	@Override
	public StopMode getStopMode() {
		if (this.isRunningOnLocalIIS())
		{
			return StopMode.NONE;
		}
		return StopMode.PARALLEL;
	}

	@Override
	public Feedback run() {
		// no need to start anything if we are running on IIS
		if (this.isRunningOnLocalIIS()) {
			log("DotNet Procedure is running on local IIS");
			return Feedback.Neutral;
		}

		// ugly, but we need to access some methods from AbstractProcess here...
		AbstractProcess abstractProcess = (AbstractProcess) process;
		abstractProcess.setTimeout(ExecuteWatchdog.INFINITE_TIMEOUT);
		
		// set out/err streams to a logfile, if we have one defined
		String logfile = getLogfile();
		final OutputStream loggingStream;
		if(logfile != null) {
			loggingStream = new LogFileStream(logfile);
			abstractProcess.setOut(new PrintStream(new TeeOutputStream(System.out, loggingStream)));
			abstractProcess.setErr(new PrintStream(new TeeOutputStream(System.err, loggingStream)));
		} else {
			loggingStream = null;
		}

		// populate the input-stream as well
		blockingInputStream = new BlockingInputStream();
		abstractProcess.setIn(blockingInputStream);

		// now start the process
		return process.start(new Runnable() {
			@Override
			public void run() {
				IOUtils.closeQuietly(blockingInputStream);
				IOUtils.closeQuietly(loggingStream);
			}
		});
	}

	@Override
	public boolean isInstrumentationSupported() { // NOPMD
    	// we don't know the state of .NET Agent instrumentation on APM NG
		if(DtVersionDetector.isAPM()) {
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.dynatrace.easytravel.launcher.engine.AbstractProcessProcedure#agentFound()
	 */
	@Override
	public boolean agentFound() {
		DtAgentConfig agentConfig = process.getDtAgentConfig();

		// COR_PROFILER specifies the GUID to use
		String profiler = System.getenv(Constants.Misc.ENV_VAR_COR_PROFILER);
		if (agentConfig.getEnvironmentArgs().containsKey(Constants.Misc.ENV_VAR_COR_PROFILER)) {
			profiler = agentConfig.getEnvironmentArgs().get(Constants.Misc.ENV_VAR_COR_PROFILER);
		}

		// COR_ENABLE_PROFILING enabled/disables profiling
		String enabled = System.getenv(Constants.Misc.ENV_VAR_COR_ENABLE_PROFILING);
		if (agentConfig.getEnvironmentArgs().containsKey(Constants.Misc.ENV_VAR_COR_ENABLE_PROFILING)) {
			enabled = agentConfig.getEnvironmentArgs().get(Constants.Misc.ENV_VAR_COR_ENABLE_PROFILING);
		}

		// report .NET Agents as found if the COR_PROFILER and COR_ENABLE_PROFILING is set in the system environment or in the
		// configured environment variables.
		return profiler != null &&
				(enabled != null &&
				(enabled.equals("0x1") || enabled.equals("0x01")));
	}

	@Override
	public Technology getTechnology() {
		return Technology.DOTNET_20;
	}

	protected abstract void log(String logMessage);

	private static final Cache<URL, Boolean> CACHED = CacheBuilder.newBuilder()
		    .concurrencyLevel(4)
		    //.weakKeys()
		    .maximumSize(10)
		    .expireAfterWrite(5, TimeUnit.MINUTES)
		    .build();

	public static boolean checkIsRunningOnIIS(String site, int port) {
		if (site == null) {
			LOGGER.warn("Cannot check if site on port " + port +
					" is running on local IIS. Possible reason: URL to indentify easyTravel site seems to be missing in easyTravelConfig.properties.");
			return false;
		}

		final URL url = urlBuilder(site, port);

		// cache the information to avoid reading this multiple times!
		Boolean cached = CACHED.getIfPresent(url);
		if(cached != null) {
			return cached.booleanValue();
		}

		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			try {
				conn.setDoOutput(false);
				conn.setDoInput(true);
				conn.setConnectTimeout(IIS_REQUEST_TIMEOUT_MS);
				conn.setReadTimeout(IIS_REQUEST_TIMEOUT_MS);
				// if connecting is not possible this will throw a connection refused exception
				conn.connect();
				// if site searched for is not running on given port on local iis we return false here
				if (conn.getResponseCode() >= 400) {
					CACHED.put(url, false);
					return false;
				}

				boolean ret = iisHeaderCheck(conn);
				
				CACHED.put(url, ret);
				return ret;
			} finally {
				conn.disconnect();
			}
		} catch (IOException e) {
			/* exception is thrown -> server not available */
			LOGGER.info("Did not find IIS at " + url.toString());
			CACHED.put(url, false);
			return false;
		}
	}
	
	private static URL urlBuilder(String site, int port){
		URL url;
		try {
			String relativeUrl = site.startsWith("/") ? site : ("/" + site);
			url = new URL(LocalUriProvider.getLocalUriWithoutTrailingSlash(port, relativeUrl));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid destination URL '" + site + "' to check if site is running on local IIS", e);
		}
		return url;
	}
	
	private static boolean iisHeaderCheck(HttpURLConnection conn){
		String headerServer = conn.getHeaderField("Server");
		String serverDetails = conn.getHeaderField("Server-Details");			
		
		boolean iisExpress = "IIS-Express".equals(serverDetails);
		boolean ret = headerServer != null && headerServer.contains("IIS") && !iisExpress;
		
		String iis = (iisExpress) ? "IIS Express" : "IIS";
		LOGGER.info("Did find " + (ret || iisExpress ? iis + " running at "
				: "a website running, but could not detect IIS because header-field 'Server' was '"
						+ headerServer + "' at ")
				+ conn.getURL().toString());
		return ret;
	}

	/**
	 * for testing.
	 *
	 * @author cwat-dstadler
	 */
	public static void clearCache() {
		CACHED.invalidateAll();
	}

	/**
	 * @param processName name of the process to check
	 * @return <code>true</code> if the passed processName is 'allowed' a port necessary for a .NET process
	 *         For instance, the IIS (w3wp.exe) may use a port destined for a .NET process without an additional warning.
	 * @author stefan.moschinski
	 */
	public static boolean isProcessOnPortWhitelist(String processName) {
		if (StringUtils.isEmpty(processName)) {
			return true;
		}

		String lowerCaseProcessName = processName.toLowerCase();
		for (String processNameWhitelisted : PROCESS_WHITELIST) {
			if (lowerCaseProcessName.contains(processNameWhitelisted))
				return true;
		}
		return false;
	}

	@Override
	public boolean hasLogfile() {
	    return !isRunningOnLocalIIS();
	}
}
