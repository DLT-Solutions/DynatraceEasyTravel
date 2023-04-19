package com.dynatrace.easytravel.frontend.rest;

public class Constants {
	public interface Fields {
		public static String USER = "user";
		public static String FIRSTNAME = "firstname";
		public static String LASTNAME = "lastname";
		public static String EMAIL = "email";
		public static String PASSWORD = "password";
		public static String STATE = "state";
		public static String CITY = "city";
		public static String STREET = "street";
		public static String DOOR = "door";
		public static String PHONE = "phone";
		public static String LOYALTY_STATUS = "loyaltystatus";
		public static String API_TOKEN = "apiToken";
		public static String ERROR = "error";
		public static String VALID = "valid";
		public static String CREDIT_CARD_NUMBER = "creditCardNumber";
	}

	public interface ErrorMessages {
		public static String AUTHENTICATION_FAILURE = "User authentication failure.";
		public static String REGISTRATION_FAILURE = "The email address you entered is already registered.";
		public static String BOOKING_FAILURE = "Could not book journey.";
	}

	public interface RestCall {

		public interface Path {
			public static String LOGIN = "/login";
			public static String SIGNIN = "/signin";
			public static String SPECIAL_OFFERS = "/journeys/special-offers";
			public static String RECOMMENDATIONS = "/journeys/recommendation";
			public static String JOURNEYS = "/journeys";
			public static String LOCATIONS = "/locations";
			public static String VALIDATE_CREDIT_CARD = "/validate-creditcard";
			public static String RECENT_BOOKINGS = "/bookings/recent/{limit}";
			public static String BOOKINGS = "/bookings";
			public static String BOOKINGS_MOBILE = "/bookings-mobile";
			public static String BOOKING = "/bookings/{bookingId}";
			public static String USERS_RANDOM = "/users/random";
			public static String USERS = "/users";
			public static String GET_JOURNEY = "/journeys/{journeyId}";
			public static String GET_JOURNEY_POST_COUNT = "/journeys/{journeyId}/post-count";
			public static String GET_RANDOM_IMAGES = "/images/random";
			public static String GET_ENABLED_PLUGINS = "/plugins/enabled";
			public static String GET_NODEJS_URL = "/config/nodejsurl";
			public static String GET_BLOG_URL = "/config/blogurl";
			public static String POST_OPENTELEMETRY = "/opentelemetry";
		}

		public interface PathParam {
			public static String BOOKING_ID = "bookingId";
			public static String LIMIT = "limit";
			public static String JOURNEY_ID = "journeyId";
		}

		public interface QueryParam {
			public static String MATCH = "match";
			public static String FROM = "from";
			public static String TO = "to";
			public static String NUMBER_OF_PEOPLE = "numberOfPeople";
		}
	}
}
