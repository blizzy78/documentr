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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

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
	private OutputStream bufferOutputStream = buffer;
	private PrintWriter bufferWriter;
	private ServletOutputStream superOutputStream;
	private PrintWriter superWriter;
	private SwitchableServletOutputStream outputStream;
	private SwitchablePrintWriter writer;
	private Boolean trimmable;
	private Integer contentLength;

	TrimResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public ServletOutputStream getOutputStream() {
		if (outputStream == null) {
			outputStream = new SwitchableServletOutputStream() {
				@Override
				OutputStream getOutputStream() throws IOException {
					if ((trimmable != null) && trimmable.booleanValue()) {
						if (bufferOutputStream == null) {
							bufferOutputStream = buffer;
						}
						return bufferOutputStream;
					} else {
						setContentLengthIfNecessary();
						if (superOutputStream == null) {
							superOutputStream = getSuperOutputStream();
						}
						return superOutputStream;
					}
				}
			};
		}

		return outputStream;
	}

	private ServletOutputStream getSuperOutputStream() throws IOException {
		return super.getOutputStream();
	}

	@Override
	public PrintWriter getWriter() {
		if (writer == null) {
			writer = new SwitchablePrintWriter() {
				@Override
				Writer getWriter() throws IOException {
					if ((trimmable != null) && trimmable.booleanValue()) {
						if (bufferWriter == null) {
							bufferWriter = new PrintWriter(new OutputStreamWriter(buffer, getCharacterEncoding()));
						}
						return bufferWriter;
					} else {
						setContentLengthIfNecessary();
						if (superWriter == null) {
							superWriter = getSuperWriter();
						}
						return superWriter;
					}
				}
			};
		}

		return writer;
	}

	private PrintWriter getSuperWriter() throws IOException {
		return super.getWriter();
	}

	@Override
	public void setContentLength(int len) {
		if ((trimmable == null) || trimmable.booleanValue()) {
			contentLength = len;
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
			super.setContentLength(contentLength);
			contentLength = null;
		}
	}
}
