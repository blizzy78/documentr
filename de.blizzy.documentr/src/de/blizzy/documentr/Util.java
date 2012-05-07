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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.web.page.PagePathValidator;


public final class Util {
	private Util() {}

	public static String toRealPagePath(String pagePath) {
		return (pagePath != null) ? pagePath.replace(',', '/') : null;
	}

	public static String toURLPagePath(String pagePath) {
		return (pagePath != null) ? pagePath.replace('/', ',') : null;
	}

	public static String generatePageName(String title) {
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
			c = Arrays.asList((Object[]) o);
		} else {
			c = Collections.singleton(o);
		}
		return StringUtils.join(c, delimiter);
	}
}
