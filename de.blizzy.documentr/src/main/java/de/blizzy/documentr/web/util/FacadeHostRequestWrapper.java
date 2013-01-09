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
package de.blizzy.documentr.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class FacadeHostRequestWrapper extends HttpServletRequestWrapper {
	private String documentrHost;

	FacadeHostRequestWrapper(HttpServletRequest request, String documentrHost) {
		super(request);
		this.documentrHost = documentrHost;
	}

	@Override
	public StringBuffer getRequestURL() {
		StringBuffer urlBuf = super.getRequestURL();
		String url = urlBuf.toString();
		String contextPath = getContextPath();
		String facadeUrl = buildFacadeUrl(url, contextPath, documentrHost);
		if (!facadeUrl.equals(url)) {
			return new StringBuffer(facadeUrl);
		} else {
			return urlBuf;
		}
	}

	public static String buildFacadeUrl(String url, String contextPath, String documentrHost) {
		contextPath = StringUtils.defaultIfBlank(contextPath, StringUtils.EMPTY);
		if (contextPath.equals("/")) { //$NON-NLS-1$
			contextPath = StringUtils.EMPTY;
		}

		String newUrl;
		if (StringUtils.isNotBlank(contextPath)) {
			int pos = url.indexOf(contextPath);
			newUrl = documentrHost + url.substring(pos + contextPath.length());
		} else {
			UriComponentsBuilder builder;
			try {
				builder = UriComponentsBuilder.fromHttpUrl(url);
			} catch (IllegalArgumentException e) {
				builder = UriComponentsBuilder.fromUriString(url);
			}
			String path = StringUtils.defaultIfBlank(
					builder.build().getPath(), StringUtils.EMPTY);
			if (StringUtils.isNotBlank(path)) {
				int pos = StringUtils.lastIndexOf(url, path);
				newUrl = documentrHost + url.substring(pos);
			} else {
				newUrl = documentrHost;
			}
		}
		return newUrl;
	}
}
