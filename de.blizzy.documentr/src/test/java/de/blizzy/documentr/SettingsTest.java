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
package de.blizzy.documentr;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;

import javax.servlet.ServletContext;

import org.junit.Test;

public class SettingsTest {
	@Test
	public void init() {
		ServletContext servletContext = mock(ServletContext.class);
		when(servletContext.getInitParameter("documentr.dataDir")).thenReturn("."); //$NON-NLS-1$ //$NON-NLS-2$
		Settings settings = new Settings();
		settings.setServletContext(servletContext);
		
		settings.init();
		assertEquals(new File("."), settings.getDocumentrDataDir()); //$NON-NLS-1$
	}
}
