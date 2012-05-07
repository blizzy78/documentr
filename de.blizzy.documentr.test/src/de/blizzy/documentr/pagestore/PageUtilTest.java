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
package de.blizzy.documentr.pagestore;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.Lists;

import de.blizzy.documentr.TestUtil;

public class PageUtilTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$

	@Test
	public void getPagePathHierarchy() throws IOException {
		Page page1 = TestUtil.createRandomPage(null);
		Page page2 = TestUtil.createRandomPage("page1"); //$NON-NLS-1$
		Page page3 = TestUtil.createRandomPage("page1/page2"); //$NON-NLS-1$
		PageStore pageStore = mock(PageStore.class);
		when(pageStore.getPage(PROJECT, BRANCH, "page1")).thenReturn(page1); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, "page1/page2")).thenReturn(page2); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, "page1/page2/page3")).thenReturn(page3); //$NON-NLS-1$
		
		assertEquals(Lists.newArrayList("page1", "page1/page2", "page1/page2/page3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				PageUtil.getPagePathHierarchy(PROJECT, BRANCH, "page1/page2/page3", pageStore)); //$NON-NLS-1$
	}
}
