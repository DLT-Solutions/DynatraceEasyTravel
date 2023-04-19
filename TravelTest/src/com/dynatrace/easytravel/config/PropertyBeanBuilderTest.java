package com.dynatrace.easytravel.config;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Test;

import com.dynatrace.easytravel.util.ConfigurationProvider;

public class PropertyBeanBuilderTest {

    private static Logger log = Logger.getLogger(PropertyBeanBuilderTest.class.getName());

    @Test
	public void testPropertyBeanBuilder() throws Exception {
		{
			Map<String, String> p = new HashMap<String, String>();
			p.put("intField", "123");
			p.put("textField", "abc");
			p.put("dateField", "2010-01-01 16:30:15");
			p.put("test.intField", "5");
			p.put("intArray", "1,2 , 3");

			MyBean bean = ConfigurationProvider.createPropertyBean(MyBean.class, p, null);
			log.info("myBean: " + bean);
		}
		{
			Map<String, String> p = new HashMap<String, String>();
			p.put("mybean.intField", "123");
			p.put("mybean.textField", "abc");
			p.put("mybean.dateField", "2010-01-01 16:30:15");
			p.put("dateField", "2010-01-01 16:30:15");

			MyBean bean = ConfigurationProvider.createPropertyBean(MyBean.class, p, "mybean");
			log.info("myBean: " + bean);
		}
	}

	public static class MyBean
	{
		private int intField;

		private String textField;

		private Date dateField;

		public int[] intArray;

		public int getIntField() {
			return intField;
		}

		public void setIntField(int intField) {
			this.intField = intField;
		}

		public String getTextField() {
			return textField;
		}

		public void setTextField(String textField) {
			this.textField = textField;
		}


		public Date getDateField() {
			return dateField;
		}

		public void setDateField(Date dateField) {
			this.dateField = dateField;
		}

		@Override
		public String toString() {
			return "MyBean [intField=" + intField + ", textField=" + textField
					+ ", dateField=" + dateField + ", intArray=" + Arrays.toString(intArray) + "]";
		}
	}
}
