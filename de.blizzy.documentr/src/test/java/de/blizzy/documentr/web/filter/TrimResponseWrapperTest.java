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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mock;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;

import de.blizzy.documentr.AbstractDocumentrTest;

public class TrimResponseWrapperTest extends AbstractDocumentrTest {
	@Mock
	private HttpServletResponse response;

	@Test
	public void setContentLengthMustBeIgnored() {
		TrimResponseWrapper wrapper = new TrimResponseWrapper(response);
		wrapper.setContentType("text/plain"); //$NON-NLS-1$
		wrapper.setContentLength(123);
		verify(response, never()).setContentLength(anyInt());
	}

	@Test
	public void getOutputStreamAndGetData() throws IOException {
		TrimResponseWrapper wrapper = new TrimResponseWrapper(response);
		wrapper.setContentType("text/plain"); //$NON-NLS-1$

		byte[] data = "hello \u20AC".getBytes(Charsets.UTF_8); //$NON-NLS-1$

		ServletOutputStream out = null;
		try {
			out = wrapper.getOutputStream();
			out.write(data);
		} finally {
			Closeables.closeQuietly(out);
		}

		assertTrue(Arrays.equals(data, wrapper.getData()));
	}

	@Test
	public void getOutputStreamAndGetDataButGetStreamFirst() throws IOException {
		TrimResponseWrapper wrapper = new TrimResponseWrapper(response);

		byte[] data = "hello \u20AC".getBytes(Charsets.UTF_8); //$NON-NLS-1$

		ServletOutputStream out = null;
		try {
			out = wrapper.getOutputStream();
			wrapper.setContentType("text/plain"); //$NON-NLS-1$
			out.write(data);
		} finally {
			Closeables.closeQuietly(out);
		}

		assertTrue(Arrays.equals(data, wrapper.getData()));
	}

	@Test
	public void getWriterAndGetData() throws IOException {
		when(response.getCharacterEncoding()).thenReturn(Charsets.UTF_8.name());

		TrimResponseWrapper wrapper = new TrimResponseWrapper(response);
		wrapper.setContentType("text/plain"); //$NON-NLS-1$

		String s = "hello \u20AC"; //$NON-NLS-1$

		PrintWriter out = null;
		try {
			out = wrapper.getWriter();
			out.write(s);
		} finally {
			Closeables.closeQuietly(out);
		}

		assertTrue(Arrays.equals(s.getBytes(Charsets.UTF_8), wrapper.getData()));
	}
}
