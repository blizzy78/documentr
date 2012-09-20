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

public class MultiPasswordEncoderTest extends AbstractDocumentrTest {
	private static final String PASSWORD = "secret"; //$NON-NLS-1$
	
	@Mock
	private SystemSettingsStore systemSettingsStore;
	@Mock
	@SuppressWarnings("unused")
	private EventBus eventBus;
	@InjectMocks
	private BCryptPasswordEncoder defaultEncoder;
	private SecureRandom ignoredRandom;
	private Sha512PasswordEncoder otherEncoder;
	private MultiPasswordEncoder multiEncoder;
	
	@Before
	public void setUp() {
		ignoredRandom = new SecureRandom();
		ignoredRandom.setSeed(System.currentTimeMillis());

		when(systemSettingsStore.getSetting(SystemSettingsStore.BCRYPT_ROUNDS)).thenReturn("4"); //$NON-NLS-1$
		defaultEncoder.init();
		otherEncoder = new Sha512PasswordEncoder(1);
		multiEncoder = new MultiPasswordEncoder(defaultEncoder, otherEncoder);
	}
	
	@Test
	public void encodePasswordMustUseDefaultEncoder() {
		String encPass = multiEncoder.encodePassword(PASSWORD, salt());
		assertTrue(defaultEncoder.isPasswordValid(encPass, PASSWORD, salt()));
	}
	
	@Test
	public void isPasswordValidMustCheckAllEncoders() {
		String encPass = defaultEncoder.encodePassword(PASSWORD, salt());
		assertTrue(multiEncoder.isPasswordValid(encPass, PASSWORD, salt()));

		encPass = otherEncoder.encodePassword(PASSWORD, salt());
		assertTrue(multiEncoder.isPasswordValid(encPass, PASSWORD, salt()));
	}

	private Long salt() {
		return Long.valueOf(ignoredRandom.nextLong());
	}
}
