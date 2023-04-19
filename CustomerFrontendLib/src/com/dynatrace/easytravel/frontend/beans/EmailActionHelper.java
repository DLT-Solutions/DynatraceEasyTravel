/*****************************************************
  *  dynaTrace Diagnostics (c) dynaTrace software GmbH
  *
  * @file: EmailActionHelper.java
  * @date: 07.05.2010
  * @author: markus.poechtrager
  *
  */
package com.dynatrace.easytravel.frontend.beans;

// copied from package com.dynatrace.diagnostics.sdk.email
// copied only method isValidEmailAddress()
public class EmailActionHelper {
	/**
	 * Returns true if the email address is valid.
	 * Checks for the email to...
	 * - contain no space ' 'char
	 * - contain only one '@' char in the middle
	 * - contain at least one dot '.' char in the middle
	 * - contain a valid-top level domain consisting of only lower case letters.
	 *
	 * @param email
	 * @return
	 * @author philipp.grasboeck
	 */
	public static boolean isValidEmailAddress(CharSequence email) {
		int len = email.length();
		int atIndex = -1;
		int dotIndex = -1;
		boolean topLevelDomainValid = false;
		for (int i = 0; i < len; i++) {
			char ch = email.charAt(i);
			switch (ch) {
				case '@': {
					if (atIndex != -1) { // double at - invalid
						return false;
					}
					atIndex = i;
					break;
				}
				case '.': { // dot - check the top level domain
					if (dotIndex == i - 1) { // consecutive dots - invalid
						return false;
					}
					dotIndex = i;
					topLevelDomainValid = true;
					break;
				}
				case ' ': { // space - invalid
					return false;
				}
				default: {
					topLevelDomainValid &= ('a' <= ch && ch <= 'z');
					break;
				}
			}
		}

		return topLevelDomainValid && 0 < atIndex && atIndex < len - 1 && 0 < dotIndex && dotIndex < len - 1;
	}
}
