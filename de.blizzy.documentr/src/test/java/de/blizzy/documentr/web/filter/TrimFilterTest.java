/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012-2013 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.blizzy.documentr.web.filter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Charsets;

import de.blizzy.documentr.AbstractDocumentrTest;

public class TrimFilterTest extends AbstractDocumentrTest {
	private static final String CONTENT_TYPE = "text/plain"; //$NON-NLS-1$
	private static final String TEXT = "  foo  \r\n"; //$NON-NLS-1$
	private static final String TRIMMED_TEXT = "foo\n"; //$NON-NLS-1$
	private static final byte[] TRIMMED_TEXT_DATA = TRIMMED_TEXT.getBytes(Charsets.UTF_8);

	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;

	@Test
	public void doFilterWithOutputStream() throws IOException, ServletException {
		doFilter(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				HttpServletResponse response = (HttpServletResponse) invocation.getArguments()[1];
				byte[] data = TEXT.getBytes(Charsets.UTF_8);
				response.setContentType(CONTENT_TYPE);
				response.setContentLength(data.length);
				response.getOutputStream().write(data);
				return null;
			}
		}, CONTENT_TYPE, TRIMMED_TEXT_DATA);
	}

	@Test
	public void doFilterWithWriter() throws IOException, ServletException {
		doFilter(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				HttpServletResponse response = (HttpServletResponse) invocation.getArguments()[1];
				byte[] data = TEXT.getBytes(Charsets.UTF_8);
				response.setContentLength(data.length);
				response.setContentType(CONTENT_TYPE);
				response.getWriter().print(TEXT);
				return null;
			}
		}, CONTENT_TYPE, TRIMMED_TEXT_DATA);
	}

	@Test
	public void doFilterWithOutputStreamButNonTrimmableContent() throws IOException, ServletException {
		final byte[] data = { 1, 2, 3 };
		doFilter(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				HttpServletResponse response = (HttpServletResponse) invocation.getArguments()[1];
				response.setContentType("image/png"); //$NON-NLS-1$
				response.setContentLength(data.length);
				for (byte b : data) {
					response.getOutputStream().write(b);
				}
				return null;
			}
		}, "image/png", data); //$NON-NLS-1$
	}

	@Test
	public void doFilterWithWriterButNonTrimmableContent() throws IOException, ServletException {
		doFilter(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				HttpServletResponse response = (HttpServletResponse) invocation.getArguments()[1];
				response.setContentType("image/png"); //$NON-NLS-1$
				response.setContentLength(TEXT.getBytes(Charsets.UTF_8).length);
				response.getWriter().print(TEXT.substring(0, 1));
				response.getWriter().print(TEXT.substring(1));
				return null;
			}
		}, "image/png", TEXT.getBytes(Charsets.UTF_8)); //$NON-NLS-1$
	}

	@Test
	public void doFilterWithUnknownContentType() throws IOException, ServletException {
		final byte[] data = { 1, 2, 3 };
		doFilter(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				HttpServletResponse response = (HttpServletResponse) invocation.getArguments()[1];
				response.setContentLength(data.length);
				response.getOutputStream().write(data);
				return null;
			}
		}, null, data);
	}

	private void doFilter(Answer<Void> doFilterAnswer, String contentType, byte[] expectedData) throws IOException, ServletException {
		when(response.getCharacterEncoding()).thenReturn(Charsets.UTF_8.name());
		when(response.getContentType()).thenReturn(contentType);

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		ServletOutputStream servletOut = new ServletOutputStream() {
			@Override
			public void write(int b) {
				out.write(b);
			}
		};
		when(response.getOutputStream()).thenReturn(servletOut);

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(servletOut, Charsets.UTF_8.name()));
		when(response.getWriter()).thenReturn(writer);

		doAnswer(doFilterAnswer).when(filterChain).doFilter(Matchers.<ServletRequest>any(), Matchers.<ServletResponse>any());

		new TrimFilter().doFilter(request, response, filterChain);

		verify(response).setContentLength(expectedData.length);
		writer.flush();
		assertTrue(Arrays.equals(expectedData, out.toByteArray()));
	}
}
