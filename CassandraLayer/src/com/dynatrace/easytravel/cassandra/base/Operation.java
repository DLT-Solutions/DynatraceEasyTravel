package com.dynatrace.easytravel.cassandra.base;

import java.text.MessageFormat;

public enum Operation {
    PLUS("+"),//PLUS(BaseConstants.PLUS), TODO change it
    MINUS("-");//MINUS(BaseConstants.MINUS); TODO change it

    private String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return this.symbol;
    }

    public String getExpression(String param1, String param2) {
        return MessageFormat.format( "{0} {1} {2}", param1, toString(), param2 );
    }
}
