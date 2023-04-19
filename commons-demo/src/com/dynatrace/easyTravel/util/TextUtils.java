package com.dynatrace.easytravel.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;


/**
 * Utility class for message concatenation.
 *
 * @author martin.wurzinger
 */
public class TextUtils {

	private static final Logger LOGGER = Logger.getLogger(TextUtils.class.getName());

	private static Random random = new Random(System.currentTimeMillis());

	/**
	 * Replaces placeholders in given string with given values.
	 *
	 * @param pattern the string to be merged with the given values, e.g.
	 *        "This is version {0} in component {1}". For more details on the expected format see <a
	 *        href="http://java.sun.com/javase/6/docs/api/java/text/MessageFormat.html"
	 *        >MessageFormat Class Description</a>.
	 * @param values e.g. Object[]{"3.0.0", "dynatrace server"}
	 * @return the merged string or "&lt;unknown&gt;" in case of invalid argument
	 * @author jakob.zwirchmayr, martin.huch, martin.wurzinger
	 */
	public static String merge(String pattern, Object... values) {
		MessageFormat messageFormat = new MessageFormat(pattern);

		// check if format string and values fit
		if (values.length != messageFormat.getFormats().length) {
			IllegalArgumentException exception =
                new IllegalArgumentException("Unexpected number of parameters at string merge. Had " + values.length + " parameters, but expected " + messageFormat.getFormats().length + ", text: " + pattern);
            LOGGER.log(Level.WARNING, "Be sure to use ''{0}'' if a single quotes should be displayed around the merged argument!", exception);
			return "<unknown>";
		}

		// check for null values or empty string
		int index = 0;
		for (Object value : values) {
			if (value == null) {
				LOGGER.warning("Parameter " + index + " was null at string merge: " + pattern);
				return "<unknown>";
			}
			index++;
		}
		// use default JDK string merging
		try {
			return messageFormat.format(values);
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, "IllegalArgumentException at string merge: " + pattern, e);
		}

		return "<unknown>";
	}

	/**
	 *
	 * @param text the text to be cut if too long
	 * @param maxLength the maximum length the text should have
	 * @return the original text if it was not too long, a shortened text ending with "..." instead
	 *
	 * @author patrick.haruksteiner
	 */
	public static String getEndEllipsis(String text, int maxLength) {
		// sanity checks or if there is nothing to cut
		if (text == null || text.isEmpty() || text.length() <= maxLength || maxLength < 0) {
			return text;
		}

		// sanity check if the maxLength is very small
		if(maxLength < 3) {
			return BaseConstants.DOTDOTDOT.substring(0, maxLength);
		}

		// cut the text as required.
		return text.substring(0, maxLength - 3) + BaseConstants.DOTDOTDOT; // -3 for "..."
	}

	public static char randomChar() {
		return (char) (65 + random.nextInt(26));
	}

	/** Make sure the path ends with a '/' */
	public static String appendTrailingSlash(String path) {
		if(path == null) {
			return null;
		}
		return path.endsWith(BaseConstants.FSLASH) ? path : path.concat(BaseConstants.FSLASH);
	}

	/**
	 * Reads the contents from the BufferedReader and returns it as a string,
	 * where content size can be limited in terms of maxLines and maxChars, of which
	 * both may be 0, meaning no limit.
	 *
	 * @param in the reader to get characters from. the caller is responsible for closing the stream.
	 * @param maxLines maxLines i.e. the last N lines to return from the stream.
	 * @param maxChars maxChars i.e. if the string to be returned is longer than maxChars, lines
	 *        will be stripped from beginning until its size is less or equal maxChars.
	 * @return the content tail string, that is guaruanteed to have less than maxLines, if
	 * maxLines > 0, and to be less then maxChars long, if maxChars > 0.
	 * @throws IOException
	 * @author cwat-pgrasboe
	 */
	public static String readTail(BufferedReader in, int maxLines, int maxChars) throws IOException {
		return readTail(in, /*buf*/ null, maxLines, maxChars).toString();
	}

	/**
	 * Reads the contents from the BufferedReader and returns it as a string,
	 * where content size can be limited in terms of maxLines and maxChars, of which
	 * both may be 0, meaning no limit.
	 *
	 * This method is optimized for consecutive calls that should write to the same
	 * {@link StringBuilder} instance.
	 *
	 * @param in the reader to get characters from. the caller is responsible for closing the stream.
	 * @param buf the StringBuilder to write lines to, if <code>null</code>, a new StringBuilder is created.
	 * @param maxLines maxLines i.e. the last N lines to return from the stream.
	 * @param maxChars maxChars i.e. if the string to be returned is longer than maxChars, lines
	 *        will be stripped from beginning until its size is less or equal maxChars.
	 * @return buf, or, newly created StringBuilder, if buf was <code>null</code>.
	 * @throws IOException
	 * @author cwat-pgrasboe
	 */
	public static StringBuilder readTail(BufferedReader in, StringBuilder buf, int maxLines, int maxChars) throws IOException {
		final String LF = System.getProperty("line.separator");
		final int LFlen = LF.length();
		Deque<String> lines = new LinkedList<String>();
		int totalChars = 0; // calculate size before to buffer doesn't need to be resized
		int totalLines = 0;

		String line;
		while ((line = in.readLine()) != null) {
			lines.addLast(line);
			totalChars += line.length() + LFlen;
			totalLines++;

			// remove from beginning as long as limits are exceeded
			while (totalLines > 0 && (maxChars > 0 && totalChars > maxChars) || (maxLines > 0 && totalLines > maxLines)) {
				String first = lines.removeFirst();
				totalChars -= first.length() + LFlen;
				totalLines--;
			}
		}

		if (buf == null) {
			buf = new StringBuilder(totalChars); // create in a way to minmize array resize calls
		} else {
			buf.ensureCapacity(buf.capacity() + totalChars); // expand in a way to minmize array resize calls
		}

		for (String lineToAdd : lines) {
			buf.append(lineToAdd).append(LF);
		}
		return buf;
	}
}
