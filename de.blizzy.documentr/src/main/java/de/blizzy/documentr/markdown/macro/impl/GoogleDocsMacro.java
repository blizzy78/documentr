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
package de.blizzy.documentr.markdown.macro.impl;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Charsets;

import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.Macro;

@Macro(name="googledocs", insertText="{{googledoc [DOCUMENT]/}}")
public class GoogleDocsMacro implements IMacroRunnable {
	@Override
	public String getHtml(IMacroContext macroContext) {
		String macroParams = macroContext.getParameters();
		String googleUrl = StringUtils.substringBefore(macroParams, " ").trim(); //$NON-NLS-1$
		String width = StringUtils.substringAfter(macroParams, " ").trim(); //$NON-NLS-1$

		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(googleUrl).build();
		String path = uriComponents.getPath();
		MultiValueMap<String, String> params = uriComponents.getQueryParams();
		if (path.startsWith("/spreadsheet/")) { //$NON-NLS-1$
			String key = params.get("key").get(0); //$NON-NLS-1$
			UriComponents components = UriComponentsBuilder.fromHttpUrl("https://docs.google.com/spreadsheet/pub") //$NON-NLS-1$
				.queryParam("key", key) //$NON-NLS-1$
				.queryParam("output", "html") //$NON-NLS-1$ //$NON-NLS-2$
				.queryParam("widget", "true") //$NON-NLS-1$ //$NON-NLS-2$
				.build();
			return buildIframe(components);
		} else if (path.startsWith("/document/")) { //$NON-NLS-1$
			String id = params.get("id").get(0); //$NON-NLS-1$
			UriComponents components = UriComponentsBuilder.fromHttpUrl("https://docs.google.com/document/pub") //$NON-NLS-1$
				.queryParam("id", id) //$NON-NLS-1$
				.queryParam("embedded", "true") //$NON-NLS-1$ //$NON-NLS-2$
				.build();
			return buildIframe(components);
		} else if (path.startsWith("/presentation/")) { //$NON-NLS-1$
			String id = params.get("id").get(0); //$NON-NLS-1$
			UriComponents components = UriComponentsBuilder.fromHttpUrl("https://docs.google.com/presentation/embed") //$NON-NLS-1$
					.queryParam("id", id) //$NON-NLS-1$
					.queryParam("start", "false") //$NON-NLS-1$ //$NON-NLS-2$
					.queryParam("loop", "false") //$NON-NLS-1$ //$NON-NLS-2$
					.queryParam("delayms", String.valueOf(TimeUnit.MILLISECONDS.convert(3, TimeUnit.SECONDS))) //$NON-NLS-1$
					.build();
			return buildIframe(components);
		} else if (path.startsWith("/drawings/")) { //$NON-NLS-1$
			String id = params.get("id").get(0); //$NON-NLS-1$
			if (StringUtils.isBlank(width)) {
				width = "960"; //$NON-NLS-1$
			}
			UriComponents components = UriComponentsBuilder.fromHttpUrl("https://docs.google.com/drawings/pub") //$NON-NLS-1$
					.queryParam("id", id) //$NON-NLS-1$
					.queryParam("w", width) //$NON-NLS-1$
					.build();
			return buildImg(components);
		} else {
			return null;
		}
	}

	private String buildIframe(UriComponents components) {
		String url = toUrl(components);
		return "<iframe class=\"googledocs-document\" src=\"" + url + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
				"allowfullscreen=\"true\" mozallowfullscreen=\"true\" webkitallowfullscreen=\"true\"></iframe>"; //$NON-NLS-1$
	}

	private String buildImg(UriComponents components) {
		String url = toUrl(components);
		return "<img src=\"" + url + "\"/>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private String toUrl(UriComponents components) {
		try {
			return components.encode(Charsets.UTF_8.name()).toUriString();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String cleanupHtml(String html) {
		return null;
	}
}
