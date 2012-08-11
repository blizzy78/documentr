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
package de.blizzy.documentr.web.access;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.ui.Model;

public class AccessControllerTest {
	@Test
	public void login() {
		assertEquals("/login", new AccessController().login()); //$NON-NLS-1$
	}

	@Test
	public void loginError() {
		AuthenticationException ex = mock(AuthenticationException.class);
		when(ex.getMessage()).thenReturn("message"); //$NON-NLS-1$
		HttpSession session = mock(HttpSession.class);
		when(session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)).thenReturn(ex);
		Model model = mock(Model.class);
		
		String view = new AccessController().loginError(session, model);
		assertEquals("/login", view); //$NON-NLS-1$
		
		verify(model).addAttribute("errorMessage", "message"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void loginForbidden() {
		AccessDeniedException ex = mock(AccessDeniedException.class);
		when(ex.getMessage()).thenReturn("message"); //$NON-NLS-1$
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getAttribute(WebAttributes.ACCESS_DENIED_403)).thenReturn(ex);
		Model model = mock(Model.class);
		
		String view = new AccessController().loginForbidden(request, model);
		assertEquals("/login", view); //$NON-NLS-1$
		
		verify(model).addAttribute("errorMessage", "message"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
