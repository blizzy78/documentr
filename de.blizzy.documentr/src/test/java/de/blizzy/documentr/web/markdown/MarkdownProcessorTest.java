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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.web.markdown.macro.IMacro;
import de.blizzy.documentr.web.markdown.macro.impl.MacroFactory;

public class MarkdownProcessorTest {
	private static final String MACRO = "macro"; //$NON-NLS-1$
	private static final String PARAMS = "params"; //$NON-NLS-1$
	
	private MacroFactory macroFactory;
	private MarkdownProcessor markdownProcessor;
	private Authentication authentication;

	@Before
	public void setUp() {
		macroFactory = mock(MacroFactory.class);

		markdownProcessor = new MarkdownProcessor();
		markdownProcessor.setMacroFactory(macroFactory);
		
		authentication = mock(Authentication.class);
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void markdownToHTML() {
		IMacro macro = mock(IMacro.class);
		final String macroHtml = "<div>macroHtml</div>"; //$NON-NLS-1$
		when(macro.isCacheable()).thenReturn(true);
		when(macro.getHtml()).thenReturn(macroHtml);
		final String cleanedMacroHtml = "<div>cleanedMacroHtml</div>"; //$NON-NLS-1$
		when(macro.cleanupHTML(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String html = (String) invocation.getArguments()[0];
				return StringUtils.replace(html, macroHtml, cleanedMacroHtml);
			}
		});
		
		when(macroFactory.get(eq(MACRO), (String) isNull(), Matchers.<HtmlSerializerContext>any())).thenReturn(macro);
		
		String markdown = "**foo**\n\n{{" + MACRO + "/}}\n\nbar\n"; //$NON-NLS-1$ //$NON-NLS-2$
		String result = markdownProcessor.markdownToHTML(
				markdown, "project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/bar", authentication); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		String expectedHTML = "<p><strong>foo</strong></p>" + cleanedMacroHtml + "<p>bar</p>"; //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(expectedHTML, result);
	}

	@Test
	@SuppressWarnings("boxing")
	public void markdownToHTMLMustNotRenderNonCacheableMacros() {
		IMacro macro = mock(IMacro.class);
		when(macro.isCacheable()).thenReturn(false);
		
		when(macroFactory.get(eq(MACRO), eq(PARAMS), Matchers.<HtmlSerializerContext>any())).thenReturn(macro);
		
		String markdown = "**foo**\n\n{{" + MACRO + " " + PARAMS + "/}}\n\nbar\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String result = markdownProcessor.markdownToHTML(
				markdown, "project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/bar", authentication); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		String expectedHTML = "<p><strong>foo</strong></p><p>" + //$NON-NLS-1$
				MarkdownProcessor.NON_CACHEABLE_MACRO_MARKER + MACRO + " " + PARAMS + //$NON-NLS-1$
				"/" + MarkdownProcessor.NON_CACHEABLE_MACRO_MARKER + "</p><p>bar</p>"; //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(expectedHTML, result);
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void processNonCacheableMacros() {
		IMacro macro = mock(IMacro.class);
		final String macroHtml = "<div>macroHtml</div>"; //$NON-NLS-1$
		when(macro.isCacheable()).thenReturn(false);
		when(macro.getHtml()).thenReturn(macroHtml);
		final String cleanedMacroHtml = "<div>cleanedMacroHtml</div>"; //$NON-NLS-1$
		when(macro.cleanupHTML(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String html = (String) invocation.getArguments()[0];
				return StringUtils.replace(html, macroHtml, cleanedMacroHtml);
			}
		});
		
		when(macroFactory.get(eq(MACRO), eq(PARAMS), Matchers.<HtmlSerializerContext>any())).thenReturn(macro);

		String html = "<p>" + MarkdownProcessor.NON_CACHEABLE_MACRO_MARKER + MACRO + " " + //$NON-NLS-1$ //$NON-NLS-2$
				PARAMS + "/" + MarkdownProcessor.NON_CACHEABLE_MACRO_MARKER + "</p>"; //$NON-NLS-1$ //$NON-NLS-2$
		String result = markdownProcessor.processNonCacheableMacros(
				html, "project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/bar", authentication); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String expectedHTML = cleanedMacroHtml;
		assertEquals(expectedHTML, result);
	}
}
