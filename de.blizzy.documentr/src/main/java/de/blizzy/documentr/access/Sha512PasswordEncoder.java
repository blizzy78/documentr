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

import java.security.SecureRandom;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

public class Sha512PasswordEncoder implements PasswordEncoder {
	private SecureRandom RANDOM = new SecureRandom();
	private MessageDigestPasswordEncoder digestEncoder = new ShaPasswordEncoder(512);

	public Sha512PasswordEncoder() {
		RANDOM.setSeed(System.currentTimeMillis());
		digestEncoder.setEncodeHashAsBase64(true);
	}
	
	@Override
	public String encodePassword(String rawPass, Object salt) {
		salt = String.valueOf(getRandomLong());
		String encPass = digestEncoder.encodePassword(rawPass, salt);
		return encPass + "{" + salt + "}"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		int saltStartPos = StringUtils.indexOf(encPass, "{") + 1; //$NON-NLS-1$
		int saltEndPos = StringUtils.indexOf(encPass, "}", saltStartPos); //$NON-NLS-1$
		salt = encPass.substring(saltStartPos, saltEndPos);
		encPass = encPass.substring(0, saltStartPos - 1);
		return digestEncoder.isPasswordValid(encPass, rawPass, salt);
	}
	
	private long getRandomLong() {
		synchronized (RANDOM) {
			return RANDOM.nextLong();
		}
	}
}
