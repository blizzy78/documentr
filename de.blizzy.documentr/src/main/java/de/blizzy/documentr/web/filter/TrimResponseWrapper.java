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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

class TrimResponseWrapper extends HttpServletResponseWrapper {
	@SuppressWarnings("nls")
	private static final String[] TRIMMABLE_CONTENT_TYPE_PREFIXES = {
		"text/",
		"application/html",
		"application/xml",
		"application/json"
	};

	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private PrintWriter writer;
	private ServletOutputStream outputStream;
	private Boolean trimmable;
	private Integer contentLength;
	
	TrimResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if ((trimmable != null) && trimmable.booleanValue()) {
			if (outputStream == null) {
				outputStream = new ServletOutputStream() {
					@Override
					public void write(int b) {
						buffer.write(b);
					}
				};
			}
			return outputStream;
		}
		
		setContentLengthIfNecessary();
		return super.getOutputStream();
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		if ((trimmable != null) && trimmable.booleanValue()) {
			if (writer == null) {
				writer = new PrintWriter(new OutputStreamWriter(buffer, getCharacterEncoding()));
			}
			return writer;
		}
		
		setContentLengthIfNecessary();
		return super.getWriter();
	}
	
	@Override
	public void setContentLength(int len) {
		if ((trimmable == null) || trimmable.booleanValue()) {
			contentLength = Integer.valueOf(len);
			// don't send to superclass
		} else {
			super.setContentLength(len);
		}
	}
	
	@Override
	public void setContentType(String type) {
		super.setContentType(type);
		trimmable = Boolean.valueOf(isTrimmable(type));
	}
	
	byte[] getData() throws IOException {
		if ((trimmable != null) && !trimmable.booleanValue()) {
			throw new IllegalStateException("content is not trimmable"); //$NON-NLS-1$
		}
		
		if (writer != null) {
			writer.flush();
		}
		if (outputStream != null) {
			outputStream.flush();
		}
		return buffer.toByteArray();
	}

	Boolean isTrimmable() {
		return trimmable;
	}
	
	private boolean isTrimmable(String contentType) {
		for (String prefix : TRIMMABLE_CONTENT_TYPE_PREFIXES) {
			if (contentType.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
	
	private void setContentLengthIfNecessary() {
		if (contentLength != null) {
			super.setContentLength(contentLength.intValue());
			contentLength = null;
		}
	}
}
