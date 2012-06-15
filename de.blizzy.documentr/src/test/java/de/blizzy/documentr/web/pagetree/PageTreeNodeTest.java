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
package de.blizzy.documentr.web.pagetree;

import static org.junit.Assert.*;

import org.junit.Test;

public class PageTreeNodeTest {
	@Test
	public void getProjectName() {
		PageTreeNode node = new PageTreeNode("project", "branch", "path", "title"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals("project", node.getProjectName()); //$NON-NLS-1$
	}

	@Test
	public void getBranchName() {
		PageTreeNode node = new PageTreeNode("project", "branch", "path", "title"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals("branch", node.getBranchName()); //$NON-NLS-1$
	}

	@Test
	public void getPath() {
		PageTreeNode node = new PageTreeNode("project", "branch", "path", "title"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals("path", node.getPath()); //$NON-NLS-1$
	}
	
	@Test
	public void getTitle() {
		PageTreeNode node = new PageTreeNode("project", "branch", "path", "title"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals("title", node.getTitle()); //$NON-NLS-1$
	}
	
	@Test
	public void setAndGetHasBranchPermissions() {
		PageTreeNode node = new PageTreeNode("project", "branch", "path", "title"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		node.setHasBranchPermissions(true);
		assertTrue(node.isHasBranchPermissions());
		node.setHasBranchPermissions(false);
		assertFalse(node.isHasBranchPermissions());
	}
}
