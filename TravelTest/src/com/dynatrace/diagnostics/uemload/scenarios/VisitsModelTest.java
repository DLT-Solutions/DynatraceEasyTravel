package com.dynatrace.diagnostics.uemload.scenarios;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Rafal.Psciuk
 *
 */
public class VisitsModelTest {

	@Test
	public void test() {
		VisitsModel model = new VisitsModel.VisitsBuilder().setDefaults()
				.setBounce(1)
				.setSearch(2)
				.setAlmost(3)
				.setConvert(4)
				.setB2b(5)
				.build();
		
		assertEquals(1, model.getBounce());
		assertEquals(2, model.getSearch());
		assertEquals(3, model.getAlmost());
		assertEquals(4, model.getConvert());
		assertEquals(5, model.getB2b());
		assertEquals(10, model.getSeo());
		assertEquals(3, model.getSpecialOffers());
		assertEquals(10, model.getImageGallery());
		assertEquals(10, model.getMagentoShort());				
	}
}
