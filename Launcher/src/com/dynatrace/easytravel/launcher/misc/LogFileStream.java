package com.dynatrace.easytravel.launcher.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.dynatrace.easytravel.logging.BasicLoggerConfig;


public class LogFileStream extends OutputStream {

    private FileOutputStream currentFileOS;
    private long currentFileSize = 0;
    private File[] files;


    public LogFileStream(String logFilePath) {
        logFilePath = logFilePath.replace("%u", "0");
        int logFileCount = logFilePath.contains("%i") ? BasicLoggerConfig.DEFAULT_MAX_FILE_INDEX : 1;
        files = new File[logFileCount];
        for (int i = 0; i < logFileCount; i++) {
            files[i] = new File(logFilePath.replace("%i", String.valueOf(i)));
        }
        verifyFileOs(0);
    }


    @Override
    public void write(int b) throws IOException {
        verifyFileOs(Integer.SIZE / 8);
        currentFileOS.write(b);
    }


    @Override
    public void write(byte[] b) throws IOException {
        verifyFileOs(b.length);
        currentFileOS.write(b);
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        verifyFileOs(len);
        currentFileOS.write(b, off, len);
    }


    @Override
    public void flush() throws IOException {
        if (currentFileOS != null) {
            currentFileOS.flush();
        }
    }


    @Override
    public void close() throws IOException {
        if (currentFileOS != null) {
            currentFileOS.close();
        }
    }


    private void verifyFileOs(int bytes2append) {
        try {
            if (currentFileOS == null) {
                long currentSize = files[0].exists() ? files[0].length() : 0;
                currentFileOS = new FileOutputStream(files[0], true);
                currentFileSize = currentSize + bytes2append;
                return;
            }
            currentFileSize += bytes2append;
            if (currentFileSize < BasicLoggerConfig.DEFAULT_MAX_BYTES) {
                return;
            }
            currentFileOS.flush();
            currentFileOS.close();
            if (files.length == 1) {
                files[0].delete();
            } else {
                for (int i = files.length - 2; i >= 0; i--) {
                    File newer = files[i];
                    if (newer.exists()) {
                        File older = files[i + 1];
                        if (older.exists()) {
                            older.delete();
                        }
                        newer.renameTo(older);
                    }
                }
            }
            currentFileOS = new FileOutputStream(files[0], true);
            currentFileSize = 0;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
