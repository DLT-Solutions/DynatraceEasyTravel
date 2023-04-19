package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.utils.ThreadTestHelper;



public class SyntheticTestHeadersGeneratorTest {
	private static final int NUMBER_OF_THREADS = 20;
	private static final int NUMBER_OF_TESTS = 5000;

	@Test
	public void testGetApplicationId() throws Exception {
		SyntheticTestHeadersGenerator gen = new SyntheticTestHeadersGenerator();
		assertNotNull(gen.getApplicationId());

		gen.next();
		assertNotNull(gen.getMonitorId());
		assertNotNull(gen.getStepId());

		for(int i = 0;i < 1000;i++) {
			gen.next();

			assertNotNull(gen.getMonitorId());
			assertNotNull(gen.getStepId());
		}
	}

	@Ignore("This class is currently only used locally, so it does not need to be mt-safe for now")
    @Test
    public void testMultipleThreads() throws Throwable {
		final SyntheticTestHeadersGenerator gen = new SyntheticTestHeadersGenerator();
		gen.next();

		ThreadTestHelper helper =
            new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

        helper.executeTest(new ThreadTestHelper.TestRunnable() {
            @Override
            public void doEnd(int threadnum) throws Exception {
                // do stuff at the end ...
            }

            @Override
            public void run(int threadnum, int iter) throws Exception {
            	if(threadnum == 0) {
            		gen.next();
            	} else {
	    			assertNotNull(gen.getMonitorId());
	    			assertNotNull(gen.getStepId());
            	}
            }
        });
    }
}
