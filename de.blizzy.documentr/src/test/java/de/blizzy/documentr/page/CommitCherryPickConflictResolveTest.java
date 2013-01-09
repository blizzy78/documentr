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
package de.blizzy.documentr.page;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CommitCherryPickConflictResolveTest {
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String COMMIT = "commit"; //$NON-NLS-1$
	private static final String TEXT = "text"; //$NON-NLS-1$

	private CommitCherryPickConflictResolve resolve;

	@Before
	public void setUp() {
		resolve = new CommitCherryPickConflictResolve(BRANCH, COMMIT, TEXT);
	}

	@Test
	public void isApplicable() {
		assertTrue(resolve.isApplicable(BRANCH, COMMIT));
		assertFalse(resolve.isApplicable("branch2", COMMIT)); //$NON-NLS-1$
		assertFalse(resolve.isApplicable(BRANCH, "commit2")); //$NON-NLS-1$
	}

	@Test
	@SuppressWarnings("nls")
	public void testEquals() {
		assertEqualsContract(
				new CommitCherryPickConflictResolve("branch", "commit", "text"),
				new CommitCherryPickConflictResolve("branch", "commit", "text"),
				new CommitCherryPickConflictResolve("branch", "commit", "text"),
				new CommitCherryPickConflictResolve("branch", "commit", "text2"));
	}

	@Test
	@SuppressWarnings("nls")
	public void testHashCode() {
		assertHashCodeContract(
				new CommitCherryPickConflictResolve("branch", "commit", "text"),
				new CommitCherryPickConflictResolve("branch", "commit", "text"));
	}
}
