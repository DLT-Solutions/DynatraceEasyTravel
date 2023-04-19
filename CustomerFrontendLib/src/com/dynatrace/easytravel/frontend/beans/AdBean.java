package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Version;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.util.TextUtils;

import static com.codahale.metrics.Timer.Context;

@ManagedBean
@SessionScoped
public class AdBean implements Serializable {

	private static final long serialVersionUID = -1848753674748640171L;
	private static final Logger log = LoggerFactory.make();

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_PROMOTION);
	private static final GenericPluginList thirdPartyPlugins = new GenericPluginList(PluginConstants.FRONTEND_THIRDPARTYADSERVER);
    private static final Object lockOfDeath1 = new Object();
    private static final Object lockOfDeath2 = new Object();

	private transient Promotion[] promotions;

	private volatile int curr;

	@ManagedProperty("#{dataBean}")
	private DataBean dataBean;

	private Promotion loadJourney(Promotion promotion) {
		if (promotion.getJourney() == null) {
            final Context context = Metrics.getTimerContext(this, "loadJourney");
	    	try {
				JourneyDO[] journeys = dataBean.getDataProvider().findJourneys(promotion.getLocation(), null, null);
				if (journeys != null && journeys.length > 0) {
			    	promotion.setJourney(journeys[0]);
		    	} else {
		    		log.warn("Could not find any Journey to " + promotion.getLocation());
		    	}
			} catch (RemoteException e) {
	    		log.warn("RemoteException while loading journey: " + e.getMessage());
			} finally {
                context.stop();
                context.close();
            }
        }
		return promotion;
	}

	public Promotion getCurrentPromotion() {
		return loadJourney(getPromotions()[curr]);
	}

	public String getBannerUrl() {
		AtomicReference<String> url = new AtomicReference<String>("");
		thirdPartyPlugins.execute(PluginConstants.FRONTEND_THIRDPARTYADSERVER, url);
		if (!url.get().isEmpty()) {
			return url.get();
		}	
		return TextUtils.merge(BaseConstants.Images.THIRDPARTY_ADVERTISMENT_IMAGE_TEMPLATE, "img/easyTravel_banner.png");
	}

	public void setDataBean(DataBean dataBean) {
		this.dataBean = dataBean;
	}

	public void clickImage() {
		AtomicBoolean deadlockEnabled = new AtomicBoolean(false);
		plugins.execute(PluginConstants.FRONTEND_PROMOTION_CLICK, deadlockEnabled);
    	if (deadlockEnabled.get()) {
    	    synchronized(lockOfDeath1) {
                try {
                    new Thread() {
                        @Override()
                        public void run() {
                            synchronized(lockOfDeath2) {
                                log.info("thread " + this + " attempting to synchronize on lockOfDeath");
                                synchronized(lockOfDeath1) {
                                    log.info("thread " + this + " synchronized on lockOfDeath (impossible)");
                                }
                            }
                        }
                    }.start();
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    log.info(ie.getMessage());
                }
                log.info("thread " + this + " attempting to synchronize on lockOfDeath 2");
                synchronized(lockOfDeath2) {
                    log.info("thread " + this + " synchronized on lockOfDeath 2 (impossible)");
                }
            }
    	}
	}

	private Promotion[] getPromotions() {
		if (promotions == null) {
			promotions = new Promotion[] {
				new Promotion("Paris"),
				new Promotion("Hawaii"),
				new Promotion("Beijing")
			};
			curr = new Random().nextInt(promotions.length);
		}
		return promotions;
	}

	public Version getVersion() {
		return Version.read();
	}
	
	public String getBuildDate(){
		Version v = Version.read();
		return v.getBuilddateString();
	}
}
