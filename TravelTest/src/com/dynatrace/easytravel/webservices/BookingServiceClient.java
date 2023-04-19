package com.dynatrace.easytravel.webservices;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.NotImplementedException;

//import com.dynatrace.easytravel.business.webservice.BookingServiceInterface;


public class BookingServiceClient /*implements BookingServiceInterface*/ {
    private static final Logger log = Logger.getLogger(WebServiceClient.class.getName());


    //private static final ObjectFactory WS_CLIENT_FACTORY = new     ObjectFactory();

   /* private  WebServiceTemplate webServiceTemplate;

    public BookingServiceClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }*/

    /*@Override
    public boolean cancelOrder(String orderRef) {
        logger.debug("Preparing CancelOrderRequest.....");
        CancelOrderRequest request =   WS_CLIENT_FACTORY.createCancelOrderRequest();
        request.setRefNumber(orderRef);

        logger.debug("Invoking Web service Operation[CancelOrder]....");
        CancelOrderResponse response = (CancelOrderResponse) webServiceTemplate.marshalSendAndReceive(request);

        logger.debug("Has the order cancelled: " + response.isCancelled());

        return response.isCancelled();
    }

    @Override
    public String placeOrder(Order order) {
        logger.debug("Preparing PlaceOrderRequest.....");
                PlaceOrderRequest request = WS_CLIENT_FACTORY.createPlaceOrderRequest();
                request.setOrder(order);

        logger.debug("Invoking Web service Operation[PlaceOrder]....");
                PlaceOrderResponse response = (PlaceOrderResponse) webServiceTemplate.marshalSendAndReceive(request);
        logger.debug("Order reference:

" + response.getRefNumber());
        return response.getRefNumber();
    }*/


  //  @Override
    public List<String> getBookingIds(String username) {
        //logger.info("Preparing B.....");
        //String username = "hainer";

        log.info("Invoking Web service Operation[getBookingIds]....");
        /*List<String> response = (List<String>) webServiceTemplate.marshalSendAndReceive((Object)username, new WebServiceMessageCallback() {
	        @Override
			public void doWithMessage(WebServiceMessage message) {
	            ((SoapMessage)message).setSoapAction("getBookingIds");
	            ((SoapMessage)message).getEnvelope().addNamespaceDeclaration("", "http://webservice.business.easytravel.dynatrace.com");
	        }
	    });
        logger.info("Had response: " + response);

        return response;*/
        return null;
    }


	//@Override
	public void storeBooking(Integer journeyId, String userName, String creditCard) {
		throw new NotImplementedException("test code");
	}

	public static void main(String[] args) throws IOException {
		/*ClassPathXmlApplicationContext context = null;
	    context = new ClassPathXmlApplicationContext("/META-INF/spring/application-context.xml");*/

	    //BookingServiceInterface client = (BookingServiceInterface) context.getBean("bookingServiceClient");

	    //List<String> bookingIds = client.getBookingIds("hainer");
		//logger.info("Had result: " +bookingIds);
	}
}
