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
import org.mockito.InjectMocks;
import org.mockito.Mock;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;

public class IfMacroTest extends AbstractDocumentrTest {
	public static String projectName;
	public static String branchName;
	public static String pagePath;

	@SuppressWarnings("nls")
	private static final String EXPRESSION =
			"{ ->\n" +
				IfMacroTest.class.getName() + ".projectName = projectName\n" +
				IfMacroTest.class.getName() + ".branchName = branchName\n" +
				IfMacroTest.class.getName() + ".pagePath = pagePath\n" +
				"true\n" +
			"}.call()";
	private static final String BODY = "body"; //$NON-NLS-1$
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$

	@Mock
	private IMacroContext context;
	@Mock
	private HtmlSerializerContext htmlSerializerContext;
	@InjectMocks
	private IfMacro macro;

	@Before
	public void setUp() {
		when(context.getParameters()).thenReturn(EXPRESSION);
		when(context.getBody()).thenReturn(BODY);
		when(context.getHtmlSerializerContext()).thenReturn(htmlSerializerContext);

		when(htmlSerializerContext.getProjectName()).thenReturn(PROJECT);
		when(htmlSerializerContext.getBranchName()).thenReturn(BRANCH);
		when(htmlSerializerContext.getPagePath()).thenReturn(PAGE);

		projectName = null;
		branchName = null;
		pagePath = null;
	}

	@Test
	public void getHtml() {
		String html = macro.getHtml(context);
		assertEquals(BODY, html);
		// verify bindings
		assertEquals(PROJECT, projectName);
		assertEquals(BRANCH, branchName);
		assertEquals(PAGE, pagePath);
	}
}
