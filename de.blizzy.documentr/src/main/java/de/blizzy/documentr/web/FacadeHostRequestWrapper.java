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
package de.blizzy.documentr.web;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import de.blizzy.documentr.DocumentrConstants;

public class FacadeHostRequestWrapper extends HttpServletRequestWrapper {
	private static final Integer DEFAULT_HTTP_PORT = Integer.valueOf(80);
	
	private String facadeHost;
	private Integer facadePort;

	FacadeHostRequestWrapper(HttpServletRequest request, String facadeHost, Integer facadePort) {
		super(request);

		this.facadeHost = facadeHost;
		this.facadePort = facadePort;
	}
	
	@Override
	public StringBuffer getRequestURL() {
		StringBuffer urlBuf = super.getRequestURL();
		String url = urlBuf.toString();
		String facadeUrl = buildFacadeUrl(url, facadeHost, facadePort);
		if (!facadeUrl.equals(url)) {
			return new StringBuffer(facadeUrl);
		} else {
			return urlBuf;
		}
	}
	
	public static String buildFacadeUrl(String url, String facadeHost, Integer facadePort) {
		if (facadePort == null) {
			facadePort = DEFAULT_HTTP_PORT;
		}

		if (StringUtils.isNotBlank(facadeHost) && (facadePort != null)) {
			try {
				return UriComponentsBuilder.fromHttpUrl(url)
					.host(facadeHost)
					.port(facadePort.intValue())
					.build()
					.encode(DocumentrConstants.ENCODING)
					.toUriString();
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} else {
			return url;
		}
	}
}
