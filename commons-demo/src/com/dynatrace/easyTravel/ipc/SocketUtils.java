package com.dynatrace.easytravel.ipc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;


public class SocketUtils {
	private static final Logger log = LoggerFactory.make();

    /**
     * Time in seconds of how long it can take for a process to
     * write the lock-file and finally open the port. If the file is
     * older than this and the port is still not used, then re-use the
     * port.
     */
    public static final int PORT_LOCK_FILE_TIMEOUT = 20;

    /**
     * Prefix for the lock-files that are generated.
     */
    private static final String PORT_LOCK_FILE_PREFIX = "port-";
	private static final String LOCAL_HOST_LOCK_FILE_PREFIX = "local-host-";

    /**
     * Reserve a port with a lock-file in the temporary directory.
     *
     * Note: You need to call {@link #freePort(int)} to release this
     * port again after usage. If the port is not used for {@value #PORT_LOCK_FILE_TIMEOUT}
     * seconds, it is re-used automatically.
     *
     * @param start
     * @param end
	 * @param hostname The hostname to bind to, this is typically either null or "localhost"
	 *
     * @return A port that can be used on this machine.
     *
     * @throws IOException If no available port is found
     *
     * @author dominik.stadler
     */
    public static final int reserveNextFreePort(int start, int end, String hostname) throws IOException {
    	// loop until we succeed in creating the file
		return getNextFreePortInternal(start, end, hostname, true);
    }

    /**
     * Release the port again after reserving it in {@link #reserveNextFreePort(int, int)}.
     *
     * @param port
     * @author dominik.stadler
     */
    public static final void freePort(int port) {
    	// remove the lock-file
		File file = new File(Directories.getExistingTempDir(), PORT_LOCK_FILE_PREFIX + port);

		// if the file exists and was created some time ago
		if(file.exists()) {
			if(!file.delete()) {
				log.warn("Could not remove port-lock-file: " + file);
			}
		}
    }

	/**
	 * Release the local host again after reserving it in {@link #re}.
	 *
	 * @param localHostIp local host ip to release
	 * @author cwat-smoschinsin
	 */
	public static final void freeLocalHostIp(String localHostIp) {
		// remove the lock-file
		File file = new File(Directories.getExistingTempDir(), LOCAL_HOST_LOCK_FILE_PREFIX + localHostIp);

		// if the file exists and was created some time ago
		if (file.exists()) {
			if (!file.delete()) {
				log.warn("Could not remove localhost-IP-lock-file: " + file);
			}
		}
	}

    /**
	 * Method that is used to find the next available port. It tries to get a port in the
	 * range of ports that are specified via start and end.
	 *
	 * @param start Start of range (including)
	 * @param end End of range (including)
	 * @param hostname The hostname to bind to, this is typically either null or "localhost"
	 *
	 * @return A port number that can be used.
	 *
	 * @throws IOException
	 *             If no available port is found.
	 */
	public static final int getNextFreePort(int start, int end, String hostname) throws IOException {
		return getNextFreePortInternal(start, end, hostname, false);
	}


