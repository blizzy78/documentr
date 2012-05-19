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
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.blizzy.documentr.web.markdown.Header;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;

public class TableOfContentsMacroTest {
	@Test
	public void getMarkdown() {
		List<Header> headers = new ArrayList<Header>();
		headers.add(new Header("foo", 1)); //$NON-NLS-1$
		headers.add(new Header("bar", 2)); //$NON-NLS-1$
		headers.add(new Header("baz", 3)); //$NON-NLS-1$
		headers.add(new Header("qux", 1)); //$NON-NLS-1$
		HtmlSerializerContext context = mock(HtmlSerializerContext.class);
		when(context.getHeaders()).thenReturn(headers);
		
		TableOfContentsMacro macro = new TableOfContentsMacro();
		macro.setHtmlSerializerContext(context);
		
		assertEquals(
				"- [[#foo]]\n" + //$NON-NLS-1$
				"    - [[#bar]]\n" + //$NON-NLS-1$
				"        - [[#baz]]\n" + //$NON-NLS-1$
				"- [[#qux]]\n\n", //$NON-NLS-1$
				macro.getMarkdown());
	}
}
