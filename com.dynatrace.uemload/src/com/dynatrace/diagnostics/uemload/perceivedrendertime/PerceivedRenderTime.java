package com.dynatrace.diagnostics.uemload.perceivedrendertime;

import java.util.List;

import com.dynatrace.diagnostics.uemload.BrowserWindowSize;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ResourceRequestSummary;

/**
 * this class represents the perceived render time value, which is being calculated
 * from the available image resources
 *
 * @author cwat-shauser
 *
 */
public class PerceivedRenderTime {

	private List<ResourceRequestSummary> loadedResources;
	/** the browser's window size */
	private BrowserWindowSize bws;
	/** load start time of the "page" */
	private long loadStartTime;
	/** calculated perceived render time value */
	private long prtval = -1;
	/** the url of the slowest image */
	private String slowestImageUrl = "";
	/** the latency of the action */
	private int latency;

	/**
	 * creates a new perceived render time object
	 *
	 * @param loadedResources the loaded resources which will determine the prt
	 * @param bws the browser's window size
	 * @param loadStart the load start time of the "page"
	 * @author cwat-shauser
	 * @param latency
	 */
	private PerceivedRenderTime(List<ResourceRequestSummary> loadedResources, BrowserWindowSize bws, long loadStart, int latency) {
		this.loadedResources = loadedResources;
		this.setBrowserWindowSize(bws);
		this.loadStartTime = loadStart;
		this.latency = latency;
	}

	/**
	 * factory method for a new perceived render time object
	 *
	 * @author cwat-shauser
	 * @param latency
	 */
	public static PerceivedRenderTime create(List<ResourceRequestSummary> loadedResources, BrowserWindowSize bws, long loadStart,
			int latency) {
		return new PerceivedRenderTime(loadedResources, bws, loadStart, latency);
	}

	public static PerceivedRenderTime create(List<ResourceRequestSummary> loadedResources, BrowserWindowSize bws, long loadStart) {
		return new PerceivedRenderTime(loadedResources, bws, loadStart, 0);
	}

	/**
	 * get the value of the perceived render time (cached, only calculated once)
	 *
	 * @return long the perceived render time
	 * @author cwat-shauser
	 */
	public long getValue(){
		if(this.prtval == -1){
			this.prtval = this.calcValue();
		}
		return this.prtval;
	}

	/**
	 * calculates the perceived render time from the given resources.
	 *
	 * only image resources will be considered for the calculation
	 *
	 * note: due to the fact that no positioning information about the image resources is available,
	 * the calculation of the perceived render time had to be reduced to take the maximum load time
	 * of a given image in order to create a plausible simulated value.
	 *
	 * if no images are available, the DOM ready time will be used instead
	 *
	 * @return
	 * @author cwat-shauser
	 */
	protected long calcValue(){
		// TODO consider browser window size as soon as image position becomes available
		long prt = 0;
		slowestImageUrl = "";
		if (this.loadedResources == null) {
			return -1;
		}
		for(ResourceRequestSummary sum : this.loadedResources){
			if(sum.isImage()){
				long loadDuration = sum.getLoadfinished() - loadStartTime;
				if (loadDuration > prt) {
					prt = loadDuration;
					slowestImageUrl = sum.getResourceUrl();
				}
			}
		}
		// Latency information is added to the perceived render time.
		return prt > 0 ? prt + latency : (System.currentTimeMillis() - this.loadStartTime);
	}

	public BrowserWindowSize getBrowserWindowSize() {
		return bws;
	}

	public void setBrowserWindowSize(BrowserWindowSize bws) {
		this.bws = bws;
	}

	public String getSlowestImageUrl() {
		return slowestImageUrl;
	}
}
