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

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.blizzy.documentr.Util;

public class HtmlSerializerContext {
	private String projectName;
	private String branchName;
	private String pagePath;

	public HtmlSerializerContext(String projectName, String branchName, String pagePath) {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		// pagePath can be null for new pages
		
		this.projectName = projectName;
		this.branchName = branchName;
		this.pagePath = pagePath;
	}
	
	String getAttachmentURI(String name) {
		if (StringUtils.isNotBlank(pagePath)) {
			try {
				String pattern = "/attachment/{projectName}/{branchName}/{pagePath}/{name}"; //$NON-NLS-1$
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(pattern).build()
					.expand(projectName, branchName, Util.toURLPagePath(pagePath), name)
					.encode("UTF-8").toUriString(); //$NON-NLS-1$
				return uri;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return "#"; //$NON-NLS-1$
	}
}
