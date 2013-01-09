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
package de.blizzy.documentr.access;

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.security.openid.OpenIDConsumerException;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.web.access.DocumentrOpenIdAuthenticationFilter;
import de.blizzy.documentr.web.util.FacadeHostRequestWrapperFactory;

public class DocumentrOpenIdAuthenticationFilterTest extends AbstractDocumentrTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private OpenIDConsumer consumer;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletRequest requestWrapper;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FacadeHostRequestWrapperFactory facadeHostRequestWrapperFactory;
	@InjectMocks
	private DocumentrOpenIdAuthenticationFilter filter;

	@Test
	@SuppressWarnings("unchecked")
	public void attemptAuthentication() throws AuthenticationException, IOException, OpenIDConsumerException {
		when(facadeHostRequestWrapperFactory.create(request)).thenReturn(requestWrapper);

		when(requestWrapper.getParameter("openid.identity")).thenReturn("identity"); //$NON-NLS-1$ //$NON-NLS-2$

		when(consumer.endConsumption(requestWrapper)).thenThrow(OpenIDConsumerException.class);

		expectedException.expect(AuthenticationServiceException.class);
		filter.attemptAuthentication(request, response);
	}
}
