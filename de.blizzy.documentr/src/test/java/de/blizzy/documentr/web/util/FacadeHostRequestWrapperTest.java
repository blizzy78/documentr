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
package de.blizzy.documentr.web.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.Mock;

import de.blizzy.documentr.AbstractDocumentrTest;

public class FacadeHostRequestWrapperTest extends AbstractDocumentrTest {
	@Mock
	private HttpServletRequest request;

	@Test
	public void getRequestURL() {
		when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:38080/documentr/page")); //$NON-NLS-1$
		when(request.getContextPath()).thenReturn("/documentr"); //$NON-NLS-1$
		FacadeHostRequestWrapper requestWrapper = new FacadeHostRequestWrapper(
				request, "https://documentr.org:1234/docs"); //$NON-NLS-1$
		assertEquals("https://documentr.org:1234/docs/page", requestWrapper.getRequestURL().toString()); //$NON-NLS-1$
	}

	@Test
	@SuppressWarnings("nls")
	public void buildFacadeUrl() {
		assertEquals("https://documentr.org/page", FacadeHostRequestWrapper.buildFacadeUrl(
				"http://localhost:38080/documentr/page", "/documentr", "https://documentr.org"));
		assertEquals("https://documentr.org:1234/page", FacadeHostRequestWrapper.buildFacadeUrl(
				"http://localhost:38080/documentr/page", "/documentr", "https://documentr.org:1234"));
		assertEquals("https://documentr.org:1234/docs/page", FacadeHostRequestWrapper.buildFacadeUrl(
				"http://localhost:38080/documentr/page", "/documentr", "https://documentr.org:1234/docs"));
		assertEquals("https://documentr.org/page", FacadeHostRequestWrapper.buildFacadeUrl(
				"http://localhost:38080/page", StringUtils.EMPTY, "https://documentr.org"));
		assertEquals("https://documentr.org:1234/page", FacadeHostRequestWrapper.buildFacadeUrl(
				"http://localhost:38080/page", StringUtils.EMPTY, "https://documentr.org:1234"));
		assertEquals("https://documentr.org:1234/docs/page", FacadeHostRequestWrapper.buildFacadeUrl(
				"http://localhost:38080/page", StringUtils.EMPTY, "https://documentr.org:1234/docs"));
		assertEquals("https://documentr.org:1234/docs", FacadeHostRequestWrapper.buildFacadeUrl(
				"http://localhost:38080", StringUtils.EMPTY, "https://documentr.org:1234/docs"));
	}
}
