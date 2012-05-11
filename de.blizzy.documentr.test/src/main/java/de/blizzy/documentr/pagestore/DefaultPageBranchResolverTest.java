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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.mockito.InOrder;

public class DefaultPageBranchResolverTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$

	@Test
	public void resolvePageBranch() throws IOException {
		PageStore pageStore = mock(PageStore.class);
		when(pageStore.listPagePaths(anyString(), anyString())).thenReturn(Collections.<String>emptyList());
		
		DefaultPageBranchResolver resolver = new DefaultPageBranchResolver();
		resolver.setPageStore(pageStore);
		
		resolver.resolvePageBranch(PROJECT, "1.2.3", PAGE); //$NON-NLS-1$

		InOrder inOrder = inOrder(pageStore);
		inOrder.verify(pageStore).listPagePaths(PROJECT, "1.2.3"); //$NON-NLS-1$
		inOrder.verify(pageStore).listPagePaths(PROJECT, "1.2.x"); //$NON-NLS-1$
		inOrder.verify(pageStore).listPagePaths(PROJECT, "1.2"); //$NON-NLS-1$
		inOrder.verify(pageStore).listPagePaths(PROJECT, "1.x"); //$NON-NLS-1$
		inOrder.verify(pageStore).listPagePaths(PROJECT, "1"); //$NON-NLS-1$
		inOrder.verifyNoMoreInteractions();
	}
}
