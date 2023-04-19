package com.dynatrace.easytravel.launcher.misc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Browser Support which in contrast to the default Eclipse Web Browser Support can open web pages on fileshares. </br>
 * (mainly based on DefaultWebBrowser class from Eclipse)
 *
 * Copied from jloadtrace-trunk!
 *
 * @author dominik.stadler
 */
public class DocumentStarter {
    private static final Logger logger = Logger.getLogger(DocumentStarter.class.getName());//NOPMD

    public void openFile(File file) {
    	try {
    		String fileString = file.toString();
    		// path containing special chars
    		if (checkForBadFileName(fileString)) {
    			openURL(file.getCanonicalPath());
    		}
    		// normal path including spaces
    		else {
    			openURL(file.toURI().toURL());
    		}
    	}
		catch (IOException e) {
			logger.log(Level.WARNING, "Exception occurred while opening file: " + file, e); //$NON-NLS-1$
		}
    }

    public void openURL(String url) {
    	/*// check if we are in a client test, then we do not actually open the file as it hinders tests
    	if(ClientTestEnvironment.isRunningAClientTestBundle()) {
    		logger.warning("Not opening URL " + url + " because we are in a client test");  //$NON-NLS-1$//$NON-NLS-2$
    		return;
    	}*/

    	String href = url;
    	if (href.startsWith(BaseConstants.Browser.STRING_FILE)) {
    		href = href.substring(5);
    		while (href.startsWith(BaseConstants.FSLASH)) {
    			href = href.substring(1);
    		}
    		href = BaseConstants.Browser.FILE_PROTOCOL + href;

    	}
    	final String localHref = href;
    	String platform = SWT.getPlatform();
    	if (BaseConstants.Browser.PLATFORM_WIN32.equals(platform)  || SystemUtils.IS_OS_WINDOWS) {
    		if (checkForBadFileName(url)) {
    			// is share or badname that can not be launched by start command
    			Program.launch(localHref);
    		}
    		else {
    			// is local
    			try {
    				// TODO@(stefan.moschinski): verify solution with Dominik Punz
					Runtime.getRuntime().exec((String[]) ArrayUtils.add(BaseConstants.Browser.CMC_C_START_ARRAY, localHref));
				}
				catch (Exception e) {
					//fallback
					Program.launch(localHref);
				}
    		}
    	}
    	else if(BaseConstants.Browser.PLATFORM_CARBON.equals(platform)) {
    		try {
    			Runtime.getRuntime().exec(BaseConstants.Browser.USR_BIN_OPEN + localHref);
    		}
    		catch(IOException e) {
    			logger.log(Level.WARNING, "Exception occurred while opening URL: " + localHref, e); //$NON-NLS-1$
    		}
    	} else  {
			String encodedLocalHref = urlEncodeForSpaces(localHref.toCharArray());
			try {
				Process p = openWebBrowser(encodedLocalHref);
				if(p != null) {
					try {
						int ret = p.waitFor();
						if(ret != 0) {
							String msg = "Non-succesful response from external application, this indicates that there is no platform support for opening files of this type. Tried to open '" + encodedLocalHref + BaseConstants.SQUOTE; //$NON-NLS-1$
							throw new IllegalArgumentException(msg);
						}
					} catch (InterruptedException e) {
    	    			logger.log(Level.WARNING, "Exception occurred while waiting for process to start, URL: " + encodedLocalHref, e); //$NON-NLS-1$
					}
				}
			} catch(IOException _ex) {
				String msg = TextUtils.merge("Exception occurred while opening URL: {0}. Exception: {1}", encodedLocalHref, _ex.getMessage()); //$NON-NLS-1$
    			logger.log(Level.WARNING, msg);
				throw new IllegalArgumentException(msg, _ex);
			}
		}
    }


	private boolean checkForBadFileName(String url) {
		boolean bad = false;
		if (url.startsWith(BaseConstants.DBSLASH)) {
			bad = true;
		}
		else if (url.contains(BaseConstants.AMP)) {
			bad = true;
		}
		else if (url.contains(BaseConstants.LSBRA)) {
			bad = true;
		}
		else if (url.contains(BaseConstants.RSBRA)) {
			bad = true;
		}
		else if (url.contains(BaseConstants.LRBRA)) {
			bad = true;
		}
		else if (url.contains(BaseConstants.RRBRA)) {
			bad = true;
		}
		else if (url.contains(BaseConstants.PLUS)) {
			bad = true;
		}
		else if (url.contains(BaseConstants.FTICK)) {
			bad = true;
		}
		else if (url.contains(BaseConstants.BTICK)) {
			bad = true;
		}



		return bad;
	}

    public void openURL(URL url) {
        String href = url.toString();
        openURL(href);
    }

    /*@Override
	public boolean close() {
        return super.close();
    }*/

    private String urlEncodeForSpaces(char input[]) {
        StringBuffer retu = new StringBuffer(input.length);
        for (char element : input) {
			if(element == ' ') {
				retu.append(BaseConstants.FORMAT_STRING_PERCENT_20);
			}
			else {
				retu.append(element);
			}
		}

        return retu.toString();
    }

    private Process openWebBrowser(String href) throws IOException {
    	String webBrowser;
    	Process p = null;
        //if (webBrowser == null) {
        	for(String browser : BaseConstants.Browser.BROWSER_CHOICES) {
				try {
					logger.info("Trying to run command '" + browser + "' to open url: " + href); //$NON-NLS-1$ //$NON-NLS-2$
	                webBrowser = browser;
	                p = Runtime.getRuntime().exec(webBrowser + BaseConstants.WS + href);

	                // if we got here without error, we have found a useable application
	                break;
	            }
	            catch(IOException _ex) {	// NOPOMD - empty catch-block on purpose
	                // ignore as we have more items to check
	            	//webBrowser = BaseConstants.BROWSER_MOZILLA;
	            }
        	}
		//}

        if (p == null) {
        	webBrowser = BaseConstants.Browser.BROWSER_FIREFOX;
            p = Runtime.getRuntime().exec(webBrowser + BaseConstants.WS + href);
        }
        return p;
    }
}
