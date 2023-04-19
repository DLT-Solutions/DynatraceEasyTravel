package com.dynatrace.easytravel;

import java.util.Arrays;
import java.util.Random;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

import ch.qos.logback.classic.Logger;

/**
 * Be sure to have Exception Aggregation enabled for all Java Agent Groups if you enable this plugin.
 * It hangs in all extension points and throws loads of exceptions in all tiers.
 *
 * @author cwat-pgrasboe
 */
public class ExceptionSpamming extends AbstractGenericPlugin  {

    private static Logger log = LoggerFactory.make();

    private static final Random rnd = new Random();

	private static String randomString() {
		final int MIN = 50;
		final int MAX = 100;
		char[] buf = new char[rnd.nextInt(MAX - MIN + 1) + MIN];
		for (int i = 0; i < buf.length; i++) {
			buf[i] =  (char) ('a' + rnd.nextInt('z' - 'a' + 1));
		}
		return new String(buf);
	}

	private static String msg(boolean random, int i) {
		return random ? "spam message: " + randomString() : "spam message #" + i;
	}


	@Override
	public Object doExecute(String location, Object... context) {
		final boolean ALSO_DO_LOG_SPAMMING = false;
	    final int MAX_MESSAGES = 50;
	    final int MAX_EXCEPTIONS = 50;
	    final int NORMAL_EXCEPTION_CHANCE = 70; // % chance of an normal exception being thrown
	    final int ANONYMOUS_EXCEPTION_CHANCE = 30; // % chance of an anonymous exception being thrown
	    final int RANDOM_MSG_CHANCE = 5; // % chance of a random message

		int messages = rnd.nextInt(MAX_MESSAGES);
		int exceptions = rnd.nextInt(MAX_EXCEPTIONS);
		int normalThrows = rnd.nextInt((int) (4 * (100.0 / NORMAL_EXCEPTION_CHANCE)));
		int anonymousThrows = rnd.nextInt((int) (4 * (100.0 / ANONYMOUS_EXCEPTION_CHANCE)));
		boolean randomMessage = rnd.nextInt(100) < RANDOM_MSG_CHANCE; // 10% chance of a random message

		if (ALSO_DO_LOG_SPAMMING) {
			String message = "-------------- LogSpamming: Had extension point: " + location
					+ ", context: " + Arrays.toString(context)
					+ ", messages: " + messages
					+ ", exceptions: " + exceptions
					+ ", normalThrows: " + normalThrows
					+ ", anonymousThrows: " + anonymousThrows
					+ ", randomMessage: " + randomMessage;
			switch (rnd.nextInt(3)) {
				case 0: log.warn(message); break;
				case 1: log.error(message);  break;
				default: log.info(message);
			}
		}

		for (int i = 0; i < messages; i++) {

			/* anonymous exceptions - test aggregation explosion */

			switch (anonymousThrows) {
				case 0: SpamException.throw100(msg(randomMessage, i));        break;
				case 1: SpamRuntimeException.throw100(msg(randomMessage, i)); break;
				case 2: SpamError.throw100(msg(randomMessage, i));            break;
				case 3: SpamThrowable.throw100(msg(randomMessage, i));        break;
			}

			/* normal exceptions */

			for (int j = 0; j < exceptions; j++) {
				switch (normalThrows) {
					case 0: try { throw new SpamException(msg(randomMessage, i)); }        catch (Throwable t) { /* ignore */ } break; // NOSONAR - on purpose here to report all problems
					case 1: try { throw new SpamRuntimeException(msg(randomMessage, i)); } catch (Throwable t) { /* ignore */ } break; // NOSONAR - on purpose here to report all problems
					case 2: try { throw new SpamError(msg(randomMessage, i)); }            catch (Throwable t) { /* ignore */ } break; // NOSONAR - on purpose here to report all problems
					case 3: try { throw new SpamThrowable(msg(randomMessage, i)); }        catch (Throwable t) { /* ignore */ } break; // NOSONAR - on purpose here to report all problems
				}
			}
		}

		return null;
	}
}
