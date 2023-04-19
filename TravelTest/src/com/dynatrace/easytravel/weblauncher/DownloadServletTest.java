package com.dynatrace.easytravel.weblauncher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.ssi.ByteArrayServletOutputStream;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.util.WebUtils;

public class DownloadServletTest {

	@Test
	public void testDownloadFile() throws Exception {
		HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
		HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);

		File thisFile = new File(TestUtil.detectTravelTestSrcDir(), TestUtil.getJavaFileName(getClass()));
		String thisPath = thisFile.getAbsolutePath();
		String thisName = thisFile.getName();

		EasyMock.expect(request.getParameter("filename")).andReturn(thisPath);

		response.setHeader("Content-Type", WebUtils.getContentType(thisPath));
		response.setHeader("Content-Disposition", "attachment; filename=" + thisName);

		ByteArrayServletOutputStream output = new ByteArrayServletOutputStream();
		EasyMock.expect(response.getOutputStream()).andReturn(output);

		EasyMock.replay(request, response);

		DownloadServlet servlet = new DownloadServlet();
		servlet.doService(request, response);

		InputStream stream = new FileInputStream(thisFile);
		try {
			Assert.assertTrue("Expecting to get DownloadServletTest.java", IOUtils.contentEquals(new ByteArrayInputStream(output.toByteArray()), stream));
		} finally {
			stream.close();
		}

		EasyMock.verify(request, response);
	}

	@Test
	public void testFileNotFound() throws Exception {
		HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
		HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);

		File thisFile = new File(TestUtil.detectTravelTestSrcDir(), "doesNotExist");
		String thisPath = thisFile.getAbsolutePath();

		EasyMock.expect(request.getParameter("filename")).andReturn(thisPath);

		response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + thisPath);

		EasyMock.replay(request, response);

		DownloadServlet servlet = new DownloadServlet();
		servlet.doService(request, response);

		EasyMock.verify(request, response);

	}
}
