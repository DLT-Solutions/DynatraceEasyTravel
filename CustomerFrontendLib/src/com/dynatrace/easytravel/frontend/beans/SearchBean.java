package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import com.dynatrace.easytravel.metrics.Metrics;
import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.frontend.data.LocationDO;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.icesoft.faces.component.selectinputtext.SelectInputText;
import com.icesoft.faces.component.selectinputtext.TextChangeEvent;

import ch.qos.logback.classic.Logger;

import static com.codahale.metrics.Timer.Context;

/**
 * This JSF managed bean handles search for journeys.
 *
 * @author philipp.grasboeck
 */
@ManagedBean
@SessionScoped
public class SearchBean implements Serializable {

	private static final long serialVersionUID = 1850474473676295996L;
	private static final Logger log = LoggerFactory.make();

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_SEARCH);

	private static final GenericPluginList beforeResultsPlugins = new GenericPluginList(PluginConstants.FRONTEND_RESULTS_BEFORE);
	private static final GenericPluginList afterResultsPlugins = new GenericPluginList(PluginConstants.FRONTEND_RESULTS_AFTER);

	private static final GenericPluginList tripDetailsPlugins = new GenericPluginList(PluginConstants.FRONTEND_TRIPDETAILS_WEATHER_FORECAST_LINK);

	// search limits
	static final int LIMIT_LOCATIONS = 15;
	static final int LIMIT_LOCATIONS_INEXACT = 30;
	static final int MIN_SEARCH_LENGTH = 2;

	// paging constants
	private static final int DEFAULT_PAGE_SIZE = 8;
	private static final int MAX_PAGES = 10;
	private static final SelectItem[] PAGE_LIST = createBackingPageList(MAX_PAGES);
	private static final List<Integer> PAGE_SIZE_LIST = Arrays.asList(4, 8, 12, 24);

	@ManagedProperty("#{dataBean}")
	private DataBean dataBean;

	@ManagedProperty("#{searchDelayStateBean}")
	private SearchDelayStateBean searchDelayStateBean;

	private final List<SelectItem> selectList = new ArrayList<SelectItem>(); // proposals
	private String destinationName;
	private String lastSearchedDestinationName;
	private LocationDO destination;
	private Date fromDate;
	private Date toDate;
	private JourneyDO[] journeys; // all found journeys
	private JourneyDO[] journeyPage; // journeys on current page
	private int page;
	private int pageCount;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private int journeyCount = -1;

	private transient List<SelectItem> pageList;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private JourneyDO[] hotDeals = new JourneyDO[0];

	/* --- public API --- */

	/**
	 * Reset the search form and the search results.
	 *
	 * @author philipp.grasboeck
	 */
	public void clearSearch() {
		clearResults();
		clearInputs();
	}

	private void clearResults() {
		journeys = journeyPage = null;
		page = pageCount = 0;
		journeyCount = 0;
	}

	private void clearInputs() {
		destination = null;
		destinationName = null;
		fromDate = toDate = null;
		journeyCount = -1;
	}


	/**
	 * Searches for trip destinations (LocationDO objects)
	 * in the auto-complete field of the selectInputText.
	 * This is invoked at every single text change.
	 *
	 * The implementation searches for locations matching the user
	 * input and adjusts the select list accordingly.
	 *
	 * @param e
	 * @author philipp.grasboeck
	 */
	public void searchDestination(TextChangeEvent e) {
		if (log.isDebugEnabled())
			log.debug("text changed: " + e.getNewValue());
		String newDestinationName = String.valueOf(e.getNewValue()).trim();
		selectList.clear();
		if (newDestinationName.length() < MIN_SEARCH_LENGTH) {
			return;
		}
        final Context context = Metrics.getTimerContext(this, "searchDestination");
		try {
			LocationDO[] locations = dataBean.getDataProvider().findLocations(newDestinationName, LIMIT_LOCATIONS);
            for (LocationDO location : locations) {
				selectList.add(new SelectItem(location, location.getName()));
			}
		} catch (RemoteException ex) {
			log.error("SearchBean.searchDestination: " + ex.getMessage());
		} finally {
            context.stop();
            context.close();
        }
    }

	/**
	 * Select a trip destination (LocationDO object) from the
	 * auto-complete list.
	 * This is invoked when the user selects one entry in the list
	 * via mouse or Enter key, or presses the Enter key.
	 *
	 * The implementation sets the <code>destination</code> member,
	 * if the user picked a destination from the list, if not, it
	 * is set to null.
	 * Then, it triggers a search for journeys.
	 *
	 * @param e
	 * @author philipp.grasboeck
	 */
	public void selectDestination(ActionEvent e) {
		SelectItem item = ((SelectInputText) e.getComponent()).getSelectedItem();
		destination = (item != null) ? (LocationDO) item.getValue() : null;
//		find();
	}

	/**
	 * Start to search for journeys.
	 * This is invoked when pressing the [Search] button.
	 *
	 * @param e
	 * @author philipp.grasboeck
	 */
	public void searchJourneys(ActionEvent e) {
		find();
	}

	/**
	 * Start to search for journeys.
	 * This is invoked when using the result view that triggers a
	 * pre-render event.
	 *
	 * @author philipp.grasboeck
	 */
	public void searchJourneysFromView() {
		findJourneysByLocation(destinationName, fromDate, toDate);
	}

	public Date getFromDate() {
		return fromDate;
	}

	// ugly, but can't f:param get to work like f:viewParam
	public String getFromDateAsString() {
		return fromDate != null ? dateFormat.format(fromDate) : BaseConstants.EMPTY_STRING;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	// ugly, but can't f:param get to work like f:viewParam
	public String getToDateAsString() {
		return toDate != null ? dateFormat.format(toDate) : BaseConstants.EMPTY_STRING;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public JourneyDO[] getJourneys() {
		return journeys;
	}

	public JourneyDO[] getJourneyPage() {

		return journeyPage;
	}

	public void setJourneys(JourneyDO[] journeys) {
		this.journeys = ArrayUtils.clone(journeys);
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public String getLastSearchedDestinationName() {
		return destinationName;
	}

	public void setLastSearchedDestinationName(String lastSearchedDestinationName) {
		this.lastSearchedDestinationName = lastSearchedDestinationName;
	}

	public void setDataBean(DataBean dataBean) {
		this.dataBean = dataBean;
	}

	public List<SelectItem> getSelectList() {
		return selectList;
	}

	public SearchDelayStateBean getSearchDelayStateBean() {
		return searchDelayStateBean;
	}

	public void setSearchDelayStateBean(SearchDelayStateBean searchDelayStateBean) {
		this.searchDelayStateBean = searchDelayStateBean;
	}

	public int getJourneyCount() {
		return journeyCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	/* --- search implementation --- */

	// choose the right search algorithm
	private void find() {
		lastSearchedDestinationName = destinationName;
		if (destination != null && destination.getName().equals(destinationName)) {
			findJourneys(destinationName, fromDate, toDate);
		} else {
			findJourneysByLocation(destinationName, fromDate, toDate);
		}
	}

	// exact search: searches for journeys to that exact location
	private void findJourneys(String newDestinationName, Date fromDate, Date toDate) {
		if (log.isDebugEnabled())
			log.debug("search for " + newDestinationName);
		if (newDestinationName.length() < MIN_SEARCH_LENGTH) {
			clearResults();
			return;
		}

        final Context context = Metrics.getTimerContext(this, "findJourneys");
		try {
			if (log.isDebugEnabled())
				log.debug("Destination: " + newDestinationName);
			calcPage(dataBean.getDataProvider().findJourneys(newDestinationName, fromDate, toDate));
		} catch (RemoteException ex) {
			log.error("SearchBean.findJourneys: " + ex.getMessage());
		} finally {
            context.stop();
            context.close();
        }

		delay();
	}

	// inexact search: searches for loactions and all journeys to that location matching destination
	private void findJourneysByLocation(String newDestinationName, Date fromDate, Date toDate) {
		if (log.isDebugEnabled())
			log.debug("search for " + newDestinationName);
		if (newDestinationName.length() < MIN_SEARCH_LENGTH) {
			clearResults();
			return;
		}

        final Context context = Metrics.getTimerContext(this, "findJourneysByLocation");
		try {
			List<JourneyDO> newJourneyList = new ArrayList<JourneyDO>();
			LocationDO[] locations = dataBean.getDataProvider().findLocations(newDestinationName, LIMIT_LOCATIONS_INEXACT);
			if (locations != null) {
				for (LocationDO location : locations) {
					JourneyDO[] newJourneys = dataBean.getDataProvider().findJourneys(location.getName(), fromDate, toDate);
					if (newJourneys != null) {
						for (JourneyDO journey : newJourneys) {
							newJourneyList.add(journey);
						}
					}
				}
				calcPage(newJourneyList.toArray(new JourneyDO[newJourneyList.size()]));
			}
		} catch (RemoteException ex) {
			log.error("SearchBean.findJourneysByLocation: " + ex.getMessage());
		} finally {
            context.stop();
            context.close();
        }

		delay();
	}

	// perform the search delay adjusted in the searchDelayStateBean
	private void delay() {
		if (searchDelayStateBean.getDelaytime() > 0) {
			if (log.isDebugEnabled()) {
				StringBuilder debugmessage = new StringBuilder();
				debugmessage.append("delaying search for ");
				debugmessage.append(searchDelayStateBean.getDelaytime());
				debugmessage.append(" [ms]");
				log.debug(debugmessage.toString());
			}
			SearchDelayUtility delayUtil = new SearchDelayUtility();
			delayUtil.doWait(searchDelayStateBean.getDelaytime(), searchDelayStateBean.getWaitingStrategyEnum());
		}
	}

	/* --- paging implementation --- */

	private static final SelectItem[] createBackingPageList(int max) {
		SelectItem[] result = new SelectItem[max];
		for (int i = 0; i < max; i++) {
			result[i] = new SelectItem(i, BaseConstants.EMPTY_STRING);
		}
		return result;
	}

	/**
	 * Returns the list of pages the user can choose from.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public List<SelectItem> getPageList() {
		List<SelectItem> pageList = this.pageList;
		if (pageList == null) {
			this.pageList = pageList = new AbstractList<SelectItem>() {

				@Override
				public SelectItem get(int index) {
					return PAGE_LIST[index];
				}

				@Override
				public int size() {
					return pageCount;
				}
			};
		}
		return pageList;
	}

	/**
	 * Returns a list of page size the user can pick.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public List<Integer> getPageSizeList() {
		return PAGE_SIZE_LIST;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		recalcPage(page); // adjust the page when changing the current page in the UI
	}

	public void nextPage() {
		recalcPage(page + 1);
	}

	public void prevPage() {
		recalcPage(page - 1);
	}

	public int getDefaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		recalcPage(page); // adjust the page when changing the page size in the UI
	}

	/**
	 * Calculate a journey page.
	 * This must be invoked when the journey list changes.
	 *
	 * in: newJourneys, pageSize
	 * out: pageCount, journeyCount, page, journeyPage
	 *
	 * @author philipp.grasboeck
	 * @param newJourneys the new journey result array
	 */
	private void calcPage(JourneyDO[] newJourneys) {
		JourneyDO[] newHotDeals = getHotDeals();

		if (!Arrays.equals(newHotDeals, hotDeals) || !Arrays.equals(newJourneys, journeys)) {
			hotDeals = newHotDeals;
			journeys = ArrayUtils.addAll(hotDeals, newJourneys);
			journeyCount = journeys.length;
			recalcPage(0);
		}
	}

	private JourneyDO[] getHotDeals() {
		for (Object obj : plugins.execute(PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE)) {
			if (obj instanceof JourneyDO[]) {
				return (JourneyDO[]) obj; // override the current page with the plugin-provided page
			}
		}
		return new JourneyDO[0];
	}

	/**
	 * Calculate a journey page.
	 * This must be invoked when the user picked a different page.
	 *
	 * in: journeys, page, pageSize, pageCount
	 * out: page (corrected), pageCount (corrected), journeyPage
	 *
	 * @author philipp.grasboeck
	 * @param newPage the new page number
	 */
	private void recalcPage(int newPage) {
		int newPageCount = (journeyCount - 1) / pageSize + 1;
		if (newPageCount > MAX_PAGES) {
			newPageCount = MAX_PAGES;
		}
		if (newPage >= newPageCount) {
			newPage = newPageCount - 1;
		}
		int from = newPage * pageSize;
		int to = from + pageSize;
		if (to > journeyCount) {
			to = journeyCount;
		}
		page = newPage;
		pageCount = newPageCount;
		journeyPage = Arrays.copyOfRange(journeys, from, to);
	}

	public String getBeforeResults() {
		return getPluginContent(PluginConstants.FRONTEND_RESULTS_BEFORE, lastSearchedDestinationName, beforeResultsPlugins);
	}

	public String getAfterResults() {
		return getPluginContent(PluginConstants.FRONTEND_RESULTS_AFTER, lastSearchedDestinationName, afterResultsPlugins);
	}

	public String getWeatherForecastLink(JourneyDO journey) {
		for (Object object : tripDetailsPlugins.execute(PluginConstants.FRONTEND_TRIPDETAILS_WEATHER_FORECAST_LINK, journey)) {
			if (object != null) {
				return object.toString();
			}
		}
		return null;
	}

	private String getPluginContent(String position, String destinationName, GenericPluginList plugins) {
		StringBuilder buf = new StringBuilder();
		for (Object object : plugins.execute(position, destinationName)) {
			if (object != null) {
				buf.append(object);
			}
		}

		return buf.toString();
	}
}
