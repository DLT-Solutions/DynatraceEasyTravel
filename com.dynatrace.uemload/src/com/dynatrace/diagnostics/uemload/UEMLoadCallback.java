package com.dynatrace.diagnostics.uemload;

import java.io.IOException;


public interface UEMLoadCallback {

    public void run() throws IOException;
    
}
