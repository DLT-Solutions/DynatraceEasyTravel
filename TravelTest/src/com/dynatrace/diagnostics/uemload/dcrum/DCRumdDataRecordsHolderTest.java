package com.dynatrace.diagnostics.uemload.dcrum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;



public class DCRumdDataRecordsHolderTest {

	DCRumdDataRecordsHolder emulator = new DCRumdDataRecordsHolder(10000, 20);

	@Test
	public void setValuesWorks() throws ExecutionException {

		String testRecordType = RandomStringUtils.randomAscii(10);
		String testPath = RandomStringUtils.randomAscii(10);
		String testUser = RandomStringUtils.randomAscii(10);
		String testHost = RandomStringUtils.randomAscii(10);

		emulator.addEntry(new DCRumDataRecord(testRecordType).setPath(testPath).setCliName(testUser).setHost(testHost));
		Map<Integer, DCRumDataRecord> cache = emulator.getRecordsMap();
		assertEquals(1, cache.size());
		DCRumDataRecord dcRumEntry = cache.get(0);

		assertEquals(testRecordType, dcRumEntry.getRecordType());
		assertEquals(testPath, dcRumEntry.getPath());
		assertEquals(testUser, dcRumEntry.getCliName());
		assertEquals(testHost, dcRumEntry.getHost());
	}

	@Test
	public void cleanUpWorks() throws ExecutionException {
		int ten = 10;
		List<DCRumDataRecord> created = new ArrayList<DCRumDataRecord>();
		for (int i = 0; i < ten; i++) {
			created.add(emulator.addEntry(createRandomDataRecord(emulator)));
		}

		Map<Integer, DCRumDataRecord> records = emulator.getRecordsMap();
		assertEquals(ten, records.size());

		emulator.cleanUpIfNecessary(); // should have no influence
		assertEquals(ten, records.size());

		emulator.setCleanUpProperties(10, 3);
		emulator.cleanUpIfNecessary(); // should clean (the oldest 3) entries
		assertEquals(ten - 3, records.size());

		// cleanup cleared the first three entries
		for (int i = 0; i < 3; i++) {
			assertFalse(records.containsValue(created.get(i)));
		}
	}

	@Test
	public void testAddEntryRaisesCleanUp() {
		emulator.setCleanUpProperties(10, 3);
		int ten = 10;
		for (int i = 0; i < ten; i++) {
			emulator.addEntry(createRandomDataRecord(emulator));
			assertEquals(i + 1, emulator.getRecordsMap().size());
		}

		// this entry should raise cleanup:
		emulator.addEntry(createRandomDataRecord(emulator));
		assertEquals(ten - 2, emulator.getRecordsMap().size()); // -3 old entries +1 new entry
	}


	private DCRumDataRecord createRandomDataRecord(DCRumdDataRecordsHolder emulator) {
		String testRecordType = RandomStringUtils.randomAscii(10);
		String testPath = RandomStringUtils.randomAscii(10);
		String testUser = RandomStringUtils.randomAscii(10);
		String testHost = RandomStringUtils.randomAscii(10);
		return new DCRumDataRecord(testRecordType).setPath(testPath).setCliName(testUser).setHost(testHost);
	}

}
