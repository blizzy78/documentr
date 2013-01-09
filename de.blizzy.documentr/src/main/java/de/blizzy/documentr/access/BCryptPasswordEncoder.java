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

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.Subscribe;

import de.blizzy.documentr.system.SystemSettingsChangedEvent;
import de.blizzy.documentr.system.SystemSettingsStore;

@Component("passwordEncoder")
@Slf4j
public class BCryptPasswordEncoder implements PasswordEncoder {
	@Autowired
	private SystemSettingsStore systemSettingsStore;
	private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder;

	@PostConstruct
	public void init() {
		encoder = createEncoder();
	}

	private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder createEncoder() {
		int rounds = Integer.parseInt(systemSettingsStore.getSetting(SystemSettingsStore.BCRYPT_ROUNDS));
		log.debug("constructing bcrypt encoder using {} rounds", rounds); //$NON-NLS-1$
		return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(rounds);
	}

	@Override
	public String encodePassword(String rawPass, Object salt) {
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		String encPass = encoder.encode(rawPass);
		stopwatch.stop();
		if (log.isTraceEnabled()) {
			log.trace("time taken to encode password: {} ms", stopwatch.elapsedMillis()); //$NON-NLS-1$
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
			log.trace("time taken to verify password: {} ms", stopwatch.elapsedMillis()); //$NON-NLS-1$
		}
		return valid;
	}

	@Subscribe
	public void systemSettingsChanged(@SuppressWarnings("unused") SystemSettingsChangedEvent event) {
		encoder = createEncoder();
	}
}
