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
package de.blizzy.documentr;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.common.collect.Lists;

import de.blizzy.documentr.web.page.PagePathValidator;

public final class Util {
	private static final String AUTHENTICATION_CREATION_TIME_PREFIX = "authenticationCreationTime:"; //$NON-NLS-1$
	
	private Util() {}

	public static String toRealPagePath(String pagePath) {
		return (pagePath != null) ? pagePath.replace(',', '/') : null;
	}

	public static String toURLPagePath(String pagePath) {
		return (pagePath != null) ? pagePath.replace('/', ',') : null;
	}

	public static String simplifyForURL(String title) {
		PagePathValidator validator = new PagePathValidator();
		StringBuilder buf = new StringBuilder();
		int len = title.length();
		for (int i = 0; i < len; i++) {
			char c = title.charAt(i);
			switch (c) {
				case ' ':
				case '.':
				case ',':
				case '/':
				case ':':
				case '(':
				case ')':
				case '[':
				case ']':
				case '<':
				case '>':
					buf.append("-"); //$NON-NLS-1$
					break;
				
				default:
					buf.append(c);
					if (!validator.isValid(buf.toString(), null)) {
						buf.deleteCharAt(buf.length() - 1);
					}
					break;
			}
		}

		String name = buf.toString()
				.replaceAll("--", "-") //$NON-NLS-1$ //$NON-NLS-2$
				.replaceAll("^-", StringUtils.EMPTY) //$NON-NLS-1$
				.replaceAll("-$", StringUtils.EMPTY) //$NON-NLS-1$
				.toLowerCase();
		return name;
	}

	public static String join(Object o, String delimiter) {
		Collection<?> c;
		if (o instanceof Collection) {
			c = (Collection<?>) o;
		} else if (o.getClass().isArray()) {
			c = Lists.newArrayList((Object[]) o);
		} else {
			c = Collections.singleton(o);
		}
		return StringUtils.join(c, delimiter);
	}
	
	public static byte[] toBytes(String s) {
		try {
			return s.getBytes(DocumentrConstants.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String fromBytes(byte[] b) {
		try {
			return StringUtils.toString(b, DocumentrConstants.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static long getAuthenticationCreationTime(Authentication authentication) {
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		for (GrantedAuthority authority : authorities) {
			if (authority instanceof SimpleGrantedAuthority) {
				String auth = authority.getAuthority();
				if (auth.startsWith(AUTHENTICATION_CREATION_TIME_PREFIX)) {
					return Long.parseLong(StringUtils.substringAfter(auth, AUTHENTICATION_CREATION_TIME_PREFIX));
				}
			}
		}
		return -1;
	}
	
	public static GrantedAuthority createAuthenticationCreationTime(long time) {
		return new SimpleGrantedAuthority(AUTHENTICATION_CREATION_TIME_PREFIX + String.valueOf(time));
	}
}
