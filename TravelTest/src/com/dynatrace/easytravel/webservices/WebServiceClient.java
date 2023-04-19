package com.dynatrace.easytravel.webservices;


import java.io.IOException;

public class WebServiceClient {

    /*private static final String MESSAGE =
        "<message xmlns=\"http://webservice.business.easytravel.dynatrace.com\" username=\"hainer\"></message>";*/

    /*private final WebServiceTemplate webServiceTemplate = new WebServiceTemplate();

    public void setDefaultUri(String defaultUri) {
        webServiceTemplate.setDefaultUri(defaultUri);
    }

    // send to the configured default URI
    public void simpleSendAndReceive() {
        StreamSource source = new StreamSource(new StringReader(MESSAGE));
        StreamResult result = new StreamResult(System.out);
        webServiceTemplate.sendSourceAndReceiveToResult(source, result);
    }*/

    // send to an explicit URI
    public static void customSendAndReceive() {
    	//WebServiceTemplate webServiceTemplate = new WebServiceTemplate();

	    /*webServiceTemplate.marshalSendAndReceive((Object)"hainer", new WebServiceMessageCallback() {
	        public void doWithMessage(WebServiceMessage message) {
	            //((SoapMessage)message).setSoapAction("http://tempuri.org/Action");
	        	((SoapMessage)message).
	        }
	    });*/

        /*StreamSource source = new StreamSource(new StringReader(MESSAGE));
        StreamResult result = new StreamResult(System.out);
        webServiceTemplate.sendSourceAndReceiveToResult("http://localhost:8091/services/services/BookingService/getBookingIds",
            source, result);*/

    	/*webServiceTemplate.setMarshaller(new org.springframework.oxm.jaxb.Jaxb2Marshaller());
    	webServiceTemplate.setUnmarshaller(new org.springframework.oxm.jaxb.Jaxb2Marshaller());

    	Object response = webServiceTemplate.marshalSendAndReceive("http://localhost:8091/services/services/BookingService/getBookingIds", "hainer");
    	logger.info("Got back: " + response);
    	*/

        //webServiceTemplate.send
    }

    //private static final Log logger = LogFactory.getLog(WebServiceClient.class);
//    private static final ObjectFactory WS_CLIENT_FACTORY = new     ObjectFactory();

    //private  WebServiceTemplate webServiceTemplate;

    /*public OrderServiceClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }*/

    //@Override
    /*public boolean cancelOrder(String orderRef) {
        logger.debug("Preparing CancelOrderRequest.....");
        CancelOrderRequest request =   WS_CLIENT_FACTORY.createCancelOrderRequest();
        request.setRefNumber(orderRef);

        logger.debug("Invoking Web service Operation[CancelOrder]....");
        CancelOrderResponse response = (CancelOrderResponse) webServiceTemplate.marshalSendAndReceive(request);

        logger.debug("Has the order cancelled: " + response.isCancelled());

        return response.isCancelled();
    }*/

    /*@Override
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

	public static void main(String[] args) throws IOException {
		customSendAndReceive();
	}
}
