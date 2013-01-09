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
package de.blizzy.documentr;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.TestPageUtil;

public final class TestUtil {
	private TestUtil() {}

	public static Page createRandomPage() {
		return createRandomPage(null);
	}

	public static Page createRandomPage(String parentPagePath) {
		Page page = Page.fromText(String.valueOf(Math.random() * Long.MAX_VALUE),
				String.valueOf(Math.random() * Long.MAX_VALUE));
		TestPageUtil.setParentPagePath(page, parentPagePath);
		return page;
	}

	public static <T> void assertEqualsContract(T equal1, T equal2, T equal3, T different) {
		assertNotSame(equal1, equal2);
		assertNotSame(equal1, equal3);
		assertNotSame(equal2, equal3);
		Class<? extends Object> clazz = equal1.getClass();
		assertEquals(clazz, equal2.getClass());
		assertEquals(clazz, equal3.getClass());
		assertEquals(clazz, different.getClass());

		// same object
		assertTrue(equal1.equals(equal1));

		// reflexive
		assertTrue(equal1.equals(equal2));

		// symmetric
		if (equal1.equals(equal2)) {
			assertTrue(equal2.equals(equal1));
		}

		// transitive
		if (equal1.equals(equal2) && equal2.equals(equal3)) {
			assertTrue(equal1.equals(equal3));
		}

		// consistent
		if (equal1.equals(equal2)) {
			assertTrue(equal1.equals(equal2));
		} else {
			assertFalse(equal1.equals(equal2));
		}

		// null
		assertFalse(equal1.equals(null));

		// difference
		assertFalse(equal1.equals(different));
		assertFalse(different.equals(equal1));

		// subclass
		T subclassed = spy(equal2);
		assertFalse(subclassed.getClass().equals(clazz));
		assertFalse(equal1.equals(subclassed));
		assertFalse(subclassed.equals(equal1));
	}

	public static <T> void assertHashCodeContract(T equal1, T equal2) {
		assertNotSame(equal1, equal2);
		assertEquals(equal1, equal2);
		Class<? extends Object> clazz = equal1.getClass();
		assertEquals(clazz, equal2.getClass());

		// consistent
		int hashCode = equal1.hashCode();
		assertEquals(hashCode, equal1.hashCode());

		// equal hash code for equal objects
		assertEquals(equal1.hashCode(), equal2.hashCode());
	}

	public static String removeViewPrefix(String view) {
		return view.contains(":") ? StringUtils.substringAfter(view, ":") : view; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void assertRedirect(String view) {
		assertTrue(view.startsWith("redirect:")); //$NON-NLS-1$
	}

	public static void assertForward(String view) {
		assertTrue(view.startsWith("forward:")); //$NON-NLS-1$
	}

	public static void assertSecondsAgo(Date d, int seconds) {
		long time = d.getTime();
		long now = System.currentTimeMillis();
		assertTrue((now - time) <= (seconds * 1000L));
	}

	public static SecurityContext createSecurityContext(Authentication authentication) {
		SecurityContextImpl context = new SecurityContextImpl();
		context.setAuthentication(authentication);
		return context;
	}

	public static String removeTextRange(String html) {
		return html.replaceAll(" data-text-range=\"[0-9]+,[0-9]+\"", StringUtils.EMPTY); //$NON-NLS-1$
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public static void assertClean(Repository repo) throws IOException {
		assertTrue(Git.wrap(repo).status().call().isClean());
	}

	public static void assertRE(String expectedRegExp, String actual) {
		@SuppressWarnings("nls")
		String msg = "text does not match regular expression:\n" +
				"regular expression:\n" +
				expectedRegExp + "\n" +
				"text:\n" +
				actual;
		assertTrue(msg, Pattern.compile(expectedRegExp, Pattern.DOTALL).matcher(actual).matches());
	}
}
