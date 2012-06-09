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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component("userDetailsService")
public class DocumentrUserDetailsService implements UserDetailsService {
	@Autowired
	private UserStore userStore;
	
	@Override
	public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
		try {
			User user = userStore.getUser(loginName);
			List<RoleGrantedAuthority> userAuthorities = userStore.getUserAuthorities(loginName);

			Set<GrantedAuthority> authorities = Sets.newHashSet();
			for (RoleGrantedAuthority rga : userAuthorities) {
				authorities.addAll(userStore.toPermissionGrantedAuthorities(rga));
			}
			
			return new org.springframework.security.core.userdetails.User(
					loginName, user.getPassword(), !user.isDisabled(), true, true, true, authorities);
		} catch (UserNotFoundException e) {
			throw new UsernameNotFoundException(StringUtils.EMPTY);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	void setUserStore(UserStore userStore) {
		this.userStore = userStore;
	}
}
