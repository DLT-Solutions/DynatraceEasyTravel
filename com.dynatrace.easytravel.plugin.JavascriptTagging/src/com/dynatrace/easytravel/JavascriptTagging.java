package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class JavascriptTagging extends AbstractGenericPlugin {

    @Override
    public String doExecute(String location, Object... context) {
        return  "window.tagViaADK = true;\n";
    }
} 
