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
