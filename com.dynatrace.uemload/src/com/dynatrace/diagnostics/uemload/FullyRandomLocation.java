package com.dynatrace.diagnostics.uemload;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FullyRandomLocation implements RandomLocation {
	private final Logger log = Logger.getLogger(FullyRandomLocation.class.getName());
	private static final Random r = new Random();

	@Override
	public Location get() {
		String ip;
		float chance = r.nextFloat();
		ip = r.nextInt(255) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
		
		if (chance <= 0.10f) {
			byte[] ipBytes = new byte[16];
			r.nextBytes(ipBytes);
			try {
				ip = Inet6Address.getByAddress(ipBytes).getHostAddress();
			} catch (UnknownHostException e) {
				log.log(Level.SEVERE, "There was an error generating IPv6 - will return IPv4", e);
			}
		}
		return new Location(null, null, ip, r.nextInt(24)-11);
	}
}
