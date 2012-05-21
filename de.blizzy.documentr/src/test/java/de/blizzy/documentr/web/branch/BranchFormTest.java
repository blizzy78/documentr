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
package de.blizzy.documentr.web.branch;

import static org.junit.Assert.*;

import org.junit.Test;

public class BranchFormTest {
	@Test
	public void getProjectName() {
		BranchForm form = new BranchForm("project", "branch", "startingBranch"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("project", form.getProjectName()); //$NON-NLS-1$
	}

	@Test
	public void getName() {
		BranchForm form = new BranchForm("project", "branch", "startingBranch"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("branch", form.getName()); //$NON-NLS-1$
	}

	@Test
	public void getStartingBranch() {
		BranchForm form = new BranchForm("project", "branch", "startingBranch"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("startingBranch", form.getStartingBranch()); //$NON-NLS-1$
	}
}
