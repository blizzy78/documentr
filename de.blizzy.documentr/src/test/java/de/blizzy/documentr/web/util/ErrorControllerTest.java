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
package de.blizzy.documentr.web.util;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.ui.Model;

import de.blizzy.documentr.AbstractDocumentrTest;

public class ErrorControllerTest extends AbstractDocumentrTest {
	@Mock
	private Model model;
	private ErrorController errorController;

	@Before
	public void setUp() {
		errorController = new ErrorController();
	}

	@Test
	public void sendError() {
		String view = errorController.sendError(HttpServletResponse.SC_FORBIDDEN, "key", model); //$NON-NLS-1$
		assertEquals("/sendError", view); //$NON-NLS-1$

		verify(model).addAttribute("statusCode", HttpServletResponse.SC_FORBIDDEN); //$NON-NLS-1$
		verify(model).addAttribute("messageKey", "key"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void notFound() {
		String view = ErrorController.notFound("key"); //$NON-NLS-1$
		assertEquals("/error/" + HttpServletResponse.SC_NOT_FOUND + "/key", removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$
		assertForward(view);
	}
}
