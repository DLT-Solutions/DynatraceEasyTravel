package com.dynatrace.easytravel.launcher.agent;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dynatrace.easytravel.utils.TestHelpers;


public class VersionTest {

    @Test
    public void testValid() {
        assertEquals("3.2.1", new Version("3.2.1").toString());
        assertEquals(3, new Version("3.2.1").getMajor());
        assertEquals(2, new Version("3.2.1").getMinor());
        assertEquals(1, new Version("3.2.1").getRevision());

        assertEquals("3.5.0", new Version("3.5.0").toString());
        assertEquals("1.2", new Version("1.2").toString());
        assertEquals("1", new Version("1").toString());

        // alternative separator
        assertEquals("3,5,0", new Version("3,5,0", ",").toString());

        // heading/trailing whitespace characters
        assertEquals("3.5.0", new Version(" 3.5.0").toString());
        assertEquals("3.5.0", new Version("3.5.0 ").toString());
        assertEquals("3.5.0", new Version(" 3.5.0 ").toString());
        assertEquals("3.5.0" , new Version(" 3 . 5 . 0 ").toString());

        // everything after "revision" is ignored
        assertEquals("3.5.0", new Version(" 3.5.0.a ").toString());
        assertEquals("3.5.0", new Version(" 3.5.0.aasdfjasldkfasd").toString());
        assertEquals("3.5.0", new Version(" 3.5.0.123.aasdfjasldkfasd").toString());
        assertEquals("3.5.0", new Version(" 3.5.0.999aasdfjasldkfasd").toString());
    }

    @Test
    public void testInvalid() {
        try {
            new Version(null);
            fail("Null argument not allowed.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new Version("");
            fail("Empty argument not allowed.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new Version(" ");
            fail("Whitespace argument not allowed.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new Version("a.b.c");
            fail("Only numbers with separators allowed.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new Version(".3.5");
            fail("Invalid version format.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new Version("3.-5.1");
            fail("Invalid version format.");
        } catch (IllegalArgumentException e) {
        }

        try {
            new Version("-3.5.1");
            fail("Invalid version format.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testCompareTo() {
        // reflexive check
        assertTrue(new Version("3.5.0").compareTo(new Version("3.5.0")) == 0);
        assertTrue(new Version("3.5").compareTo(new Version("3.5")) == 0);
        assertTrue(new Version("3").compareTo(new Version("3")) == 0);

        // symmetry check
        assertTrue(new Version("3.5.0").compareTo(new Version("3.5.1")) < 0);
        assertTrue(new Version("3.5.1").compareTo(new Version("3.5.0")) > 0);

        assertTrue(new Version("3.5.0").compareTo(new Version("3.6.0")) < 0);
        assertTrue(new Version("3.6.0").compareTo(new Version("3.5.0")) > 0);

        assertTrue(new Version("3.5").compareTo(new Version("3.6")) < 0);
        assertTrue(new Version("3.6").compareTo(new Version("3.5")) > 0);

        assertTrue(new Version("3").compareTo(new Version("3.1")) < 0);
        assertTrue(new Version("3.1").compareTo(new Version("3")) > 0);

        // transitiv check
        assertTrue(new Version("3.2.1").compareTo(new Version("3.2.2")) < 0);
        assertTrue(new Version("3.2.2").compareTo(new Version("3.2.3")) < 0);
        assertTrue(new Version("3.2.1").compareTo(new Version("3.2.3")) < 0);

        assertTrue(new Version("3").compareTo(new Version("3.2")) < 0);
        assertTrue(new Version("3.2").compareTo(new Version("3.2.3")) < 0);
        assertTrue(new Version("3").compareTo(new Version("3.2.3")) < 0);

        // ensure no lexicographical comparison is used
        assertTrue(new Version("13").compareTo(new Version("2")) > 0);
        assertTrue(new Version("13.1").compareTo(new Version("2.3")) > 0);

        // check null arguments
        assertTrue(new Version("1.2.3").compareTo(null) > 0);

        // check different number of version parts (handle "1.2" like "1.2.0")
        assertTrue(new Version("1.2.3").compareTo(new Version("1.1")) > 0);
        assertTrue(new Version("1.2.3").compareTo(new Version("1.2")) > 0);
        assertTrue(new Version("1.2.3").compareTo(new Version("2.3")) < 0);
        assertTrue(new Version("1.1").compareTo(new Version("1.2.3")) < 0);
        assertTrue(new Version("1.2").compareTo(new Version("1.2.3")) < 0);
        assertTrue(new Version("1.3").compareTo(new Version("1.2.3")) > 0);

        assertTrue(new Version("1.2").compareTo(new Version("1")) > 0);
        assertTrue(new Version("1.2.3").compareTo(new Version("1")) > 0);
        assertTrue(new Version("1.2").compareTo(new Version("2")) < 0);
        assertTrue(new Version("1.2.3").compareTo(new Version("2")) < 0);
    }

    @Test
    public void testToString() {
    	TestHelpers.ToStringTest(new Version("1.2.3"));
    }
}
