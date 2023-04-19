package com.dynatrace.diagnostics.uemload.http.callback;

import java.io.IOException;


public interface HttpReaderCallback {

    public void readDone(byte[] bytes) throws IOException;
    
}
