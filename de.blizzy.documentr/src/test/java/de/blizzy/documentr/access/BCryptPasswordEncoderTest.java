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

import java.security.SecureRandom;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.encoding.PasswordEncoder;

public class BCryptPasswordEncoderTest {
	public static final String PASSWORD = "secret"; //$NON-NLS-1$
	
	private SecureRandom ignoredRandom;
	
	@Before
	public void setUp() {
		ignoredRandom = new SecureRandom();
		ignoredRandom.setSeed(System.currentTimeMillis());
	}
	
	@Test
	public void encodeAndCheckPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
		String encPass = passwordEncoder.encodePassword(PASSWORD, salt());
		assertTrue(passwordEncoder.isPasswordValid(encPass, PASSWORD, salt()));
	}

	@Test
	public void isPasswordValidMustUseIterationsFromEncodedPassword() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
		String encPass = passwordEncoder.encodePassword(PASSWORD, salt());
		
		passwordEncoder = new BCryptPasswordEncoder(5);
		assertTrue(passwordEncoder.isPasswordValid(encPass, PASSWORD, salt()));
	}

	private Long salt() {
		return Long.valueOf(ignoredRandom.nextLong());
	}
}
