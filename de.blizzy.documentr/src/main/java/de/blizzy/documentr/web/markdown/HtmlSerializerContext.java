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
package de.blizzy.documentr.web.markdown;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.Util;
import de.blizzy.documentr.web.markdown.macro.MacroInvocation;

public class HtmlSerializerContext {
	private String projectName;
	private String branchName;
	private String pagePath;
	private MarkdownProcessor markdownProcessor;
	private List<Header> headers = new ArrayList<Header>();
	private List<MacroInvocation> macroInvocations = new ArrayList<MacroInvocation>();

	public HtmlSerializerContext(String projectName, String branchName, String pagePath,
			MarkdownProcessor markdownProcessor) {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		// pagePath can be null for new pages
		Assert.notNull(markdownProcessor);
		
		this.projectName = projectName;
		this.branchName = branchName;
		this.pagePath = pagePath;
		this.markdownProcessor = markdownProcessor;
	}

	public String getProjectName() {
		return projectName;
	}
	
	public String getBranchName() {
		return branchName;
	}
	
	public String getPagePath() {
		return pagePath;
	}
	
	public String getAttachmentURI(String name) {
		if (StringUtils.isNotBlank(pagePath)) {
			try {
				String pattern = "/attachment/{projectName}/{branchName}/{pagePath}/{name}"; //$NON-NLS-1$
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
						.path(pattern).build()
						.expand(projectName, branchName, Util.toURLPagePath(pagePath), name)
						.encode(DocumentrConstants.ENCODING).toUriString()
						.replaceFirst("^http(?:s)?://[^/]+(/.*)$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
				return uri;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return "#"; //$NON-NLS-1$
	}
	
	public String getPageURI(String path) {
		try {
			String pattern = "/page/{projectName}/{branchName}/{pagePath}"; //$NON-NLS-1$
			String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path(pattern).build()
					.expand(projectName, branchName, Util.toURLPagePath(path))
					.encode(DocumentrConstants.ENCODING).toUriString()
					.replaceFirst("^http(?:s)?://[^/]+(/.*)$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
			return uri;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String markdownToHTML(String markdown) {
		return markdownProcessor.markdownToHTML(markdown, projectName, branchName, pagePath);
	}

	void addHeader(String text, int level) {
		headers.add(new Header(text, level));
	}
	
	public List<Header> getHeaders() {
		return Collections.unmodifiableList(headers);
	}

	public MacroInvocation addMacroInvocation(String macroName, String params) {
		MacroInvocation invocation = markdownProcessor.getMacroInvocation(macroName, params, this);
		macroInvocations.add(invocation);
		return invocation;
	}
	
	List<MacroInvocation> getMacroInvocations() {
		return macroInvocations;
	}
}
