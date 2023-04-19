package com.dynatrace.easytravel.launcher.misc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dynatrace.easytravel.utils.ThreadTestHelper;


public class BlockingInputStreamTest {
	final BlockingInputStream stream = new BlockingInputStream();
	boolean done = false;
	Exception exception = null;

	@Test
	public void testBlockingInputStream() throws Exception {
		runBlockingTest(new ThreadTestHelper.TestRunnable() {
			
			@Override
			public void run(int threadnum, int iter) throws Exception {
				stream.read();
			}
			
			@Override
			public void doEnd(int threadnum) throws Exception {
			}
		}, false);
	}

	@Test
	public void testBlockingInputStream2() throws Exception {
		runBlockingTest(new ThreadTestHelper.TestRunnable() {
			
			@Override
			public void run(int threadnum, int iter) throws Exception {
				byte[] b = new byte[] {};
				stream.read(b);
			}
			
			@Override
			public void doEnd(int threadnum) throws Exception {
			}
		}, false);
	}

	@Test
	public void testBlockingInputStream3() throws Exception {
		runBlockingTest(new ThreadTestHelper.TestRunnable() {
			
			@Override
			public void run(int threadnum, int iter) throws Exception {
				byte[] b = new byte[] {};
				stream.read(b, 0, 0);
			}
			
			@Override
			public void doEnd(int threadnum) throws Exception {
			}
		}, false);
	}

	@Test
	public void testBlockingInputStreamClosedBefore() throws Exception {
		// close the stream before trying to block
		stream.close();
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					new ThreadTestHelper.TestRunnable() {
						
						@Override
						public void run(int threadnum, int iter) throws Exception {
							stream.read();
						}
						
						@Override
						public void doEnd(int threadnum) throws Exception {
						}
					}.run(0, 0);
				} catch (Exception e) {
					exception =e;
				}
				
				done = true;
			}
			
		};

		thread.start();

		Thread.sleep(1000);

		assertTrue("Should be set now that we unblocked the stream...", done);

		assertNull("Should not have an exception, but had: " + exception, exception);		

		// we expect the thread to terminate now
		thread.join();
	}
	
	@Test
	public void testBlockingInputStreamClosed() throws Exception {
		runBlockingTest(new ThreadTestHelper.TestRunnable() {
			
			@Override
			public void run(int threadnum, int iter) throws Exception {
				stream.read();
			}
			
			@Override
			public void doEnd(int threadnum) throws Exception {
			}
		}, true);
	}
	
	@Test
	public void testBlockingInputStreamInterrupted() throws Exception {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					new ThreadTestHelper.TestRunnable() {
						
						@Override
						public void run(int threadnum, int iter) throws Exception {
							stream.read();
						}
						
						@Override
						public void doEnd(int threadnum) throws Exception {
						}
					}.run(0, 0);
				} catch (Exception e) {
					exception =e;
				}
				
				done = true;
			}
			
		};

		thread.start();

		Thread.sleep(1000);

		// interrupt the blocking call
		thread.interrupt();
		
		Thread.sleep(1000);

		assertTrue("Should be set now that we unblocked the stream...", done);

		assertNull("Should not have an exception, but had: " + exception, exception);		

		// we expect the thread to terminate now
		thread.join();
	}
	
	private void runBlockingTest(final ThreadTestHelper.TestRunnable runnable, boolean close) throws Exception {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					runnable.run(0, 0);
				} catch (Exception e) {
					exception =e;
				}
				
				done = true;
			}
			
		};

		thread.start();

		Thread.sleep(1000);

		assertFalse("Should not be set until we unblock the stream...", done);

		// now unblock or close the stream to make it return in read
		if(close) {
			stream.close();
		} else {
			stream.unblock();
		}

		// we expect the thread to terminate now
		thread.join();

		assertTrue("Should be set now that we unblocked the stream...", done);

		assertNull("Should not have an exception, but had: " + exception, exception);
	}

}
