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
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.collect.Sets;

/**
 * User details service that provides {@link UserDetails} with appropriate authorities.
 * It relies on subclasses to load up {@link User} instances.
 */
abstract class AbstractUserDetailsService implements UserDetailsService {
	@Autowired
	@Getter(AccessLevel.PACKAGE)
	private UserStore userStore;

	/**
	 * Returns {@link UserDetails} according to a specified login name.
	 * This method invokes {@link #loadUser} and uses the {@link User} instance to
	 * construct its result.
	 */
	@Override
	public UserDetails loadUserByUsername(String loginName) {
		if (loginName.equals(UserStore.ANONYMOUS_USER_LOGIN_NAME)) {
			throw new UsernameNotFoundException("user not found: " + loginName); //$NON-NLS-1$
		}

		try {
			User user = loadUser(loginName);
			loginName = user.getLoginName();

			List<RoleGrantedAuthority> userAuthorities = userStore.getUserAuthorities(loginName);

			Set<GrantedAuthority> authorities = Sets.newHashSet();
			for (RoleGrantedAuthority rga : userAuthorities) {
				authorities.addAll(userStore.toPermissionGrantedAuthorities(rga));
			}

			return new org.springframework.security.core.userdetails.User(
					loginName, user.getPassword(), !user.isDisabled(), true, true, true, authorities);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/** Returns the {@link User} that has the specified login name. */
	abstract User loadUser(String loginName) throws IOException;
}
