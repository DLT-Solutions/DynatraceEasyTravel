package com.dynatrace.easytravel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.cache.PaymentService;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPlugin;

/**
 * Simulates a slow remote payment service.
 */
public class SlowIoDummyPaymentService extends AbstractPlugin implements PaymentService {

	private static final Logger log = LoggerFactory.make();

	private static final int ACCEPT_DELAY = 10000; // ms
	private static final int RESPOND_DELAY = 50000; // ms

	@Override
	public String callPaymentService(String bookingId, String creditCard, String user, double amount, String location, String tenant) throws IOException {
		String result = OTHER_ERROR;

		Socket socket = null;
		try {
			// We use sockets because Java piped streams would not be considered I/O
			ServerSocket server = new ServerSocket(0); // obtains random unused port
			new Thread(new RemotePaymentService(server)).start();

			socket = new Socket();
			socket.connect(server.getLocalSocketAddress());

			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			output.writeUTF(creditCard);
			output.writeDouble(amount);

			DataInputStream input = new DataInputStream(socket.getInputStream());
			result = input.readUTF();
		} catch (IOException e) {
			log.warn("Exception in slow I/O dummy payment application", e);
		} finally {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}

		return result;
	}

	private static class RemotePaymentService implements Runnable {

		private final ServerSocket server;

		public RemotePaymentService(ServerSocket server) {
			this.server = server;
		}

		@Override
		public void run() {
			Object waitObj = new Object();
			try {
				synchronized (waitObj) {
					waitObj.wait(ACCEPT_DELAY); // Object.wait() looks nicer than Thread.sleep()
				}
			} catch (InterruptedException e) {
			}

			Socket socket = null;
			try {
				socket = server.accept();

				DataInputStream input = new DataInputStream(socket.getInputStream());
				/*String creditCard =*/ input.readUTF();
				/*double amount =*/ input.readDouble();

				try {
					synchronized (waitObj) {
						waitObj.wait(RESPOND_DELAY);
					}
				} catch (InterruptedException e) {
				}

				DataOutputStream output = new DataOutputStream(socket.getOutputStream());
				output.writeUTF(PAYMENT_ACCEPTED);
			} catch (IOException e) {
				log.warn("Exception in slow I/O dummy payment application remote thread", e);
			} finally {
				try {
					socket.close();
				} catch (Exception e) {
				}
				try {
					server.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
