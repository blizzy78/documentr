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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;

public class DocumentrAnonymousAuthenticationFilterTest {
	@Test
	public void createAuthentication() throws IOException {
		AbstractAuthenticationToken authentication = mock(AbstractAuthenticationToken.class);
		
		DocumentrAnonymousAuthenticationFactory authenticationFactory = mock(DocumentrAnonymousAuthenticationFactory.class);
		when(authenticationFactory.create(eq(DocumentrConstants.ANONYMOUS_AUTH_KEY), anyString()))
			.thenReturn(authentication);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		
		DocumentrAnonymousAuthenticationFilter filter = new DocumentrAnonymousAuthenticationFilter();
		filter.setAnonymousAuthenticationFactory(authenticationFactory);

		Authentication result = filter.createAuthentication(request);
		assertSame(authentication, result);
	}
}
