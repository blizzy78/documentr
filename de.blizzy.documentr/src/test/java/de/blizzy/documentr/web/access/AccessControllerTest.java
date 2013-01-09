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
package de.blizzy.documentr.web.access;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.ui.Model;

import de.blizzy.documentr.AbstractDocumentrTest;

public class AccessControllerTest extends AbstractDocumentrTest {
	@Mock
	private AuthenticationException authenticationException;
	@Mock
	private AccessDeniedException accessDeniedException;
	@Mock
	private HttpServletRequest request;
	@Mock
	private Model model;
	@Mock
	private HttpSession session;

	@Test
	public void login() {
		assertEquals("/login", new AccessController().login()); //$NON-NLS-1$
	}

	@Test
	public void loginError() {
		when(authenticationException.getMessage()).thenReturn("message"); //$NON-NLS-1$
		when(session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)).thenReturn(authenticationException);

		String view = new AccessController().loginError(session, model);
		assertEquals("/login", view); //$NON-NLS-1$

		verify(model).addAttribute("errorMessage", "message"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void loginForbidden() {
		when(accessDeniedException.getMessage()).thenReturn("message"); //$NON-NLS-1$
		when(request.getAttribute(WebAttributes.ACCESS_DENIED_403)).thenReturn(accessDeniedException);

		String view = new AccessController().loginForbidden(request, model);
		assertEquals("/login", view); //$NON-NLS-1$

		verify(model).addAttribute("errorMessage", "message"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
