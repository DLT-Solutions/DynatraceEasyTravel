package com.dynatrace.easytravel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.cache.PaymentService;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.spring.AbstractPlugin;

public class DotNetPaymentService extends AbstractPlugin implements PaymentService {

	private static Logger log = LoggerFactory.make();

	private static final int SOCKET_TIMEOUT = 60000;

	@Override
	public String callPaymentService(String bookingId, String creditCard, String user, double amount, String location,
			String tenant) throws IOException {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();

		StringBuilder buf = new StringBuilder(128);
		buf.append(CONFIG.dotNetBackendWebServiceBaseDir);
		buf.append("Payment/Pay?bookingId=").append(bookingId);
		buf.append("&user=").append(user).append("&ccNumber=").append(encrypt(creditCard));
		buf.append("&amount=").append(amount);
		String url = buf.toString();
		try {
			String response = UrlUtils.retrieveData(url, "UTF-8", SOCKET_TIMEOUT);

			if (log.isDebugEnabled()) {
				log.debug("Called .NET payment service, url=" + url + ", response=" + response);
			}

			return response;
		} catch (IOException e) {
			throw new IOException("While handling url: " + url, e);
		}
	}

	private String encrypt(String input) {
		try {
			SecretKey key = new SecretKeySpec(new byte[] { -113, 26, -60, 69, -53, -70, 122, -98 }, "DES");
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] utf8 = input.getBytes("UTF8");
			byte[] enc = cipher.doFinal(utf8);
			return URLEncoder.encode(Base64.encodeBase64String(enc), "UTF8");
		} catch (NoSuchPaddingException nspe) {
			log.error("Exception encrypting String", nspe);
		} catch (NoSuchAlgorithmException nsae) {
			log.error("Exception encrypting String", nsae);
		} catch (InvalidKeyException ike) {
			log.error("Exception encrypting String", ike);
		} catch (UnsupportedEncodingException uee) {
			log.error("Exception encrypting String", uee);
		} catch (IllegalBlockSizeException ibse) {
			log.error("Exception encrypting String", ibse);
		} catch (BadPaddingException bpe) {
			log.error("Exception encrypting String", bpe);
		}
		return input;
	}
}
