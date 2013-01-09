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
package de.blizzy.documentr.web.filter;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.TestUtil;

public class AuthenticationCreationTimeFilterTest extends AbstractDocumentrTest {
	@Mock
	private Authentication authentication;
	@Mock
	private HttpSession session;
	@Mock
	private HttpServletRequest request;
	@Mock
	private ServletResponse response;
	@Mock
	private FilterChain filterChain;

	@Test
	public void doFilter() throws IOException, ServletException {
		when(authentication.getName()).thenReturn("user"); //$NON-NLS-1$
		doReturn(Sets.newHashSet(
				new SimpleGrantedAuthority("authority1"), //$NON-NLS-1$
				new SimpleGrantedAuthority("authority2"))) //$NON-NLS-1$
			.when(authentication).getAuthorities();

		when(session.getAttribute("authenticationHashCode")).thenReturn(123); //$NON-NLS-1$

		when(request.getSession()).thenReturn(session);

		SecurityContextImpl securityContext = new SecurityContextImpl();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);
		new AuthenticationCreationTimeFilter().doFilter(request, response, filterChain);
		SecurityContextHolder.clearContext();

		ArgumentCaptor<Long> timeArgument = ArgumentCaptor.forClass(Long.class);
		verify(session).setAttribute(eq("authenticationCreationTime"), timeArgument.capture()); //$NON-NLS-1$
		TestUtil.assertSecondsAgo(new Date(timeArgument.getValue()), 5);
	}
}
