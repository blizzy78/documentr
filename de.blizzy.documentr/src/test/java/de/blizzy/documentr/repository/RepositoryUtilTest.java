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
package de.blizzy.documentr.repository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.eclipse.jgit.lib.Repository;
import org.junit.Test;
import org.junit.Test.None;

public class RepositoryUtilTest {
	@Test(expected=None.class)
	public void closeRepositoryQuietlyWithRepository() {
		Repository repo = mock(Repository.class);
		RepositoryUtil.closeQuietly(repo);
		verify(repo).close();
	}
	
	@Test(expected=None.class)
	public void closeRepositoryQuietlyMustAcceptNull() {
		RepositoryUtil.closeQuietly((Repository) null);
	}

	@Test(expected=None.class)
	public void closeRepositoryQuietlyMustCatchExceptions() {
		Repository repo = mock(Repository.class);
		doThrow(new RuntimeException()).when(repo).close();
		
		RepositoryUtil.closeQuietly(repo);
	}
	
	@Test(expected=None.class)
	public void closeRepositoryQuietlyWithLockedRepository() {
		ILockedRepository repo = mock(ILockedRepository.class);
		RepositoryUtil.closeQuietly(repo);
		verify(repo).close();
	}

	@Test(expected=None.class)
	public void closeLockedRepositoryQuietlyMustAcceptNull() {
		RepositoryUtil.closeQuietly((ILockedRepository) null);
	}

	@Test(expected=None.class)
	public void closeLockedRepositoryQuietlyMustCatchExceptions() {
		ILockedRepository repo = mock(ILockedRepository.class);
		doThrow(new RuntimeException()).when(repo).close();
		
		RepositoryUtil.closeQuietly(repo);
	}
	
	@Test
	public void getWorkingDir() {
		Repository repo = mock(Repository.class);
		File dir = new File("."); //$NON-NLS-1$
		when(repo.getDirectory()).thenReturn(dir);
		assertEquals(dir.getParentFile(), RepositoryUtil.getWorkingDir(repo));
	}
}
