package com.dynatrace.easytravel.webservices;

import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import com.dynatrace.easytravel.TestUtil;

public class TestClientAXIOM {
	
    private static Logger log = Logger.getLogger(TestClientAXIOM.class.getName());

    private static EndpointReference targetEPR =
        new EndpointReference(
               TestUtil.getBusinessBackendUrl() + "/services/services/JourneyService");

    /**
     * Simple axis2 client.
     *
     * @param args Main
     */
    public static void main(String[] args) {
        try {
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = factory.createOMNamespace(
                        "http://springExample.org/example1", "example1");

            OMElement method = factory.createOMElement("getJourneyNames", omNs);
            //OMElement value = factory.createOMElement("Text", omNs);
            //value.addChild(factory.createOMText(value, "Some String "));
            //method.addChild(value);

            ServiceClient serviceClient = new ServiceClient();

            Options options = new Options();
            serviceClient.setOptions(options);
            options.setTo(targetEPR);

            //Blocking invocation
            OMElement result = serviceClient.sendReceive(method);

            StringWriter writer = new StringWriter();
            result.serialize(XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(writer));
            writer.flush();

            log.fine("Response: " + writer.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
