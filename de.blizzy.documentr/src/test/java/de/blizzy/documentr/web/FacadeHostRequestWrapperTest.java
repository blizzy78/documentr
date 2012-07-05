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
package de.blizzy.documentr.web;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import de.blizzy.documentr.AbstractDocumentrTest;

public class FacadeHostRequestWrapperTest extends AbstractDocumentrTest {
	private static final String URI = "/foo/bar.html?baz=qux&quux=quuux"; //$NON-NLS-1$
	private static final String FACADE_HOST = "test.example.org"; //$NON-NLS-1$
	private static final int FACADE_PORT = 9090;
	
	@Mock
	private HttpServletRequest request;
	
	@Before
	public void setUp() {
		when(request.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com:8080" + URI)); //$NON-NLS-1$
	}
	
	@Test
	public void getRequestURL() {
		FacadeHostRequestWrapper requestWrapper = new FacadeHostRequestWrapper(request, FACADE_HOST, FACADE_PORT);
		assertEquals("http://" + FACADE_HOST + ":" + String.valueOf(FACADE_PORT) + URI, //$NON-NLS-1$ //$NON-NLS-2$
			requestWrapper.getRequestURL().toString());
	}
}
