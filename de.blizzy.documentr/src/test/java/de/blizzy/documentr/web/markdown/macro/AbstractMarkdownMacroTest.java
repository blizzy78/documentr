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
package de.blizzy.documentr.web.markdown.macro;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import de.blizzy.documentr.web.markdown.HtmlSerializerContext;

public class AbstractMarkdownMacroTest {
	private static final class TestMacro extends AbstractMarkdownMacro {
		@Override
		protected String getMarkdown(String body) {
			return getParameters();
		}
	}
	
	@Test
	public void getHtml() {
		HtmlSerializerContext context = mock(HtmlSerializerContext.class);
		when(context.markdownToHTML(anyString())).thenReturn("markdown"); //$NON-NLS-1$
		
		TestMacro macro = new TestMacro();
		macro.setHtmlSerializerContext(context);
		
		macro.setParameters(null);
		assertNull(macro.getHtml(null));

		macro.setParameters(StringUtils.EMPTY);
		assertNull(macro.getHtml(null));

		macro.setParameters("foo"); //$NON-NLS-1$
		assertEquals("markdown", macro.getHtml(null)); //$NON-NLS-1$
	}
}
