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
package de.blizzy.documentr.web.page;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PageFormTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	private static final String PARENT = "parent"; //$NON-NLS-1$
	private static final String TITLE = "title"; //$NON-NLS-1$
	private static final String TEXT = "text"; //$NON-NLS-1$
	private static final String VIEW_ROLE = "viewRole"; //$NON-NLS-1$
	
	private PageForm form;

	@Before
	public void setUp() {
		form = new PageForm(PROJECT, BRANCH, PAGE, PARENT, TITLE, TEXT, VIEW_ROLE);
	}
	
	@Test
	public void getProjectName() {
		assertEquals(PROJECT, form.getProjectName());
	}
	
	@Test
	public void getBranchName() {
		assertEquals(BRANCH, form.getBranchName());
	}
	
	@Test
	public void getPath() {
		assertEquals(PAGE, form.getPath());
	}
	
	@Test
	public void getParentPagePath() {
		assertEquals(PARENT, form.getParentPagePath());
	}

	@Test
	public void getTitle() {
		assertEquals(TITLE, form.getTitle());
	}
	
	@Test
	public void getText() {
		assertEquals(TEXT, form.getText());
	}
	
	@Test
	public void getViewRestrictionRole() {
		assertEquals(VIEW_ROLE, form.getViewRestrictionRole());
	}
}