	/**
	 *
	 * @return a host in the range from 127.0.0.1 to 127.0.0.9
	 * @throws IOException
	 * @author stefan.moschinski
	 */
	public static final String getNextFreeLocalHostIp(int port) throws IOException {
		for (int addressEnding = 1; addressEnding <= 9; addressEnding++) {
			String localIp = TextUtils.merge("127.0.0.{0}", addressEnding);
			if (!isPortAvailable(port, localIp)) {
				continue;
			}
			File file = new File(Directories.getExistingTempDir(), LOCAL_HOST_LOCK_FILE_PREFIX + localIp);

			// if the file exists and was created some time ago we can reuse it
			if (file.exists() &&
					(System.currentTimeMillis() - file.lastModified()) > TimeUnit.SECONDS.toMillis(PORT_LOCK_FILE_TIMEOUT)) {
				log.warn("Re-using localhost IP address " + localIp +
						" although there is a lock-file present because the creation time of the lockfile '" +
						new Date(file.lastModified()) + "' is longer ago than " + PORT_LOCK_FILE_TIMEOUT + " seconds. File: " + file);

				// here we have to remove the file so it is recreated below
				if (!file.delete()) {
					// somebody prevents us from recreating the file => try another port
					log.warn("Could not remove stale localhost-lock-file at " + file);
					continue;
				}
			}

			// if there is no host-lock-file yet, create one and use this host
			if (!file.exists()) {
				try {
					OutputStream str = new FileOutputStream(file);
					try {
						str.write("\n".getBytes()); //$NON-NLS-1$
					} finally {
						str.close();
					}
					return localIp;
				} catch (IOException e) {
					log.error("Could not create file: " + file + ": " + e.getMessage()); //$NON-NLS-1$
				}
			}
		}
		throw new IOException("No free host found for hostname in the range of [127.0.0.1 - 127.0.0.9]");
	}

	/**
	 * Checks if the port is available for binding a server socket.
	 *
	 * @param port
	 * @param hostname Null to use the default port, or a hostname or localhost.
	 *
	 * @return true if this port can be used to bind a server socket, false otherwise.
	 */
	public static final boolean isPortAvailable(int port, String hostname) {
		ServerSocket sock = null;
		try {
			sock = new ServerSocket();
			//sock.setReuseAddress(true);
			if(hostname == null || hostname.isEmpty()) {
				sock.bind(new InetSocketAddress(port));
			} else {
				sock.bind(new InetSocketAddress(hostname, port));
			}

			return true;
		} catch (IOException e) {
			// seems to be taken
			if(log.isDebugEnabled()) {
				log.debug("Port " + port + " on host " + hostname + " seems to be used already (" + e.getMessage() + ")");
			}

			// try next one
			return false;
		} finally {
			if(sock != null) {
				try {
					sock.close();
				} catch (IOException e) {
					log.warn("Closing ServerSocket failed", e);
				}
			}
		}
	}

	private static int getNextFreePortInternal(int start, int end, String hostname, boolean useLockFiles) throws IOException {
		for (int port = start; port <= end; port++) {
			// first test if this port is still available
			if(!isPortAvailable(port, hostname)) {
				continue;
			}

			if(!useLockFiles) {
				// we found a port that we can use
				return port;
			}

			// if the port is available, we can put a lockfile in place, this is used when multiple processes are using the ports
			// and we want to avoid timing issues when the processes need some time to actually open the port

			// create a "lock-file" in the common temporary directory
    		File file = new File(Directories.getExistingTempDir(), PORT_LOCK_FILE_PREFIX + port);

    		// if the file exists and was created some time ago we can reuse it
    		if(file.exists() &&
    				(System.currentTimeMillis() -file.lastModified()) > (PORT_LOCK_FILE_TIMEOUT * 1000) ) {
    			log.warn("Re-using port " + port + " although there is a lock-file present because the creation time of the lockfile '" +
    					new Date(file.lastModified()) + "' is longer ago than " + PORT_LOCK_FILE_TIMEOUT + " seconds. File: " + file);

    			// here we have to remove the file so it is recreated below
    			if(!file.delete()) {
    				// somebody prevents us from recreating the file => try another port
    				log.warn("Could not remove stale port-lock-file at " + file);
    				continue;
    			}
    		}

    		// if there is no port-lock-file yet, create one and use this port
			if(!file.exists()) {
				try {
					OutputStream str = new FileOutputStream(file);
					try {
						str.write("\n".getBytes()); //$NON-NLS-1$
					} finally {
						str.close();
					}

					// we found a port that we can use
					return port;
				} catch (IOException e) {
					// report the problem and look for the next port
					log.error("Could not create file: " + file + ": " + e.getMessage()); //$NON-NLS-1$
				}
    		}
		}

		throw new IOException("No free socket port found for hostname '" + hostname + "' in the range of [" + start + " - " + end + "]");
	}
}
