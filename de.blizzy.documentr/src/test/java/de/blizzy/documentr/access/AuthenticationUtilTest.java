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
package de.blizzy.documentr.access;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpSession;

import org.junit.Test;

public class AuthenticationUtilTest {
	@Test
	public void setAuthenticationCreationTime() {
		HttpSession session = mock(HttpSession.class);
		AuthenticationUtil.setAuthenticationCreationTime(session, 123);
		verify(session).setAttribute("authenticationCreationTime", Long.valueOf(123)); //$NON-NLS-1$
	}
	
	@Test
	public void getAuthenticationCreationTime() {
		HttpSession session = mock(HttpSession.class);
		Calendar c = new GregorianCalendar(2012, Calendar.JUNE, 17);
		c.set(Calendar.MILLISECOND, 123);
		long time = c.getTimeInMillis();
		when(session.getAttribute("authenticationCreationTime")).thenReturn(Long.valueOf(time)); //$NON-NLS-1$
		assertEquals(time / 1000L * 1000L, AuthenticationUtil.getAuthenticationCreationTime(session));
	}
}
