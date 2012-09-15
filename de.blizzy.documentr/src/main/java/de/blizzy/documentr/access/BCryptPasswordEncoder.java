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

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.google.common.base.Stopwatch;

@Slf4j
public class BCryptPasswordEncoder implements PasswordEncoder {
	private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder;

	public BCryptPasswordEncoder(int strength) {
		encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(strength);
	}

	@Override
	public String encodePassword(String rawPass, Object salt) {
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		String encPass = encoder.encode(rawPass);
		stopwatch.stop();
		if (log.isTraceEnabled()) {
			log.trace("time taken to encode password: {} ms", Long.valueOf(stopwatch.elapsedMillis())); //$NON-NLS-1$
		}
		return encPass;
	}

	@Override
	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		boolean valid = encoder.matches(rawPass, encPass);
		stopwatch.stop();
		if (log.isTraceEnabled()) {
			log.trace("time taken to verify password: {} ms", Long.valueOf(stopwatch.elapsedMillis())); //$NON-NLS-1$
		}
		return valid;
	}
}
