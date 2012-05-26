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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import de.blizzy.documentr.FirstParameter;
import de.blizzy.documentr.web.markdown.macro.IMacro;
import de.blizzy.documentr.web.markdown.macro.MacroFactory;

public class MarkdownProcessorTest {
	private static final String MACRO = "macro"; //$NON-NLS-1$
	
	private MacroFactory macroFactory;
	private MarkdownProcessor markdownProcessor;

	@Before
	public void setUp() {
		macroFactory = mock(MacroFactory.class);

		markdownProcessor = new MarkdownProcessor();
		markdownProcessor.setMacroFactory(macroFactory);
	}
	
	@Test
	public void markdownToHTML() {
		IMacro macro = mock(IMacro.class);
		String macroHtml = "<div>macroHtml</div>"; //$NON-NLS-1$
		when(macro.getHtml()).thenReturn(macroHtml);
		when(macro.cleanupHTML(anyString())).thenAnswer(new FirstParameter<String>());
		
		when(macroFactory.get(eq(MACRO), (String) isNull(), Matchers.<HtmlSerializerContext>any())).thenReturn(macro);
		
		String markdown = "**foo**\n\n{{" + MACRO + "/}}\n\nbar\n"; //$NON-NLS-1$ //$NON-NLS-2$
		String result = markdownProcessor.markdownToHTML(markdown, "project", "branch", "home/bar"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		String expectedHTML = "<p><strong>foo</strong></p>" + macroHtml + "<p>bar</p>"; //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(expectedHTML, result);
	}
}
