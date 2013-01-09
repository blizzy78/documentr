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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.security.SecureRandom;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.common.eventbus.EventBus;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.system.SystemSettingsStore;

public class BCryptPasswordEncoderTest extends AbstractDocumentrTest {
	public static final String PASSWORD = "secret"; //$NON-NLS-1$

	@Mock
	private SystemSettingsStore systemSettingsStore;
	@Mock
	@SuppressWarnings("unused")
	private EventBus eventBus;
	@InjectMocks
	private BCryptPasswordEncoder passwordEncoder;
	private SecureRandom ignoredRandom;

	@Before
	public void setUp() {
		ignoredRandom = new SecureRandom();
		ignoredRandom.setSeed(System.currentTimeMillis());
	}

	@Test
	public void encodeAndCheckPassword() {
		when(systemSettingsStore.getSetting(SystemSettingsStore.BCRYPT_ROUNDS)).thenReturn("4"); //$NON-NLS-1$
		passwordEncoder.init();

		String encPass = passwordEncoder.encodePassword(PASSWORD, salt());
		assertTrue(passwordEncoder.isPasswordValid(encPass, PASSWORD, salt()));
	}

	@Test
	public void isPasswordValidMustUseIterationsFromEncodedPassword() {
		when(systemSettingsStore.getSetting(SystemSettingsStore.BCRYPT_ROUNDS)).thenReturn("4"); //$NON-NLS-1$
		passwordEncoder.init();
		String encPass = passwordEncoder.encodePassword(PASSWORD, salt());

		when(systemSettingsStore.getSetting(SystemSettingsStore.BCRYPT_ROUNDS)).thenReturn("4"); //$NON-NLS-1$
		passwordEncoder.init();
		assertTrue(passwordEncoder.isPasswordValid(encPass, PASSWORD, salt()));
	}

	private Long salt() {
		return ignoredRandom.nextLong();
	}
}
