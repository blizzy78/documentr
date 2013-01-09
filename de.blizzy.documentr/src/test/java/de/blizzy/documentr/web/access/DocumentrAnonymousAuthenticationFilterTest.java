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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;

public class DocumentrAnonymousAuthenticationFilterTest extends AbstractDocumentrTest {
	@Mock
	private AnonymousAuthenticationToken authentication;
	@Mock
	private DocumentrAnonymousAuthenticationFactory authenticationFactory;
	@Mock
	private HttpServletRequest request;
	@InjectMocks
	private DocumentrAnonymousAuthenticationFilter filter;

	@Test
	public void createAuthentication() throws IOException {
		when(authenticationFactory.create(anyString())).thenReturn(authentication);

		Authentication result = filter.createAuthentication(request);
		assertSame(authentication, result);
	}
}
