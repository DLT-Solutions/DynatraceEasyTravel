package com.dynatrace.easytravel.config;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.easytravel.util.ConfigurationProvider;
import com.dynatrace.easytravel.util.ConfigurationProvider.DynamicProperty;

/**
 * Test ConfigurationProvider.replaceDynamicProperties()
 * This method was added to support the dynamic Agent Name Feature (with port appended).
 *
 * @author philipp.grasboeck
 */
public class ReplaceDynamicPropertiesTest {

	@Test
	public void testDynamicAgentName() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{port}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "5050";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_5050", dtAgentName);
	}

	@Test
	public void testDynamicAgentNameDifferentFormat() {
		String dtAgentName = "CustomerFrontend_easyTravel#{_port}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "5050";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_5050", dtAgentName);
	}

	@Test
	public void testDynamicAgentNameDifferentFormatEmpty() {
		String dtAgentName = "CustomerFrontend_easyTravel#{_port}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_", dtAgentName);
	}

	@Test
	public void testDynamicAgentNameDifferentFormatNull() {
		String dtAgentName = "CustomerFrontend_easyTravel#{_port}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return null;
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel", dtAgentName);
	}

	@Test
	public void testDynamicAgentNameDifferentFormat1() {
		String dtAgentName = "CustomerFrontend_easyTravel#{_XXX+port+YYY}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "5050";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_XXX+5050+YYY", dtAgentName);
	}

	@Test
	public void testDynamicAgentNameDifferentFormatEmpty1() {
		String dtAgentName = "CustomerFrontend_easyTravel#{_XXX+port+YYY}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_XXX++YYY", dtAgentName);
	}

	@Test
	public void testDynamicAgentNameDifferentFormatNull1() {
		String dtAgentName = "CustomerFrontend_easyTravel#{_XXX+port+YYY}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return null;
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel", dtAgentName);
	}

	@Test
	public void testDynamicAgentNameRandomPort() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{port}";
		final int port = new Random().nextInt(11000) + 1000;

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return String.valueOf(port);
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_" + port, dtAgentName);
	}

	@Test
	public void testSeveralDynamicProperties() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{port}_#{myproperty1}_#{myproperty2}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "5050";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "myproperty1";
			}
			@Override
			public Object value() {
				return "myvalue1";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "myproperty2";
			}
			@Override
			public Object value() {
				return "myvalue2";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_5050_myvalue1_myvalue2", dtAgentName);
	}

	@Test
	public void testMultipleDynamicProperties() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{port}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "5050";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "6060";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_5050", dtAgentName);
	}

	@Test
	public void testMissingDynamicProperties() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{port}_#{myproperty1}_#{foo}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "5050";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "myproperty1";
			}
			@Override
			public Object value() {
				return "myvalue1";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_5050_myvalue1_#{foo}", dtAgentName);
	}

	@Test
	public void testEmptyDynamicProperties() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{port}_#{myproperty1}_#{foo}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "5050";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "myproperty1";
			}
			@Override
			public Object value() {
				return "myvalue1";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "foo";
			}
			@Override
			public Object value() {
				return "";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_5050_myvalue1_", dtAgentName);
	}

	@Test
	public void testNullDynamicProperties() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{port}_#{myproperty1}_#{foo}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "5050";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "myproperty1";
			}
			@Override
			public Object value() {
				return "myvalue1";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "foo";
			}
			@Override
			public Object value() {
				return null;
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_5050_myvalue1_", dtAgentName);
	}

	@Test
	public void testIndirection1() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{port}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "#{(ValueForPort)}";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "ValueForPort";
			}
			@Override
			public Object value() {
				return "5050";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_(5050)", dtAgentName);
	}

	@Test
	public void testNesting1() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{prefix#{middle}postfix}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "middle";
			}
			@Override
			public Object value() {
				return "MIDDLE";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "prefixMIDDLEpostfix";
			}
			@Override
			public Object value() {
				return "Beekeeper";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_Beekeeper", dtAgentName);
	}
	@Test
	public void testNesting2() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{prefix#{middle}postfix}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "prefixMIDDLEpostfix";
			}
			@Override
			public Object value() {
				return "Beekeeper";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "middle";
			}
			@Override
			public Object value() {
				return "MIDDLE";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_Beekeeper", dtAgentName);
	}

	@Test
	public void testIndirection2() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{port}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "ValueForPort";
			}
			@Override
			public Object value() {
				return "5050";
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return "#{(ValueForPort)}";
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_(5050)", dtAgentName);
	}

	@Test
	public void testSubString1() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{portlet}_XXX_#{port}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return 500;
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "portlet";
			}
			@Override
			public Object value() {
				return 1000;
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_1000_XXX_500", dtAgentName);
	}

	@Test
	public void testSubString2() {
		String dtAgentName = "CustomerFrontend_easyTravel_#{portlet}_XXX_#{port}";

		dtAgentName = ConfigurationProvider.replaceDynamicProperties(dtAgentName, new DynamicProperty() {
			@Override
			public String name() {
				return "portlet";
			}
			@Override
			public Object value() {
				return 1000;
			}
		}, new DynamicProperty() {
			@Override
			public String name() {
				return "port";
			}
			@Override
			public Object value() {
				return 500;
			}
		});

		Assert.assertEquals("CustomerFrontend_easyTravel_1000_XXX_500", dtAgentName);
	}
}
