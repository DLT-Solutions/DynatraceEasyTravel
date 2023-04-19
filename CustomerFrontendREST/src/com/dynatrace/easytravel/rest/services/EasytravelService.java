package com.dynatrace.easytravel.rest.services;

import static com.dynatrace.easytravel.frontend.rest.Constants.ErrorMessages.*;
import static com.dynatrace.easytravel.frontend.rest.Constants.Fields.CREDIT_CARD_NUMBER;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.Path.*;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.PathParam.*;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.QueryParam.*;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.http.HttpStatus;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.beans.JourneyBean;
import com.dynatrace.easytravel.frontend.data.DataProvider;
import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.frontend.data.UserDO;
import com.dynatrace.easytravel.frontend.lib.CustomerFrontendUtil;
import com.dynatrace.easytravel.frontend.lib.RequestProxy;
import com.dynatrace.easytravel.frontend.login.LoginLogic;
import com.dynatrace.easytravel.frontend.login.UserContext;
import com.dynatrace.easytravel.frontend.plugin.WebservicePluginStateProxy;
import com.dynatrace.easytravel.frontend.rest.data.*;
import com.dynatrace.easytravel.json.JSONObject;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.UserType;
import com.dynatrace.easytravel.rest.DTOMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;

/**
 *
 * @author Michal.Bakula
 *
 */
@Path("/")
public class EasytravelService {

	private static final Logger log = LoggerFactory.make();

	// search limit
	private static final int LIMIT_LOCATIONS = 10;

	private static final String ANGULAR_SEARCH_KEY = "Angular";
	private static final String DELIMITER = ":";

	private static final String EMPTY_STRING = "";
	private static final DataProviderInterface DATA_PROVIDER = new DataProvider();
	private static final WebservicePluginStateProxy PLUGIN_DATA_PROVIDER = new WebservicePluginStateProxy();
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final EasyTravelConfig config = EasyTravelConfig.read();

	/**
	 * Basic services required for new frontend traffic generation.
	 */

