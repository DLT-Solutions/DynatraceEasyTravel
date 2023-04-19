package com.dynatrace.easytravel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

public abstract class JavascriptChangeDetection extends AbstractGenericPlugin {

	private final String JS_FILE_TO_CHANGE = "changedetectionlib.js";
	private final String WEBAPP_PATH = "webapp" + "/" + "problempatterns";
	private final String RELATIVE_JS_FILE_PATH = WEBAPP_PATH + "/"  + JS_FILE_TO_CHANGE;

	private volatile boolean jsSourceChanged = false;

    private static Logger LOGGER = LoggerFactory.make();

    @Override
    public synchronized String doExecute(String location, Object... context) {


    	if(location == PluginConstants.LIFECYCLE_PLUGIN_ENABLE && jsSourceChanged == false) {
    		LOGGER.info("Detection will be enabled.");
    		if(updateJavaScriptFile(getFileContent())) {
    			jsSourceChanged = true;
    		} else {
    			LOGGER.warn("File could not be changed - detection will not be recognized!");
    		}
    	}

    	if(location == PluginConstants.LIFECYCLE_PLUGIN_DISABLE && jsSourceChanged == true) {
        	LOGGER.info("ChangeDetection will be disabled");
        	if(resetOriginalFileState()) {
        		jsSourceChanged = false;
        	} else {
        		LOGGER.error("Could not reset js change detection file - State is inconsistent.");
        	}
    	}

        return null;
    }

    protected abstract String getFileContent();

    protected boolean resetOriginalFileState() {
    	deleteJsFile();
    	if(createJsFile() == false) {
	    	LOGGER.info("Could not restore emtpy js file");
	    	return false;
    	}

    	return true;
    }

    protected boolean updateJavaScriptFile(String fileContent)  {
    	FileWriter fileWriter = null;
    	try {
    		try {
		    	deleteJsFile();
		    	File newFile = new File(RELATIVE_JS_FILE_PATH);
		    	LOGGER.info("Try to write erroneous code to: " + newFile.getAbsolutePath());
		    	fileWriter = new FileWriter(newFile);
		    	fileWriter.write(fileContent);
		    	fileWriter.close();
		    	LOGGER.info("Changed js file successfully.");
    		} finally {
    			if(fileWriter != null)
    				fileWriter.close();
			}
    	} catch(IOException ex) {
    		ex.printStackTrace();
        	LOGGER.error("Could not update js file.");
        	return false;
    	}
    	return true;
    }


    private boolean createJsFile() {
    	File defaultFile = new File(RELATIVE_JS_FILE_PATH);
        OutputStream out;
		try {
			out = new FileOutputStream(defaultFile);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

    	return defaultFile.setLastModified(System.currentTimeMillis());
    }

    private boolean deleteJsFile() {
    	File fileToChange = new File(RELATIVE_JS_FILE_PATH);
    	return fileToChange.delete();
    }
}
