package com.dynatrace.easytravel.util;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;
import org.mvel2.MVEL;
import org.mvel2.PropertyAccessException;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Utility collection for MVEL property getting/setting.
 * Only class that needs MVEL for compile.
 *
 * @author philipp.grasboeck
 */
@SuppressWarnings("rawtypes")
public class MvelUtils {

	private static final Logger log = LoggerFactory.make();

    static {
        // date handling
        addConversion(Date.class, new DateConversionHandler());

        // Arrays handlers Java primitive types
        addArrayConversion(int[].class);
        addArrayConversion(byte[].class);
        addArrayConversion(short[].class);
        addArrayConversion(long[].class);
        addArrayConversion(char[].class);
        addArrayConversion(boolean[].class);

        // Arrays handlers for wrappers of Java primitive types
        addArrayConversion(Integer[].class);
        addArrayConversion(Byte[].class);
        addArrayConversion(Short[].class);
        addArrayConversion(Long[].class);
        addArrayConversion(Character[].class);
        addArrayConversion(Boolean[].class);

        // Other arrays handlers
        addArrayConversion(String[].class);
        addArrayConversion(Date[].class);
    }

    private static void addConversion(Class type, ConversionHandler handler)
    {
        DataConversion.addConversionHandler(type, handler);
    }

    private static void addArrayConversion(Class arrayType)
    {
        addConversion(arrayType, new ArrayConversionHandler(arrayType.getComponentType()));
    }

    private static void addEnumConversion(Class enumType)
    {
        addConversion(enumType, new EnumConversionHandler(enumType));
    }

	public static Object getProperty(String property, Object root)
	{
		try
		{
			return MVEL.getProperty(property, root);
		}
		catch (RuntimeException e)
		{
			if (log.isDebugEnabled()) log.debug("can't get: " + property + ", for root: " + root);
			return null;
		}
	}

