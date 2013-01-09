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

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.google.common.base.Charsets;

import de.blizzy.documentr.AbstractDocumentrTest;

public class RequestEncodingFilterTest extends AbstractDocumentrTest {
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;

	@Test
	public void foo() throws IOException, ServletException {
		RequestEncodingFilter filter = new RequestEncodingFilter();
		filter.doFilter(request, response, filterChain);

		InOrder inOrder = inOrder(request, response, filterChain);
		inOrder.verify(request).setCharacterEncoding(Charsets.UTF_8.name());
		inOrder.verify(response).setCharacterEncoding(Charsets.UTF_8.name());
		inOrder.verify(filterChain).doFilter(request, response);
	}
}
