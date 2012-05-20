/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

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

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TrimFilterTest {
	private static final String CONTENT_TYPE = "text/plain"; //$NON-NLS-1$
	private static final String TEXT = "  foo  \r\n"; //$NON-NLS-1$
	private static final String TRIMMED_TEXT = "foo\n"; //$NON-NLS-1$
	private static final byte[] TRIMMED_TEXT_DATA;
	
	static {
		try {
			TRIMMED_TEXT_DATA = TRIMMED_TEXT.getBytes("UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void doFilterWithOutputStream() throws IOException, ServletException {
		doFilter(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				HttpServletResponse response = (HttpServletResponse) invocation.getArguments()[1];
				byte[] data = TEXT.getBytes("UTF-8"); //$NON-NLS-1$
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
				byte[] data = TEXT.getBytes("UTF-8");  //$NON-NLS-1$
				response.setContentLength(data.length);
				response.getWriter().print(TEXT);
				return null;
			}
		}, CONTENT_TYPE, TRIMMED_TEXT_DATA);
	}
	
	@Test
	public void doFilterWithNonTrimmableContent() throws IOException, ServletException {
		final byte[] data = { 1, 2, 3 };
		doFilter(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				HttpServletResponse response = (HttpServletResponse) invocation.getArguments()[1];
				response.setContentLength(data.length);
				response.getOutputStream().write(data);
				return null;
			}
		}, "image/png", data); //$NON-NLS-1$
	}
	
	private void doFilter(Answer<Void> doFilterAnswer, String contentType, byte[] expectedData) throws IOException, ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain filterChain = mock(FilterChain.class);

		when(response.getCharacterEncoding()).thenReturn("UTF-8"); //$NON-NLS-1$
		when(response.getContentType()).thenReturn(contentType);

		ServletOutputStream out = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(out);
		
		doAnswer(doFilterAnswer).when(filterChain).doFilter(Matchers.<ServletRequest>any(), Matchers.<ServletResponse>any());
		
		new TrimFilter().doFilter(request, response, filterChain);
		
		verify(response).setContentLength(expectedData.length);
		verify(out).write(expectedData);
	}
}
