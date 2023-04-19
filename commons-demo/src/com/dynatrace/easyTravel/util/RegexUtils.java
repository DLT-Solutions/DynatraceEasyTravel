package com.dynatrace.easytravel.util;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex Utility Collection
 * Should be unified with easyTravel version (currently equal).
 *
 * @author philipp.grasboeck
 */
public class RegexUtils {

    /**
     * Maximum depth of recursive replacements, i.e. patterns evaluated repeatedly.
     */
    private static final int MAX_DEPTH = 15;

	/**
     * Scans a text for regex and performs some actions on the occurences
     * specified by a visitor.
     *
     * <code>
	 *  RegexUtils.dynamicScan(text, Pattern.compile(regex), new ScanVisitor() {
	 *		@Override
	 *		public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
	 *			return replacement;
	 *		}
	 *	});
     * </code>
     * will behave exactly like
     * <code>
     * input.replaceAll(regex, Matcher.quoteReplacement(replacement))
     * </code>
     *
     * Note: java.util.regex.Matcher.quoteReplacement() will instruct the Matcher to treat
     * capturing-groups replacements like $1, $2 like literals, which this class does, too.
     * The MatchResult argument of ScanVisitor.visit() provides access to groups.
     * Note also: in case of recurring patterns, behavior will be different (recursive replace).
     *
     * @param text       the text to scan in. In case of replacement mode: must be a StringBuilder.
     * @param pattern    the compiled regex pattern.
     * @param visitor    called for every occurrence, may be null.
     * @return           visitor != null: number of actual replacements,
     *                   i.e. number of provided replacements not equal to the respective matching group.
     *                   visitor == null: number of occurrences, i.e. counts matcher.find() yielding true.
     *                   infinite recursion: -1 i.e. MAX_DEPTH has been exceeded.
     *
     * @author philipp.grasboeck
     */
    public static int dynamicScan(CharSequence text, Pattern pattern, ScanVisitor visitor) {
    	return scan(text, pattern, visitor);
	}

    /**
     * Like dynamicScan, but capable of repeating (recursive) patterns
     * that are replaced immediately.
     *
     * @param text       the text to scan in. In case of replacement mode: must be a StringBuilder.
     * @param pattern    the compiled regex pattern.
     * @param visitor    called for every occurrence, may be null.
     * @return           visitor != null: number of actual replacements,
     *                   i.e. number of provided replacements not equal to the respective matching group.
     *                   visitor == null: number of occurences, i.e. counts matcher.find() yielding true.
     *                   infinite recursion: -1 i.e. MAX_DEPTH has been exceeded.
     *
     * @author philipp.grasboeck
     */
    public static int recursiveScan(CharSequence text, Pattern pattern, ScanVisitor visitor) {
    	return scan(text, pattern, visitor);
    }

    /**
     * Actual regex scan implementation.
     *
     * Algorithm notes:
     * - need an offset since the matcher operates on a toString() copy of the object
     * - infinite recursion is detected by a depth counter that must non exceed MAX_DEPTH.
     * - unlike the old algorithm type of recursiveScan, that used to reset the matcher for each occurence
     *   immediately, this code performs all replacements at a time, looks at the whole text again,
     *   and then resets the matcher, if desired.
     */
	private static int scan(CharSequence text, Pattern pattern, ScanVisitor visitor) {
	    Matcher matcher = pattern.matcher(text.toString()); // toString() important - capture current value
        StringBuilder buf = (text instanceof StringBuilder) ? (StringBuilder) text : null; // null of no changes desired
		int count = 0; // counts of occurences / replacements
        int depth = 0; // to avoid infinite recursion
        boolean repeat = matcher.find(); // whether to scan again

        while (repeat) {
            int offset = 0; // text offset in changing text
            int n = 0;      // number of occurences / replacements in current pass

			while (repeat) {
				int startIndex = matcher.start() - offset;
				int endIndex = matcher.end() - offset;
				if (visitor == null) {
					n++; // just count
				} else {
					CharSequence token = visitor.visit(matcher, depth, count + n, startIndex, endIndex);
					String group = matcher.group();
					if (token != null && !token.equals(group)) {
		    			n++; // count and replace
		    			if (buf != null) {
			                buf.replace(startIndex, endIndex, token.toString());
			    			offset += group.length() - token.length();
		    			}
					}
				}
				repeat = matcher.find();
			}

			if (n > 0) {
				count += n;
				if (buf != null) {
					repeat = matcher.reset(buf.toString()).find();
					if (repeat && ++depth > MAX_DEPTH) {
						return -1; // probable infinite recursion or too much depth
					}
				}
			}
        }

        return count;
	}

    /**
	 * Visitor pattern: visits one occurrence that matches the regex passed to
	 * dynamicScan() or recursiveScan().
	 *
	 * @author philipp.grasboeck
	 */
	public static interface ScanVisitor {

	    /**
         * Visit one occurrence.
	     *
	     * @param match        the MatchResult that can be queried for matching groups.
	     *                     The whole matching group (i.e. text returned by match.group())
	     *                     is equal to text.substring(startIndex, endIndex).
	     * @param depth        the current nesting depth, i.e. pass number.
	     * @param index        index of this occurrence, running from 0..count-1
	     * @param startIndex   start index of the matching group in the text.
	     *                     Note that this can be different from match.start(),
	     *                     since it changes the text in place.
	     * @param endIndex     end index  of the matching group in the text.
	     *                     Note that this can be different from end(),
	     *                     since it changes the text in place.
	     * @return             the replacement text, if replace is desired, or null otherwise.
	     *
	     * @author philipp.grasboeck
	     */
	    public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex);
	}
}
