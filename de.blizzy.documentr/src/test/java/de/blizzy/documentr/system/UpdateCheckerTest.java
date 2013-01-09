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
package de.blizzy.documentr.system;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.support.membermodification.MemberMatcher.*;
import static org.powermock.api.support.membermodification.MemberModifier.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.common.base.Charsets;

import de.blizzy.documentr.AbstractDocumentrTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UpdateChecker.class)
public class UpdateCheckerTest extends AbstractDocumentrTest {
	private static final String CURRENT_VERSION = "1.0.0"; //$NON-NLS-1$
	private static final String LATEST_VERSION = "1.1.0"; //$NON-NLS-1$

	@Mock
	private SystemSettingsStore systemSettingsStore;
	@Mock
	private Downloader downloader;
	@InjectMocks
	private UpdateChecker updateChecker;
	private String updatePropertiesUrl;

	@Before
	public void setUp() throws Exception {
		Field updatePropertiesUrlField = Whitebox.getField(UpdateChecker.class, "UPDATE_PROPERTIES_URL"); //$NON-NLS-1$
		updatePropertiesUrl = updatePropertiesUrlField.get(null).toString();

		PowerMockito.whenNew(Downloader.class).withNoArguments().thenReturn(downloader);

		replace(method(UpdateChecker.class, "getResourceAsStream")).with(new InvocationHandler() { //$NON-NLS-1$
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				String text = "version=" + CURRENT_VERSION; //$NON-NLS-1$
				byte[] data = text.getBytes(Charsets.UTF_8);
				return new ByteArrayInputStream(data);
			}
		});

		when(systemSettingsStore.getSetting(SystemSettingsStore.UPDATE_CHECK_INTERVAL))
			.thenReturn(SystemSettingsStore.UPDATE_CHECK_INTERVAL_DAILY);
	}

	@Test
	public void checkForUpdate() throws IOException {
		when(downloader.getTextFromUrl(updatePropertiesUrl, Charsets.UTF_8))
			.thenReturn(LATEST_VERSION + "=2012-10-26"); //$NON-NLS-1$

		updateChecker.checkForUpdate();
		assertTrue(updateChecker.isUpdateAvailable());
		assertEquals(LATEST_VERSION, updateChecker.getLatestVersion());
	}

	@Test
	public void checkForUpdateButNoUpdateAvailable() throws IOException {
		when(downloader.getTextFromUrl(updatePropertiesUrl, Charsets.UTF_8))
			.thenReturn(CURRENT_VERSION + "=2012-10-26"); //$NON-NLS-1$

		updateChecker.checkForUpdate();
		assertFalse(updateChecker.isUpdateAvailable());
	}
}