	@POST
	@Path(LOGIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logInUser(LoginUserDTO user) {
		try {
			UserContext uc = new UserContext();
			LoginLogic.authenticate(user.getUsername(), user.getPassword(), uc);
			if (uc.isAuthenticated()) {
				return Response.status(HttpStatus.OK.value()).entity(mapper.writeValueAsString(new UserAuthenticationDTO(DTOMapperFactory.convert(uc)))).build();
			} else {
				return Response.status(HttpStatus.FORBIDDEN.value()).entity(mapper.writeValueAsString(new ErrorDTO(AUTHENTICATION_FAILURE))).build();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@POST
	@Path(SIGNIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signInUser(UserDTO user) {
		try {
			boolean isAdded = LoginLogic.addNewUser(user.getEmail(), String.format("%s %s", user.getFirstName(), user.getLastName()), user.getEmail(), user.getPassword());
			if (isAdded) {
				return Response.status(HttpStatus.OK.value()).entity(mapper.writeValueAsString(new UserAuthenticationDTO(user))).build();
			} else {
				return Response.status(HttpStatus.FORBIDDEN.value()).entity(mapper.writeValueAsString(new ErrorDTO(REGISTRATION_FAILURE))).build();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@GET
	@Path(SPECIAL_OFFERS)
	@Produces(MediaType.APPLICATION_JSON)
	public JourneyDTO[] getSpecialOffers(){
		List<JourneyDO> journeys = CustomerFrontendUtil.getRandomJourneys(DATA_PROVIDER);
		return DTOMapperFactory.convert(journeys.toArray(new JourneyDO[journeys.size()]));
	}

	@GET
	@Path(RECOMMENDATIONS)
	@Produces(MediaType.APPLICATION_JSON)
	public JourneyDTO[] getRecommendations(){
		List<JourneyDO> journeys = CustomerFrontendUtil.getRecommendedJourneys(DATA_PROVIDER);
		return DTOMapperFactory.convert(journeys.toArray(new JourneyDO[journeys.size()]));
	}

	@GET
	@Path(JOURNEYS)
	@Produces(MediaType.APPLICATION_JSON)
	public JourneyDTO[] searchJourney(@QueryParam(MATCH) String destination,
									 @QueryParam(FROM) DateParam from,
									 @QueryParam(TO) DateParam to) {
		try {
			Date fromDate = (from != null) ? from.getDate() : null;
			Date toDate = (to != null) ? to.getDate() : null;
			return DTOMapperFactory.convert(DATA_PROVIDER.findJourneys(destination, fromDate, toDate));
		} catch (RemoteException e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@GET
	@Path(LOCATIONS)
	@Produces(MediaType.APPLICATION_JSON)
	public LocationDTO[] searchLocations(@QueryParam(MATCH) String text) {
		try {
			return DTOMapperFactory.convert(DATA_PROVIDER.findLocations(text, LIMIT_LOCATIONS));
		} catch (RemoteException e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@POST
	@Path(VALIDATE_CREDIT_CARD)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response validateCreditCard(String number) {
		try {
			CardValidationDTO obj = new CardValidationDTO(DATA_PROVIDER.checkCreditCard(new JSONObject(number).getString(CREDIT_CARD_NUMBER)));
			return Response.status(200).entity(mapper.writeValueAsString(obj)).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@GET
	@Path(RECENT_BOOKINGS)
	@Produces(MediaType.APPLICATION_JSON)
	public BookingDTO[] getRecentBookings(@PathParam(LIMIT) Integer limit) {
		try {
			return DTOMapperFactory.convert(DATA_PROVIDER.getRecentBookings(limit));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@POST
	@Path(BOOKINGS)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response storeBooking(StoreBookingDTO bookingDTO) {
		return storeBooking(UserType.WEB, bookingDTO);
	}
	
	@POST
	@Path(BOOKINGS_MOBILE)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response storeBookingMobile(StoreBookingDTO bookingDTO) {
		return storeBooking(UserType.MOBILE, bookingDTO);
	}
	
	private Response storeBooking(UserType userType, StoreBookingDTO bookingDTO) {
		try {
			//TODO: travelers should be set from value passed in bookingDTO, the whole JourneyAccount object should be mapped from StoreBookingDTO object
			JourneyPriceHelper jph = new JourneyPriceHelper();
			bookingDTO.setAmount(jph.getTotalCosts(bookingDTO.getAmount(), 6, PLUGIN_DATA_PROVIDER.getEnabledPlugins()));

			String bookingId = DATA_PROVIDER.storeBooking(bookingDTO.getJourneyId(), bookingDTO.getUsername(), userType, bookingDTO.getCreditcard(), bookingDTO.getAmount());
			if(bookingId != null) {
				JourneyDO journey = DATA_PROVIDER.getJourneyById(bookingDTO.getJourneyId());
				BookingConfirmationDTO obj = new BookingConfirmationDTO(bookingId, bookingDTO.getJourneyId(), journey.getFromDate(), journey.getToDate());
				return Response.status(HttpStatus.OK.value()).entity(mapper.writeValueAsString(obj)).build();
			} else {
				return Response.status(HttpStatus.FORBIDDEN.value()).entity(mapper.writeValueAsString(new ErrorDTO(BOOKING_FAILURE))).build();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Response.status(500).entity("E-1234").build();
		}
	}

	@GET
	@Path(BOOKING)
	@Produces(MediaType.APPLICATION_JSON)
	public BookingDTO getBooking(@PathParam(BOOKING_ID) String bookingId) {
		try {
			return DTOMapperFactory.convert(DATA_PROVIDER.getBookingById(bookingId));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Additional services
	 */

	@GET
	@Path(USERS_RANDOM)
	@Produces(MediaType.APPLICATION_JSON)
	public UserDO[] getTwentyUsers() {
		try {
			UserDO[] responseUsers = DATA_PROVIDER.getUsersWithPrefix(EMPTY_STRING);

			Arrays.sort(responseUsers, new Comparator<UserDO>() {
				@Override
				public int compare(UserDO o1, UserDO o2) {
					return o1.getName().compareTo(o2.getName());
	            }
	        });

			return responseUsers;
		} catch (RemoteException e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@GET
	@Path(USERS)
	@Produces(MediaType.APPLICATION_JSON)
	public UserDO[] getUsersWithPrefix(@QueryParam(MATCH) String text) {
		try {
			UserDO[] responseUsers = DATA_PROVIDER.getUsersWithPrefix(text);

			Arrays.sort(responseUsers, new Comparator<UserDO>() {
				@Override
				public int compare(UserDO o1, UserDO o2) {
					return o1.getName().compareTo(o2.getName());
	            }
	        });

			return responseUsers;
		} catch (RemoteException e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@GET
	@Path(GET_JOURNEY)
	@Produces(MediaType.APPLICATION_JSON)
	public JourneyDTO getJourneyById(@PathParam(JOURNEY_ID) Integer journeyId) {
		try {
			JourneyDTO responseJourney = DTOMapperFactory.convert(DATA_PROVIDER.getJourneyById(journeyId));
			if(responseJourney == null) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
			return responseJourney;
		} catch (RemoteException e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@GET
	@Path(GET_JOURNEY_POST_COUNT)
	@Produces(MediaType.APPLICATION_JSON)
	public int getJourneyPostCount(@PathParam(JOURNEY_ID) Integer journeyId) {
		return new JourneyBean().getJourneyPostCount(journeyId);
	}

	@GET
	@Path(GET_ENABLED_PLUGINS)
	@Produces(MediaType.APPLICATION_JSON)
	public String[] getEnabledAngularPluginNames() {
		String[] enabledPlugins = PLUGIN_DATA_PROVIDER.getEnabledPlugins();
		String[] enabledAngularPlugins = null;

		if (enabledPlugins != null) {
			enabledAngularPlugins = Arrays
					.stream(enabledPlugins)
					.filter(s -> isAngularPlugin(s))
					.map(s -> s.substring(0, s.indexOf(DELIMITER)))
					.toArray(String[]::new);
		}

		return enabledAngularPlugins;
	}

	protected boolean isAngularPlugin(String pluginDetails) {
		boolean result = false;

		if (pluginDetails != null && pluginDetails.length() > 0) {
			String[] afterSpliting = pluginDetails.split(DELIMITER);
			if (afterSpliting.length > 1) {
				return afterSpliting[1].indexOf(ANGULAR_SEARCH_KEY) >= 0;
			}
		}

		return result;
	}

	@GET
	@Path(GET_RANDOM_IMAGES)
	@Produces(MediaType.APPLICATION_JSON)
	public JourneyImageDTO getRandomImagePath() {
		return new JourneyImageDTO();
	}

	@GET
	@Path(GET_NODEJS_URL)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodejsURL() {
		try {
			return Response.status(HttpStatus.OK.value()).entity(mapper.writeValueAsString(config.nodejsURL)).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}

	@GET
	@Path(GET_BLOG_URL)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBlogURL() {
		try {
			String configUrl = config.wordpressBlogUrl;
			if(configUrl.isEmpty()) {
				configUrl = "http://" + config.apacheWebServerHost + ":" + config.apacheWebServerPort + "/blog/";
			}
			return Response.status(HttpStatus.OK.value()).entity(mapper.writeValueAsString(configUrl)).build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
		}
	}
	
	@POST
	@Path(POST_OPENTELEMETRY)
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	public Response postOpentelemetry(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		if (config.angularOpenTelemetryForwardUrl.isEmpty()) {
			log.info("Cannot forward OpenTelemetry data. Forward URL is empty.");
			return Response.status(HttpStatus.OK.value()).build();
		}
		
		try {
			RequestProxy.instance().forward(request, response, config.angularOpenTelemetryForwardUrl);
			return Response.status(HttpStatus.OK.value()).build();
		} catch (Exception e) {
			log.warn("Cannot forward OpenTelemetry data.", e);
			throw new WebApplicationException(e, Response.Status.SERVICE_UNAVAILABLE);
		}
	}
}
