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
package de.blizzy.documentr.web.filter;

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.base.Charsets;

public class RequestEncodingFilterTest {
	@Test
	public void foo() throws IOException, ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		
		RequestEncodingFilter filter = new RequestEncodingFilter();
		filter.doFilter(request, response, chain);

		InOrder inOrder = inOrder(request, response, chain);
		inOrder.verify(request).setCharacterEncoding(Charsets.UTF_8.name());
		inOrder.verify(response).setCharacterEncoding(Charsets.UTF_8.name());
		inOrder.verify(chain).doFilter(request, response);
	}
}
