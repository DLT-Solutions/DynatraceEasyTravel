package com.dynatrace.diagnostics.uemload;

public class BrowserWindowSizeDistribution {
	
	private BrowserWindowSizeDistribution() {
		throw new IllegalAccessError("Utility class");
	}
	/**
	 * creates a random set of browser window size distributions.
	 *
	 * sources:
	 *
	 *  http://www.w3counter.com/globalstats.php (Jan 2012)
	 *  http://www.w3schools.com/browsers/browsers_display.asp
	 *
	 * @return
	 * @author cwat-shauser
	 */
	public static RandomSet<BrowserWindowSize> createDefaulBrowserWindowSize() {
		BrowserWindowSizeDistributionBuilder b = new BrowserWindowSizeDistributionBuilder();
		
		b.use(BrowserWindowSize._1366x768, 30);
		b.use(BrowserWindowSize._1920x1080, 15);
		b.use(BrowserWindowSize._1440x900, 6);
		b.use(BrowserWindowSize._1024x768, 6);
		b.use(BrowserWindowSize._1600x900, 6);
		b.use(BrowserWindowSize._1280x800, 6);
		b.use(BrowserWindowSize._1280x1024, 6);
		b.use(BrowserWindowSize._1680x1050, 3);
		b.use(BrowserWindowSize._1360x768, 2);
		b.use(BrowserWindowSize._1280x720, 2);
		b.use(BrowserWindowSize._640x360, 2);
		b.use(BrowserWindowSize._1536x864, 2);
		b.use(BrowserWindowSize._2560x1440, 1);
		b.use(BrowserWindowSize._1920x1200, 1);
		b.use(BrowserWindowSize._1280x768, 1);
		b.use(BrowserWindowSize._1024x600, 1);
		b.use(BrowserWindowSize._800x600, 1);
		b.use(BrowserWindowSize._1364x768, 1);
		b.use(BrowserWindowSize._1152x864, 1);
		b.use(BrowserWindowSize._2560x2048, 1);
		b.use(BrowserWindowSize._2048x1536, 1);
		b.use(BrowserWindowSize._1600x1200, 1);
		b.use(BrowserWindowSize._1440x960, 1);
		b.use(BrowserWindowSize._1400x1050, 1);

		return b.build();
	}
	
	public static RandomSet<BrowserWindowSize> createDefaulMobileBrowserWindowSize() {
		BrowserWindowSizeDistributionBuilder b = new BrowserWindowSizeDistributionBuilder();
		
		b.use(BrowserWindowSize._m640x360, 28);
		b.use(BrowserWindowSize._m667x375, 8);
		b.use(BrowserWindowSize._m568x320, 6);
		b.use(BrowserWindowSize._m800x480, 6);
		b.use(BrowserWindowSize._m1280x720, 6);
		b.use(BrowserWindowSize._m534x320, 4);
		b.use(BrowserWindowSize._m480x320, 3);
		b.use(BrowserWindowSize._m960x540, 3);
		b.use(BrowserWindowSize._m854x480, 3);
		b.use(BrowserWindowSize._m736x414, 2);
		b.use(BrowserWindowSize._m570x320, 2);
		b.use(BrowserWindowSize._m1920x1080, 2);
		b.use(BrowserWindowSize._m732x412, 1);
		b.use(BrowserWindowSize._m320x240, 1);
		b.use(BrowserWindowSize._m1184x720, 1);
		b.use(BrowserWindowSize._m592x360, 1);

		return b.build();
	}
	
	/**
	 * builder class to receive random browser window size
	 *
	 * @author cwat-shauser
	 */
	public static class BrowserWindowSizeDistributionBuilder {

		private RandomSet<BrowserWindowSize> BrowserWindowSizes = new RandomSet<BrowserWindowSize>();

		public BrowserWindowSizeDistributionBuilder use(BrowserWindowSize[] bwsarray){
			for(BrowserWindowSize bws : bwsarray){
				use(bws);
			}
			return this;
		}

		public BrowserWindowSizeDistributionBuilder use(BrowserWindowSize bws){
			return use(bws, 1);
		}


		public BrowserWindowSizeDistributionBuilder use(BrowserWindowSize bws, int weight) {
			BrowserWindowSizes.add(bws, weight);
			return this;
		}

		public RandomSet<BrowserWindowSize> build(){
			return BrowserWindowSizes;
		}
	}
}
