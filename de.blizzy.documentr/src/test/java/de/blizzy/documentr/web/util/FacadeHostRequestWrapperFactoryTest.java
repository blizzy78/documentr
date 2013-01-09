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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.system.SystemSettingsStore;

public class FacadeHostRequestWrapperFactoryTest extends AbstractDocumentrTest {
	private static final String URI = "/foo/bar.html?baz=qux&quux=quuux"; //$NON-NLS-1$
	private static final String DOCUMENTR_HOST = "https://documentr.org:1234"; //$NON-NLS-1$

	@InjectMocks
	private FacadeHostRequestWrapperFactory factory;
	@Mock
	private SystemSettingsStore systemSettingsStore;
	@Mock
	private HttpServletRequest request;

	@Before
	public void setUp() {
		when(systemSettingsStore.getSetting(SystemSettingsStore.DOCUMENTR_HOST)).thenReturn(DOCUMENTR_HOST);

		when(request.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com:8080" + URI)); //$NON-NLS-1$
	}

	@Test
	public void create() {
		HttpServletRequest requestWrapper = factory.create(request);
		assertEquals(DOCUMENTR_HOST + URI, requestWrapper.getRequestURL().toString());
	}
}
