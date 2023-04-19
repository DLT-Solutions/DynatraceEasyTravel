package com.dynatrace.easytravel;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

public class QueueConfiguration {
	private ConnectionStringBuilder csb;
    private ConnectionFactory cf;
    private Destination destination;


    public QueueConfiguration(ConnectionStringBuilder csb, ConnectionFactory cf, Destination destination) {
        this.csb = csb;
        this.cf = cf;
        this.destination = destination;
    }

    public ConnectionStringBuilder getCsb() {
        return csb;
    }

    public ConnectionFactory getCf() {
        return cf;
    }

    public Destination getDestination() {
        return destination;
    }
}
