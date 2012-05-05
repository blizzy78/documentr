package de.blizzy.documentr.access;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
	@Autowired
	private UserStore userStore;
	
	@Override
	public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
		try {
			User user = userStore.getUser(loginName);
			Set<GrantedAuthority> authorities = new HashSet<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_USER")); //$NON-NLS-1$
			if (user.isAdmin()) {
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); //$NON-NLS-1$
			}
			return new org.springframework.security.core.userdetails.User(
					loginName, user.getPassword(), !user.isDisabled(), true, true, true, authorities);
		} catch (UsernameNotFoundException e) {
			throw new UsernameNotFoundException(StringUtils.EMPTY);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
