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

import java.io.IOException;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/** User details service that resolves users by their login names. */
@Component("loginNameUserDetailsService")
public class LoginNameUserDetailsService extends AbstractUserDetailsService {
	/** Returns the {@link User} that has the specified login name. */
	@Override
	User loadUser(String loginName) throws IOException {
		try {
			return getUserStore().getUser(loginName);
		} catch (UserNotFoundException e) {
			throw new UsernameNotFoundException("unknown user name: " + loginName, e); //$NON-NLS-1$
		}
	}
}
