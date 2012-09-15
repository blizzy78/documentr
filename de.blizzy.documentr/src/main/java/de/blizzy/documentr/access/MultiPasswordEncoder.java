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

import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.util.Assert;

public class MultiPasswordEncoder implements PasswordEncoder {
	private PasswordEncoder[] encoders;

	public MultiPasswordEncoder(PasswordEncoder defaultEncoder, PasswordEncoder... otherEncoders) {
		Assert.notNull(defaultEncoder);
		
		if (otherEncoders == null) {
			otherEncoders = new PasswordEncoder[0];
		}
		encoders = new PasswordEncoder[otherEncoders.length + 1];
		encoders[0] = defaultEncoder;
		System.arraycopy(otherEncoders, 0, encoders, 1, otherEncoders.length);
	}
	
	@Override
	public String encodePassword(String rawPass, Object salt) {
		return encoders[0].encodePassword(rawPass, salt);
	}

	@Override
	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		for (PasswordEncoder encoder : encoders) {
			try {
				if (encoder.isPasswordValid(encPass, rawPass, salt)) {
					return true;
				}
			} catch (IllegalArgumentException e) {
				// ignore
			}
		}
		return false;
	}
}
