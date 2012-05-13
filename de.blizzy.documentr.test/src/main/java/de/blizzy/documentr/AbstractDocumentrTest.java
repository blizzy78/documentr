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

import static junit.framework.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;

import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryUtil;

public abstract class AbstractDocumentrTest {
	private static final String TEMP_DIR_PATH = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
	
	private Set<File> tempDirs = new HashSet<>();
	private Set<ILockedRepository> repositories = new HashSet<>();
	
	@Before
	public void setUpTempDirs() {
		assertTrue(StringUtils.isNotBlank(TEMP_DIR_PATH));
	}

	@After
	public void deleteTempDirs() {
		for (ILockedRepository repo : repositories) {
			RepositoryUtil.closeQuietly(repo);
		}
		repositories.clear();
		
		for (File dir : tempDirs) {
			try {
				FileUtils.forceDelete(dir);
			} catch (IOException e) {
				System.out.println(getClass().getName());
			}
		}
		tempDirs.clear();
	}
	
	protected File createTempDir() {
		Path tempDir = new File(TEMP_DIR_PATH).toPath();
		try {
			Path dir = Files.createTempDirectory(tempDir, "documentr-tests-"); //$NON-NLS-1$
			File f = dir.toFile();
			tempDirs.add(f);
			return f;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void register(ILockedRepository repository) {
		repositories.add(repository);
	}
}