	/**
	 * Return the String-value of the given property. If it is an array,
	 * create the string-representation out of it.
	 *
	 * @param property The name of the property
	 * @param root The bean from where the property is read
	 *
	 * @return The string-value of the given property.
	 */
	public static String getPropertyString(String property, Object root) {
		Object obj = getProperty(property, root);
		if(obj == null) {
			return null;
		}

		if(obj.getClass().isArray()) {
			Object[] array = (Object[])obj;
			StringBuilder ret = new StringBuilder();
			for(Object o : array) {
				ret.append(o).append(",");
			}

			if(ret.length() > 0 && ret.charAt(ret.length()-1) == ',') {
				ret.setLength(ret.length() - 1);
			}

			return ret.toString();
		}

		return obj.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> Object evaluateStrict(String expression, Object root, Class<T> causeExceptionClass) throws T
	{
		try {
			return MVEL.eval(expression, root);
		} catch (PropertyAccessException e) {
			Throwable ex = e;
			while (!causeExceptionClass.isAssignableFrom(ex.getClass())) { // i.e. instanceof
				if (ex.getCause() == null) {
					ex.printStackTrace();
					throw new IllegalArgumentException(ex.getMessage());
				}
				ex = ex.getCause();
			}
			throw (T) ex;
		}
	}


	public static Object evaluate(String expression, Object root)
	{
		try
		{
			return MVEL.eval(expression, root);
		}
		catch (RuntimeException e)
		{
			if (log.isDebugEnabled()) log.debug("can't eval: " + expression + ", for root: " + root);
			return null;
		}
	}

	public static void injectProperties(Object target, Map<?, ?> propertyMap, String namespace)
	{
		for (Map.Entry<?, ?> entry : propertyMap.entrySet())
		{
			String propertyName = entry.getKey().toString();
			Object propertyValue = entry.getValue();
			boolean set = false;

			int i = propertyName.lastIndexOf('.');
			if (i == -1)
			{
				set = namespace == null;
			}
			else if (namespace != null)
			{
				String propertyNamespace = propertyName.substring(0, i);
				set = propertyNamespace.equals(namespace);
				if (set)
				{
					propertyName = propertyName.substring(i + 1);
				}
			}

			if (set)
			{
				try {
					MVEL.setProperty(target, propertyName, propertyValue);
				} catch (RuntimeException e) {
					if (log.isDebugEnabled()) log.debug("can't set: " + propertyName);
				}
			}
		}
	}

	/**
	 * Read the given list of properties from the given bean object, observe a namespace
	 * if given and return a map of all the properties that were found.
	 *
	 * @param root
	 * @param propertyNames
	 * @param namespace
	 *
	 * @return A map containing all the properties in the form of "namespace.propertyName"
	 * the value is converted to String, arrays are converted to their String-representation.
	 *
	 * @author dominik.stadler
	 */
	public static <T> Map<String, String> readProperties(T root, Set<String> propertyNames, String namespace) {
		Map<String, String> properties = new HashMap<String, String>();

		for(String propertyName : propertyNames) {
			final String property;

			// handle namespace
			if(namespace != null && propertyName.startsWith(namespace + ".")) {
				property = getPropertyString(propertyName.substring(namespace.length()+1), root);
				if(property != null) {
					properties.put(propertyName, property);
				}
			} else {
				property = getPropertyString(propertyName, root);
				if(property != null) {
					if(namespace != null && namespace.length() > 0) {
						properties.put(namespace + "." + propertyName, property);
					} else {
						properties.put(propertyName, property);
					}
				}
			}
		}

		return properties;
	}

    public static void registerEnumType(Class enumType)
    {
        if (enumType.isEnum())
        {
            addEnumConversion(enumType);
        }
        else
        {
            throw new IllegalArgumentException("Not an enum type: " + enumType);
        }
    }

    public static void registerArrayType(Class arrayType)
    {
        if (arrayType.isArray())
        {
            addArrayConversion(arrayType);
        }
        else
        {
            throw new IllegalArgumentException("Not an array type: " + arrayType);
        }
    }

    static class ArrayConversionHandler implements ConversionHandler
    {
        private final Class<?> componentType;

        public ArrayConversionHandler(Class<?> componentType)
        {
            super();
            this.componentType = componentType;
        }

        @Override
        public boolean canConvertFrom(Class cls)
        {
            return true;
        }

        @Override
    	public Object convertFrom(Object value)
        {
        	final String commaMarker = (char)0 + "";
            if (value instanceof String)
            {
                String[] array = {};
                String svalue = ((String) value).trim();
                if (svalue.length() > 0)
                {
                	svalue = svalue.replace(",,", commaMarker);
                    array = svalue.split(",");
                }
                Object result = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++)
                {
                    String in = array[i];
                    in = in.replace(commaMarker, ",");
                    in = in.trim();
                    Array.set(result, i, DataConversion.convert(in, componentType));
                }
                return result;
            }

            return null;
        }
    }

    static class DateConversionHandler implements ConversionHandler
    {
    	static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    	/*
    	 * Need a thread-local date formatter since SimpleDateFormatter is not designed to work with concurrent threads
    	 * and this converter is a shared static converter for all dates that are set with MvelUtils.
    	 */
    	static final ThreadLocal<DateFormat> DATE_FORMATTER = new ThreadLocal<DateFormat>() {
    		@Override
			protected DateFormat initialValue() {
    			return new SimpleDateFormat(DATE_FORMAT);
    		}
    	};

    	@Override
    	public boolean canConvertFrom(Class cls)
        {
            return true;
        }

        @Override
    	public Object convertFrom(Object value)
        {
        	if (value != null)
        	{
    			try {
    				return DATE_FORMATTER.get().parseObject(value.toString());
    			} catch (ParseException e) {
    			}
        	}
        	return null;
        }
    }

    static class EnumConversionHandler implements ConversionHandler
    {
        private final Class enumType;

        public EnumConversionHandler(Class enumType)
        {
            if (!enumType.isEnum())
            {
                throw new IllegalArgumentException("Not an enum type: " + enumType);
            }
            this.enumType = enumType;
        }

        @Override
    	public boolean canConvertFrom(Class cls)
        {
            return true;
        }

        @Override
    	@SuppressWarnings("unchecked")
    	public Object convertFrom(Object value)
        {
            return (value != null) ? Enum.valueOf(enumType, value.toString()) : null;
        }
    }

}
