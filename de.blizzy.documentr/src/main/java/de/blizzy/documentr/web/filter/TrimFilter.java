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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class TrimFilter implements Filter {
	private TrimWriter writer = new TrimWriter();
	
	@SuppressWarnings("nls")
	private static final String[] TRIMMABLE_CONTENT_TYPE_PREFIXES = {
		"text/",
		"application/html",
		"application/xml",
		"application/json"
	};
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
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
		
		byte[] data;
		if (isTrimmable(StringUtils.defaultString(trimResponse.getContentType()))) {
			String encoding = trimResponse.getCharacterEncoding();
			String text = new String(trimResponse.getData(), encoding);
			BufferedReader in = null;
			ByteArrayOutputStream out = null;
			try {
				in = new BufferedReader(new StringReader(text));
				out = new ByteArrayOutputStream();
				writer.write(text, out, encoding);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
			data = out.toByteArray();
		} else {
			data = trimResponse.getData();
		}
		response.setContentLength(data.length);
		response.getOutputStream().write(data);
	}

	private boolean isTrimmable(String contentType) {
		for (String prefix : TRIMMABLE_CONTENT_TYPE_PREFIXES) {
			if (contentType.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
}
