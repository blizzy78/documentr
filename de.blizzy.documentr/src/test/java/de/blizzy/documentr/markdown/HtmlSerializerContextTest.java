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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.system.SystemSettingsStore;
import de.blizzy.documentr.util.Util;

public class HtmlSerializerContextTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = DocumentrConstants.HOME_PAGE_NAME + "/foo"; //$NON-NLS-1$
	private static final String CONTEXT = "/context"; //$NON-NLS-1$
	private static final String DOCUMENTR_HOST = "http://www.example.com:1234"; //$NON-NLS-1$

	@Mock
	private MarkdownProcessor markdownProcessor;
	@Mock
	private Authentication authentication;
	@Mock
	private HttpServletRequest request;
	@Mock
	private IPageStore pageStore;
	@Mock
	private SystemSettingsStore systemSettingsStore;
	private HtmlSerializerContext htmlSerializerContext;

	@Before
	public void setUp() {
		htmlSerializerContext = new HtmlSerializerContext(PROJECT, BRANCH, PAGE, markdownProcessor, authentication,
				pageStore, systemSettingsStore, CONTEXT);

		when(request.getServerName()).thenReturn("www.example.com"); //$NON-NLS-1$
		when(request.getScheme()).thenReturn("http"); //$NON-NLS-1$
		when(request.getContextPath()).thenReturn(CONTEXT);

		when(systemSettingsStore.getSetting(SystemSettingsStore.DOCUMENTR_HOST)).thenReturn(DOCUMENTR_HOST);

		ServletRequestAttributes attrs = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(attrs);
	}

	@After
	public void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void getAttachmentUri() {
		String uri = htmlSerializerContext.getAttachmentUri("test.png"); //$NON-NLS-1$
		assertEquals(CONTEXT + "/attachment/" + PROJECT + "/" + BRANCH + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Util.toUrlPagePath(PAGE) + "/test.png", //$NON-NLS-1$
				uri);
	}

	@Test
	public void getPageUri() {
		String page = "foo/bar/baz"; //$NON-NLS-1$
		String uri = htmlSerializerContext.getPageUri(page);
		assertEquals(CONTEXT + "/page/" + PROJECT + "/" + BRANCH + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Util.toUrlPagePath(page),
				uri);
	}

	@Test
	public void getUrl() {
		String result = htmlSerializerContext.getUrl(CONTEXT + "/foo/bar"); //$NON-NLS-1$
		assertEquals(DOCUMENTR_HOST + "/foo/bar", result); //$NON-NLS-1$
	}

	@Test
	public void markdownToHtml() {
		when(markdownProcessor.markdownToHtml("md", PROJECT, BRANCH, PAGE, authentication, CONTEXT)).thenReturn("html"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("html", htmlSerializerContext.markdownToHtml("md")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void addAndGetHeaders() {
		Header[] headers = {
				new Header("foo", 1), //$NON-NLS-1$
				new Header("bar", 2), //$NON-NLS-1$
				new Header("baz", 3) //$NON-NLS-1$
		};
		for (Header header : headers) {
			htmlSerializerContext.addHeader(header.getText(), header.getLevel());
		}

		List<Header> result = htmlSerializerContext.getHeaders();
		for (int i = 0; i < result.size(); i++) {
			assertTrue(EqualsBuilder.reflectionEquals(result.get(i), headers[i], true));
		}
	}

	@Test
	public void addAndGetMacroInvocations() {
		MacroInvocation invocation = htmlSerializerContext.addMacroInvocation("foo", "params"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("foo", invocation.getMacroName()); //$NON-NLS-1$
		assertEquals("params", invocation.getParameters()); //$NON-NLS-1$

		List<MacroInvocation> invocations = htmlSerializerContext.getMacroInvocations();
		assertEquals(1, invocations.size());
		invocation = invocations.get(0);
		assertEquals("foo", invocation.getMacroName()); //$NON-NLS-1$
		assertEquals("params", invocation.getParameters()); //$NON-NLS-1$
	}
}
