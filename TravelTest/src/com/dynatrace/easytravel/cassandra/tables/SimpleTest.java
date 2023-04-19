package com.dynatrace.easytravel.cassandra.tables;

import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.io.BaseEncoding;

public class SimpleTest extends SimpleTestUtil {
    @Test
    public void convertHexBytes() {
        String chars = "4e6f4c6f636174696f6e73";
        byte[] array = BaseEncoding.base16().lowerCase().decode(chars);
        String s = new String(array);
        System.out.println(new String(array));
        String encoded = BaseEncoding.base16().lowerCase().encode(s.getBytes());
        System.out.println(encoded);
        assertEquals(chars, encoded);
    }

    @Test
    public void testStringToHex() {
        String s = "NoLogins";
        String encoded = BaseEncoding.base16().lowerCase().encode(s.getBytes());
        System.out.println(encoded);
    }

    @Test
    public void test() {
        Calendar calendar = new GregorianCalendar(2017, 11, 1);
        Date bb = calendar.getTime();
        System.out.println(bb);
        calendar.set(2018, 0, 31);
        Date end = calendar.getTime();
        System.out.println(end);
    }

    @Test
    public void testTextUtils() {
        System.out.println(TextUtils.merge("test {0,number,#}", System.currentTimeMillis()));
    }

    @Test
    public void testMessageFormat() {
        System.out.println(MessageFormat.format("test {0,number,#}", System.currentTimeMillis()));
    }
}
