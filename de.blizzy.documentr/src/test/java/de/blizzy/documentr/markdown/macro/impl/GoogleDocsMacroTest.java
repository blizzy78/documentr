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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.impl.GoogleDocsMacro;

public class GoogleDocsMacroTest extends AbstractDocumentrTest {
	private GoogleDocsMacro runnable;
	@Mock
	private IMacroContext context;

	@Before
	public void setUp() {
		runnable = new GoogleDocsMacro();
	}

	@Test
	public void getHtmlForSpreadsheet() {
		when(context.getParameters()).thenReturn("https://docs.google.com/spreadsheet/pub?key=0As6LH-BXhcPZdEFtWi1Udl9HM0Z6T3h5NmhLYzlrd1E&output=html"); //$NON-NLS-1$
		assertEquals("<iframe class=\"googledocs-document\" src=\"" + //$NON-NLS-1$
				"https://docs.google.com/spreadsheet/pub?key=0As6LH-BXhcPZdEFtWi1Udl9HM0Z6T3h5NmhLYzlrd1E&output=html&widget=true" + //$NON-NLS-1$
				"\" allowfullscreen=\"true\" mozallowfullscreen=\"true\" webkitallowfullscreen=\"true\"></iframe>", //$NON-NLS-1$
				runnable.getHtml(context));
	}

	@Test
	public void getHtmlForTextDocument() {
		when(context.getParameters()).thenReturn("https://docs.google.com/document/pub?id=1TjMiACP4BZsPiY3KIXHiXOw11QpLtLOfFC-G-4EQinc"); //$NON-NLS-1$
		assertEquals("<iframe class=\"googledocs-document\" src=\"" + //$NON-NLS-1$
				"https://docs.google.com/document/pub?id=1TjMiACP4BZsPiY3KIXHiXOw11QpLtLOfFC-G-4EQinc&embedded=true" + //$NON-NLS-1$
				"\" allowfullscreen=\"true\" mozallowfullscreen=\"true\" webkitallowfullscreen=\"true\"></iframe>", //$NON-NLS-1$
				runnable.getHtml(context));
	}

	@Test
	public void getHtmlForPresentation() {
		when(context.getParameters()).thenReturn("https://docs.google.com/presentation/pub?id=1WzJncZtIcd9up5b_MI2oBceQ2PnTmIUSltp0RKYXqJo&start=false&loop=false&delayms=4000"); //$NON-NLS-1$
		assertEquals("<iframe class=\"googledocs-document\" src=\"" + //$NON-NLS-1$
				"https://docs.google.com/presentation/embed?id=1WzJncZtIcd9up5b_MI2oBceQ2PnTmIUSltp0RKYXqJo&start=false&loop=false&delayms=3000" + //$NON-NLS-1$
				"\" allowfullscreen=\"true\" mozallowfullscreen=\"true\" webkitallowfullscreen=\"true\"></iframe>", //$NON-NLS-1$
				runnable.getHtml(context));
	}

	@Test
	public void getHtmlForDrawing() {
		when(context.getParameters()).thenReturn("https://docs.google.com/drawings/pub?id=1ZHG2f0l-NgC52MwW9nWwbnrNIkE4azVhVJ9plMry3ic&w=210&h=196"); //$NON-NLS-1$
		assertEquals("<img src=\"" + //$NON-NLS-1$
				"https://docs.google.com/drawings/pub?id=1ZHG2f0l-NgC52MwW9nWwbnrNIkE4azVhVJ9plMry3ic&w=960" + //$NON-NLS-1$
				"\"/>", //$NON-NLS-1$
				runnable.getHtml(context));
	}

	@Test
	public void getHtmlForDrawingWithWidth() {
		when(context.getParameters()).thenReturn("https://docs.google.com/drawings/pub?id=1ZHG2f0l-NgC52MwW9nWwbnrNIkE4azVhVJ9plMry3ic&w=210&h=196 123"); //$NON-NLS-1$
		assertEquals("<img src=\"" + //$NON-NLS-1$
				"https://docs.google.com/drawings/pub?id=1ZHG2f0l-NgC52MwW9nWwbnrNIkE4azVhVJ9plMry3ic&w=123" + //$NON-NLS-1$
				"\"/>", //$NON-NLS-1$
				runnable.getHtml(context));
	}
}
