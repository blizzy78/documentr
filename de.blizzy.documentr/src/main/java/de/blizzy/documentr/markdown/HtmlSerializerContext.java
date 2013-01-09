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
package de.blizzy.documentr.markdown;

import java.io.UnsupportedEncodingException;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.system.SystemSettingsStore;
import de.blizzy.documentr.util.Util;
import de.blizzy.documentr.web.util.FacadeHostRequestWrapper;

public class HtmlSerializerContext {
	@Getter
	private String projectName;
	@Getter
	private String branchName;
	@Getter
	private String pagePath;
	private MarkdownProcessor markdownProcessor;
	private List<Header> headers = Lists.newArrayList();
	@Getter(AccessLevel.PACKAGE)
	private List<MacroInvocation> macroInvocations = Lists.newArrayList();
	@Getter
	private Authentication authentication;
	private SystemSettingsStore systemSettingsStore;
	@Getter
	private IPageStore pageStore;
	private String contextPath;

	public HtmlSerializerContext(String projectName, String branchName, String pagePath,
			MarkdownProcessor markdownProcessor, Authentication authentication, IPageStore pageStore,
			SystemSettingsStore systemSettingsStore, String contextPath) {

		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		// pagePath can be null for new pages
		Assert.notNull(markdownProcessor);
		Assert.notNull(authentication);
		Assert.notNull(pageStore);
		Assert.notNull(systemSettingsStore);

		this.projectName = projectName;
		this.branchName = branchName;
		this.pagePath = pagePath;
		this.markdownProcessor = markdownProcessor;
		this.authentication = authentication;
		this.pageStore = pageStore;
		this.systemSettingsStore = systemSettingsStore;
		this.contextPath = contextPath;
	}

	public String getAttachmentUri(String name) {
		if (StringUtils.isNotBlank(pagePath)) {
			try {
				String pattern = "/attachment/{projectName}/{branchName}/{pagePath}/{name}"; //$NON-NLS-1$
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
						.path(pattern).build()
						.expand(projectName, branchName, Util.toUrlPagePath(pagePath), name)
						.encode(Charsets.UTF_8.name()).toUriString()
						.replaceFirst("^http(?:s)?://[^/]+(/.*)$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
				return uri;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (IllegalStateException e) {
				// ignore: not in web request context
			}
		}
		return "#"; //$NON-NLS-1$
	}

	public String getPageUri(String path) {
		try {
			String pattern = "/page/{projectName}/{branchName}/{pagePath}"; //$NON-NLS-1$
			String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path(pattern).build()
					.expand(projectName, branchName, Util.toUrlPagePath(path))
					.encode(Charsets.UTF_8.name()).toUriString()
					.replaceFirst("^http(?:s)?://[^/]+(/.*)$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
			return uri;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String getUrl(String uri) {
		String documentrHost = systemSettingsStore.getSetting(SystemSettingsStore.DOCUMENTR_HOST);
		return FacadeHostRequestWrapper.buildFacadeUrl(uri, contextPath, documentrHost);
	}

	public String markdownToHtml(String markdown) {
		return markdownProcessor.markdownToHtml(markdown, projectName, branchName, pagePath, authentication, contextPath);
	}

	void addHeader(String text, int level) {
		headers.add(new Header(text, level));
	}

	public List<Header> getHeaders() {
		return ImmutableList.copyOf(headers);
	}

	public MacroInvocation addMacroInvocation(String macroName, String params) {
		MacroInvocation invocation = new MacroInvocation(macroName, params);
		macroInvocations.add(invocation);
		return invocation;
	}
}
