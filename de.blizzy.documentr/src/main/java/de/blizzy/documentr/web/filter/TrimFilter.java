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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.Closeables;

public class TrimFilter implements Filter {
	private TrimWriter writer = new TrimWriter();

	@Override
	public void init(FilterConfig filterConfig) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletResponse resp = (HttpServletResponse) response;
		TrimResponseWrapper trimResponse = new TrimResponseWrapper(resp);
		chain.doFilter(request, trimResponse);

		Boolean trimmable = trimResponse.isTrimmable();
		if ((trimmable != null) && trimmable.booleanValue()) {
			byte[] data = getTrimmedData(trimResponse);
			response.setContentLength(data.length);
			response.getOutputStream().write(data);
		}
	}

	private byte[] getTrimmedData(TrimResponseWrapper trimResponse) throws IOException {
		BufferedReader in = null;
		ByteArrayOutputStream out = null;
		try {
			Charset charset = Charset.forName(trimResponse.getCharacterEncoding());
			String text = new String(trimResponse.getData(), charset);
			in = new BufferedReader(new StringReader(text));
			out = new ByteArrayOutputStream();
			writer.write(text, out, charset);
		} finally {
			Closeables.closeQuietly(in);
			Closeables.closeQuietly(out);
		}
		return out.toByteArray();
	}
}
