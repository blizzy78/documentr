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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.util.Assert;

/**
 * Password encoder that encodes passwords with <a href="http://en.wikipedia.org/wiki/SHA-2">SHA-512</a>
 * using a random <a href="http://en.wikipedia.org/wiki/Salt_(cryptography)">salt</a> and a configurable
 * number of <a href="http://en.wikipedia.org/wiki/Key_stretching">iterations</a>.
 */
public class Sha512PasswordEncoder implements PasswordEncoder {
	private static final Pattern PATTERN = Pattern.compile("^.+?\\{.+?\\}\\{[0-9]+?\\}$"); //$NON-NLS-1$
	
	private int iterations;
	private SecureRandom random = new SecureRandom();

	/**
	 * Constructs a new SHA-512 password encoder.
	 * 
	 * @param iterations the number of iterations to perform
	 */
	public Sha512PasswordEncoder(int iterations) {
		Assert.isTrue(iterations >= 1);
		
		this.iterations = iterations;
		
		random.setSeed(System.currentTimeMillis());
	}

	/**
	 * Encodes the specified raw password. This method ignores the specified salt and uses its own random salt generation.
	 * 
	 * @param rawPass the raw password to encode
	 * @param salt (ignored)
	 */
	@Override
	public String encodePassword(String rawPass, Object salt) {
		String newSalt = String.valueOf(getRandomLong());
		PasswordEncoder digestEncoder = createDigestEncoder(iterations);
		String encPass = digestEncoder.encodePassword(rawPass, newSalt);
		return encPass + "{" + newSalt + "}{" + String.valueOf(iterations) + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Verifies a raw password against a previously encoded password.
	 * 
	 * @param encPass The previously encoded password to verify against. This password may have been encoded using a
	 *        different number of iterations than this password encoder is instructed to perform.
	 * @param rawPass the raw password to verify
	 * @param salt (ignored)
	 */
	@Override
	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		if (!PATTERN.matcher(encPass).matches()) {
			throw new IllegalArgumentException("password does not seem to have been encoded with this encoder"); //$NON-NLS-1$
		}
		
		int saltStartPos = StringUtils.indexOf(encPass, "{") + 1; //$NON-NLS-1$
		int saltEndPos = StringUtils.indexOf(encPass, "}", saltStartPos); //$NON-NLS-1$
		String newSalt = encPass.substring(saltStartPos, saltEndPos);
		int iterationsStartPos = StringUtils.indexOf(encPass, "{", saltEndPos + 1) + 1; //$NON-NLS-1$
		int iterationsEndPos = StringUtils.indexOf(encPass, "}", iterationsStartPos); //$NON-NLS-1$
		int iterations = Integer.parseInt(encPass.substring(iterationsStartPos, iterationsEndPos));
		encPass = encPass.substring(0, saltStartPos - 1);
		PasswordEncoder digestEncoder = createDigestEncoder(iterations);
		return digestEncoder.isPasswordValid(encPass, rawPass, newSalt);
	}

	private PasswordEncoder createDigestEncoder(int iterations) {
		ShaPasswordEncoder encoder = new ShaPasswordEncoder(512);
		encoder.setEncodeHashAsBase64(true);
		encoder.setIterations(iterations);
		return encoder;
	}
	
	private long getRandomLong() {
		synchronized (random) {
			return random.nextLong();
		}
	}
}
