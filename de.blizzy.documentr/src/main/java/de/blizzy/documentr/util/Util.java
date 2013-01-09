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
package de.blizzy.documentr.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import de.blizzy.documentr.validation.PagePathValidator;

/** Generic utility methods. */
@Slf4j
public final class Util {
	private static final String CHARS_TO_DASH = " .,:;/()[]<>"; //$NON-NLS-1$

	private Util() {}

	/** Converts a page path from URL format to real format. */
	public static String toRealPagePath(String pagePath) {
		return (pagePath != null) ? pagePath.replace(',', '/') : null;
	}

	/** Converts a page path from real format to URL format. */
	public static String toUrlPagePath(String pagePath) {
		return (pagePath != null) ? pagePath.replace('/', ',') : null;
	}

	/**
	 * Converts a text so that it can be used as a URL component. For example, the text
	 * &quot;<code>My Funny Valentine</code>&quot; will be converted to
	 * &quot;<code>my-funny-valentine</code>&quot;. This method is useful to convert
	 * page titles to page path components.
	 */
	public static String simplifyForUrl(String text) {
		PagePathValidator validator = new PagePathValidator();
		StringBuilder buf = new StringBuilder();
		int len = text.length();
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			if (StringUtils.contains(CHARS_TO_DASH, c)) {
				buf.append("-"); //$NON-NLS-1$
			} else {
				buf.append(c);
				if (!validator.isValid(buf.toString(), null)) {
					buf.deleteCharAt(buf.length() - 1);
				}
			}
		}

		String name = buf.toString()
				.replaceAll("--", "-") //$NON-NLS-1$ //$NON-NLS-2$
				.replaceAll("^-", StringUtils.EMPTY) //$NON-NLS-1$
				.replaceAll("-$", StringUtils.EMPTY) //$NON-NLS-1$
				.toLowerCase();
		return name;
	}

	/**
	 * <p>Joins a list of objects into a string using a delimiter. This works for different types
	 * of <code>o</code>:</p>
	 *
	 * <ul>
	 *   <li>If <code>o</code> is a {@link Collection}, all elements are joined together.</li>
	 *   <li>If <code>o</code> is an array, all elements are joined together.</li>
	 *   <li>Otherwise, <code>o</code> is used directly.</li>
	 * </ul>
	 *
	 * <p>For each element in <code>o</code> its respective {@link Object#toString()} method is invoked.</p>
	 */
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

	/** Deletes a file ignoring exceptions. If <code>f</code> is a directory, it is deleted recursively. */
	public static void deleteQuietly(File f) {
		if ((f != null) && f.exists()) {
			try {
				FileUtils.forceDelete(f);
			} catch (IOException e) {
				log.warn(StringUtils.EMPTY, e);
			}
		}
	}

	public static RuntimeException toRuntimeException(Throwable t) {
		if (t instanceof RuntimeException) {
			return (RuntimeException) t;
		} else {
			return new RuntimeException(t);
		}
	}

	public static File toFile(File baseDir, String path) {
		File result = baseDir;
		for (String part : path.split("/")) { //$NON-NLS-1$
			result = new File(result, part);
		}
		return result;
	}
}
