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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.macro.IMacroContext;

public class TwitterMacroTest extends AbstractDocumentrTest {
	@Mock
	private IMacroContext context;
	private TwitterMacro runnable;

	@Before
	public void setUp() {
		runnable = new TwitterMacro();
	}

	@Test
	public void getHtml() {
		when(context.getParameters()).thenReturn("\"searchParams\""); //$NON-NLS-1$

		String html = runnable.getHtml(context);
		@SuppressWarnings("nls")
		String expectedHtml = "<script charset=\"UTF-8\" src=\"http://widgets.twimg.com/j/2/widget.js\"></script>\n" +
			"<script>\n" +
			"new TWTR.Widget({" +
				"version: 2," +
				"type: 'search'," +
				"search: '" + StringEscapeUtils.escapeEcmaScript("\"searchParams\"") + "'," +
				"interval: 15000," +
				"title: ''," +
				"subject: ''," +
				"width: 300," +
				"height: 300," +
				"features: {" +
					"scrollbar: true," +
					"loop: false," +
					"live: true," +
					"behavior: 'default'" +
				"}" +
			"}).render().start(); require(['documentr/fixTwitterCss']);\n" +
			"</script>\n";
		assertEquals(expectedHtml, html);
	}
}
