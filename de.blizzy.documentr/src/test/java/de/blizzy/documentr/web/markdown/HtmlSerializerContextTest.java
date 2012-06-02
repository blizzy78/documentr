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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.blizzy.documentr.Util;
import de.blizzy.documentr.web.markdown.macro.MacroInvocation;

public class HtmlSerializerContextTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "home/foo"; //$NON-NLS-1$
	private static final String CONTEXT = "/context"; //$NON-NLS-1$
	
	private MarkdownProcessor markdownProcessor;
	private Authentication authentication;
	private HtmlSerializerContext htmlSerializerContext;
	private HttpServletRequest request;

	@Before
	public void setUp() {
		markdownProcessor = mock(MarkdownProcessor.class);
		authentication = mock(Authentication.class);
		htmlSerializerContext = new HtmlSerializerContext(PROJECT, BRANCH, PAGE, markdownProcessor, authentication);
		
		request = mock(HttpServletRequest.class);
		when(request.getServerName()).thenReturn("www.example.com"); //$NON-NLS-1$
		when(request.getScheme()).thenReturn("http"); //$NON-NLS-1$
		when(request.getContextPath()).thenReturn(CONTEXT);
		
		ServletRequestAttributes attrs = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(attrs);
	}
	
	@After
	public void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}
	
	@Test
	public void getProjectName() {
		assertEquals(PROJECT, htmlSerializerContext.getProjectName());
	}

	@Test
	public void getBranchName() {
		assertEquals(BRANCH, htmlSerializerContext.getBranchName());
	}
	
	@Test
	public void getPagePath() {
		assertEquals(PAGE, htmlSerializerContext.getPagePath());
	}
	
	@Test
	public void getAuthentication() {
		assertSame(authentication, htmlSerializerContext.getAuthentication());
	}
	
	@Test
	public void getAttachmentURI() {
		String uri = htmlSerializerContext.getAttachmentURI("test.png"); //$NON-NLS-1$
		assertEquals(CONTEXT + "/attachment/" + PROJECT + "/" + BRANCH + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Util.toURLPagePath(PAGE) + "/test.png", //$NON-NLS-1$
				uri);
	}

	@Test
	public void getPageURI() {
		String page = "foo/bar/baz"; //$NON-NLS-1$
		String uri = htmlSerializerContext.getPageURI(page);
		assertEquals(CONTEXT + "/page/" + PROJECT + "/" + BRANCH + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Util.toURLPagePath(page),
				uri);
	}
	
	@Test
	public void markdownToHTML() {
		when(markdownProcessor.markdownToHTML("md", PROJECT, BRANCH, PAGE, authentication)).thenReturn("html"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("html", htmlSerializerContext.markdownToHTML("md")); //$NON-NLS-1$ //$NON-NLS-2$
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
		MacroInvocation invocation1 = mock(MacroInvocation.class);
		when(markdownProcessor.getMacroInvocation("foo", "params", htmlSerializerContext)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(invocation1);
		MacroInvocation invocation2 = mock(MacroInvocation.class);
		when(markdownProcessor.getMacroInvocation("bar", null, htmlSerializerContext)) //$NON-NLS-1$
			.thenReturn(invocation2);
		MacroInvocation invocation3 = mock(MacroInvocation.class);
		when(markdownProcessor.getMacroInvocation("bar", "params2", htmlSerializerContext)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(invocation3);
		
		htmlSerializerContext.addMacroInvocation("foo", "params"); //$NON-NLS-1$ //$NON-NLS-2$
		htmlSerializerContext.addMacroInvocation("bar", null); //$NON-NLS-1$
		htmlSerializerContext.addMacroInvocation("bar", "params2"); //$NON-NLS-1$ //$NON-NLS-2$
		
		List<MacroInvocation> result = htmlSerializerContext.getMacroInvocations();
		assertEquals(Arrays.asList(invocation1, invocation2, invocation3), result);
	}
}
