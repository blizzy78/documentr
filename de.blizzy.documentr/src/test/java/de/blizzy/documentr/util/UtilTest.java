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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import de.blizzy.documentr.AbstractDocumentrTest;

public class UtilTest extends AbstractDocumentrTest {
	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	@Test
	public void toRealPagePath() {
		assertEquals("x/y/z", Util.toRealPagePath("x,y,z")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("x/y/z", Util.toRealPagePath("x/y/z")); //$NON-NLS-1$ //$NON-NLS-2$
		assertNull(Util.toRealPagePath(null));
	}

	@Test
	public void toUrlPagePath() {
		assertEquals("x,y,z", Util.toUrlPagePath("x/y/z")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("x,y,z", Util.toUrlPagePath("x,y,z")); //$NON-NLS-1$ //$NON-NLS-2$
		assertNull(Util.toUrlPagePath(null));
	}

	@Test
	public void simplifyForUrl() {
		assertEquals("changes-for-1-0-x-code-name-wheatley-xl", //$NON-NLS-1$
				Util.simplifyForUrl("Changes For 1.0.x: Code Name \"Wheatley\" (XL)")); //$NON-NLS-1$
	}

	@Test
	public void join() {
		assertEquals("1, 2, 3", Util.join( //$NON-NLS-1$
				new Integer[] { 1, 2, 3 }, ", ")); //$NON-NLS-1$
		assertEquals("1, 2, 3", Util.join(Lists.newArrayList( //$NON-NLS-1$
				new Integer[] { 1, 2, 3 }), ", ")); //$NON-NLS-1$
		assertEquals("123", Util.join(123, ", ")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test(expected=None.class)
	public void deleteQuietly() throws IOException {
		File dir = tempDir.getRoot();
		File file = new File(dir, "test.txt"); //$NON-NLS-1$
		FileUtils.touch(file);
		// check that file actually exists
		assertTrue(file.isFile());

		Util.deleteQuietly(file);
		assertFalse(file.exists());

		Util.deleteQuietly(dir);
		assertFalse(dir.exists());
	}

	@Test(expected=None.class)
	public void deleteQuietlyMustAcceptNull() {
		Util.deleteQuietly(null);
	}

	@Test(expected=None.class)
	public void deleteQuietlyMustCatchExceptions() throws IOException {
		File dir = tempDir.getRoot();
		File file = new File(dir, "test.txt"); //$NON-NLS-1$
		FileOutputStream out = new FileOutputStream(file);

		Util.deleteQuietly(dir);

		Closeables.closeQuietly(out);
	}

	@Test
	public void toFile() {
		File dir = new File("."); //$NON-NLS-1$
		File result = Util.toFile(dir, "x/y/z"); //$NON-NLS-1$
		File expectedFile = new File(new File(new File(dir, "x"), "y"), "z"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(expectedFile, result);
	}
}
