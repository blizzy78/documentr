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

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

/** {@link Authentication} utility methods. */
public final class AuthenticationUtil {
	private static final String AUTHENTICATION_CREATION_TIME = "authenticationCreationTime"; //$NON-NLS-1$

	private AuthenticationUtil() {}

	/** Stores the creation time of the current user's {@link Authentication} into their session. */
	public static void setAuthenticationCreationTime(HttpSession session, long time) {
		session.setAttribute(AUTHENTICATION_CREATION_TIME, time);
	}

	/** Retrieves the creation time of the current user's {@link Authentication} from their session. */
	public static long getAuthenticationCreationTime(HttpSession session) {
		Long time = (Long) session.getAttribute(AUTHENTICATION_CREATION_TIME);
		if (time != null) {
			// shave off milliseconds
			long seconds = TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);
			return TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);
		} else {
			return 0;
		}
	}
}
