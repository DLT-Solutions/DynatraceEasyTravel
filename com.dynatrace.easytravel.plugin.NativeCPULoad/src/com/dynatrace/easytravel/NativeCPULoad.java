package com.dynatrace.easytravel;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

/**
 * This Plugin is able to launch a native child process which consumes 100% CPU time on purpose.
 *
 * @author cwat-rpilz
 *
 */
public final class NativeCPULoad extends AbstractGenericPlugin {

	protected static Logger log = LoggerFactory.make();

    // these are overwritten by setter usually!
    //private long time2wait = 20;

    private static final String EXIT_MESSAGE = "EXIT";
	private static final int SHUTDOWN_TIMEOUT_MS = 10000;

	private Process process = null;

	private static boolean simulate = false;

	private Config CONFIG = new Config();

	public static void main(String[] args) {
		if (args != null) {
			for (String arg : args) {
				if (arg == null) {
					continue;
				} else if (arg.equals("-s")) {
					simulate = true;
				} else {
					System.err.println("unrecognized option " + arg);
				}
			}
		}
		final NativeCPULoad nativeCPULoad = new NativeCPULoad();
		if (nativeCPULoad.enable()) {
			System.out.println("Press CTRL-C to shut down");
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					nativeCPULoad.disable();
				}
			});
		}
		synchronized(nativeCPULoad) {
			try {
				nativeCPULoad.wait();	// NOSONAR - I think this is used to wait endlessly here, or?
			} catch (InterruptedException e) {
				// no need to do anything special here
			}
		}
	}

    /*public void setTime2wait(long time2wait) {
        this.time2wait = time2wait;
    }*/

    /*
         * (non-Javadoc)
         * @see com.dynatrace.easytravel.spring.AbstractGenericPlugin#doExecute(java.lang.String, java.lang.Object[])
         */
	@Override
	public Object doExecute(String location, Object... context) {
		if (PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
			enable();
		} else if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location) || PluginConstants.LIFECYCLE_BACKEND_SHUTDOWN.equals(location)) {
			disable();
		} /* replaced by functionality inside BusinessBackend, see CPUHelper usage in JourneyService
		else if (PluginConstants.DATAACESS_INTERCEPT_QUERY.equals(location)) {
            doBusyWait(time2wait);
        }*/
		return null; //nothing to do
	}

	private static boolean recheckFileExists(File dest) {
		int loop = 0;
		while (loop < 10 && !dest.exists()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// ignore
			}
			loop++;
		}
		return dest.exists();
	}

    /*private void doBusyWait(long time2wait) {
       long startTime = System.currentTimeMillis();
       long endTime = startTime + time2wait;
       long delaycounter = Long.MIN_VALUE;
       while (System.currentTimeMillis() < endTime ) {
           delaycounter += 1;
           if (delaycounter >= Long.MAX_VALUE) {
               delaycounter = Long.MIN_VALUE;
           }
        }
     }*/

	/**
	 * Extracts a suitable binary from the plugin archive for the current operating system and architecture
	 * and launches it as a child process.<br />
	 * <br />
	 *
	 */
	private boolean enable() {
		synchronized (getClass()) {
			final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
			
			try {
				log.warn("Starting NativeCPULoad problem pattern with file <" + CONFIG.EXE_NAME + ">");

				File dest = new File(CONFIG.EXE_NAME);
				copyExecutable(dest);
				if (!recheckFileExists(dest)) {
					log.error("Executable file " + CONFIG.EXE_NAME + " does not exist on disc - aborting");
					return false;
				}
				
				//=======================================
				// Build the command
				//=======================================
				
				List<String> myCommand = new ArrayList<String>();
				
				if (Config.isLinux()) {
					myCommand.add(new StringBuilder().append(BaseConstants.DOT).append(BaseConstants.FSLASH).append(CONFIG.EXE_NAME).toString());
				} else {
					if (EASYTRAVEL_CONFIG.NativeCPULoadPri > 0) {
						// Under windows we can increase the process priority, but for the time being only by one.
						// Currently there is no need to increase process priority for Linux.
						myCommand.add("cmd"); // to use START we need to execute another command line processor
						myCommand.add("/C");	// means "execute the following command"
						myCommand.add("start");
						myCommand.add("/B"); // but do not open a new window
						myCommand.add("/ABOVENORMAL");
						myCommand.add("/WAIT"); // wait till it completes - the cmd will wait, not easyTravel
						myCommand.add(CONFIG.EXE_NAME);
					} else {
						myCommand.add(CONFIG.EXE_NAME);
					}
				}
			
				myCommand.add("-p"); myCommand.add(String.valueOf(CONFIG.PORT));
				
				if (simulate) {
					myCommand.add("-s");
				}
				
				if (EASYTRAVEL_CONFIG.NativeCPULoadActiveTime >=0) {
					myCommand.add("-a"); myCommand.add(String.valueOf(EASYTRAVEL_CONFIG.NativeCPULoadActiveTime));
				} else {
					// default: run active forever (default CPULoad behavior)
				}
				
				if (EASYTRAVEL_CONFIG.NativeCPULoadQuietTime > 0) {
					myCommand.add("-q"); myCommand.add(String.valueOf(EASYTRAVEL_CONFIG.NativeCPULoadQuietTime));
				} else {
					// default: zero
				}
				
				if (EASYTRAVEL_CONFIG.NativeCPULoadTotalTime >=0) {
					myCommand.add("-t"); myCommand.add(String.valueOf(EASYTRAVEL_CONFIG.NativeCPULoadTotalTime));
				} else {
					// default: run forever (default CPULoad behaviour)
				}
				
				//=======================================
				// Build and execute the process
				//=======================================
				
				ProcessBuilder processBuilder = new ProcessBuilder(myCommand);
				processBuilder.directory(new File(BaseConstants.DOT));
				dest.setExecutable(true);
				
				// Dump command content to diag
				myCommand = processBuilder.command();
				log.debug("CPULoad command components: START");
				for (String cmdPart:myCommand) {
					log.debug("<" + cmdPart + ">");
				}
				log.debug("CPULoad command components: END");
			
				process = processBuilder.start();
				
				// TBD
				// We could do work here to capture the stderr and stdout.
				// processBuilder.redirectErrorStream(true);
				// ...
				
				return true;
			} catch (IOException e) {
				log.warn("Error launching native cpu process", e);
				return false;
			}
		}
	}

	private void copyExecutable(File dest) throws IOException {
		URL urlBinary = getClass().getClassLoader().getResource(CONFIG.getExecutablePath());
		InputStream in = urlBinary.openStream();
		try {
			if (!dest.exists()) {
				dest.deleteOnExit();
				IO.copy(in, dest);
			}
		} finally {
			in.close();
		}
	}


	/**
	 * Contacts the native part of the plugin and signals it to shut down.<br />
	 * If after {@link #SHUTDOWN_TIMEOUT_MS} milliseconds the native process still has not shut down,
	 * it will be terminated ungracefully.
	 *
	 * @author cwat-rpilz
	 */
	private void disable() {
		synchronized (getClass()) {
			log.warn("Stopping NativeCPULoad problem pattern with exe name <" + CONFIG.EXE_NAME + ">");

			Thread t = new Thread("NativeCPULoad stopper") {
				/**
				 * Waiting for the native child process to shut down is being performed
				 * in an extra thread, which is getting handed over the plugin object itself
				 * as {@link Runnable}.<br />
				 * <br />
				 * This method either completes because the child process has indeed exited
				 * or because it is getting interrupted from the outside after
				 * {@link #SHUTDOWN_TIMEOUT_MS} milliseconds.
				 *
				 * @author cwat-rpilz
				 */
				@Override
				public void run() {
					try {
						if (process != null) {
							process.waitFor();
						}
					} catch (InterruptedException ie) {
						if (process != null) {
							process.destroy();
						}
						log.warn("native process forcibly destroyed");
					}
				}
			};
			t.start();
			sendShutdown();
            try {
				t.join(SHUTDOWN_TIMEOUT_MS);
				if(t.isAlive()) {
					log.warn("native process thread is still alive after " + SHUTDOWN_TIMEOUT_MS + "ms");
					//force termination of child process
					t.interrupt();
				} else {
					log.warn("native process has been shut down");
				}
			} catch (InterruptedException e) {
				log.warn("native process did not terminate gracefully");
				t.interrupt();
			} finally {
				File dest = new File(CONFIG.EXE_NAME);
				if(!dest.delete()) {
					log.warn("Could not remove file: '" + dest + "', still running?");
					
					try {
						// Make another effort to stop the process:
						// first gently and then increasingly forcibly.
						// The problem that has sometimes been observed is that the native process
						// appears to exit insofar as the waitFor() completes above, but the file is not removed
						// and the process is actually still running.
					
						// However, first simply wait a bit longer to see if the file has been removed after all.
						// Because of timing issues, the call to delete() (above) can fail, but the file
						// will eventually be removed, only a bit later.
						Thread.sleep(10000);
						if (dest.exists()) {
						
							final int MAX_TRIES = 10;	
							for (int i = 0; i <= MAX_TRIES; i++) {
						
								sendShutdown();
						
								// 10 seconds breathing space should be enough between re-tries
								// or to see the file removed.
								Thread.sleep(10000);
						
								if(dest.delete()) {
									log.warn("Try <" + i + ">: Succeeded removing file: '" + dest + "'");
									break; // for
								} else {
									log.warn("Try <" + i + ">: Could not remove file: '" + dest + "', still running?");
							
									if (i == MAX_TRIES) {
									
										process.destroy();
										Thread.sleep(10000);
								
										if(dest.delete()) {
											log.warn("Succeeded removing file: '" + dest + "' after explicit process destroy.");
										} else {
											log.warn("Could not remove file: '" + dest + "', even after explicit process destroy. Porcess still active?");
										}
									}	
								}
							}
						} else {
							log.warn("File no longer exists: '" + dest + "'");
						}
					} catch (InterruptedException e) {}
				} else {
					log.warn("Removed file: '" + dest + "'");
				}
			}
		}
	}

	/**
	 * Opens a socket connection to the native child process,
	 * sends the string EXIT and waits for it to be mirrored.<br />
	 * <br />
	 * The response is not getting validated - the child process is allowed to respond any string
	 *
	 * @author cwat-rpilz
	 */
	private void sendShutdown() {
    	Socket socket = null;
        try {
        	socket = new Socket(InetAddress.getLocalHost(), CONFIG.PORT);	// NOSONAR - we don't care too much about multi-home machines here
        	byte[] messageBuffer = EXIT_MESSAGE.getBytes(Charset.defaultCharset());

        	OutputStream out = socket.getOutputStream();
        	try {
	        	BufferedOutputStream bos = new BufferedOutputStream(out);
	        	try {
		        	bos.write(messageBuffer, 0, messageBuffer.length);
		        	InputStream in = socket.getInputStream();
		        	try {
		        		in.read(messageBuffer, 0, Math.min(in.available(), messageBuffer.length));
		        	} finally {
		        		in.close();
		        	}
	        	} finally {
	        		bos.close();
	        	}
        	} finally {
        		out.close();
        	}
        } catch (ConnectException e) {
        	log.warn("NativeCPULoad not found: " + e);
        } catch (IOException e) {
        	log.warn("Exception while trying to stop NativeCPULoad", e);
		} finally {
        	if (socket != null) {
        		try {
					socket.close();
				} catch (IOException e) {
					// ignore
				}
        	}
        }
	}

	@TestOnly
	public String getExecutableName() {
		return CONFIG.EXE_NAME;
	}

	@TestOnly
	public int getPort() {
		return CONFIG.PORT;
	}

}
