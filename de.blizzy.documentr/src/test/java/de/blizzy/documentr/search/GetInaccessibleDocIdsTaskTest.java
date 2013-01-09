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
package de.blizzy.documentr.search;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import com.google.common.collect.Lists;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.UserStore;

public class GetInaccessibleDocIdsTaskTest extends AbstractDocumentrTest {
	@Mock
	private IndexSearcher searcher;
	@Mock
	private UserStore userStore;
	@InjectMocks
	private GetInaccessibleDocIdsTask task;

	@Test
	public void call() throws IOException {
		when(userStore.listRoles()).thenReturn(Lists.newArrayList("reader", "editor")); //$NON-NLS-1$ //$NON-NLS-2$

		BooleanQuery query = new BooleanQuery();
		query.add(new TermQuery(new Term(PageIndex.VIEW_RESTRICTION_ROLE, "reader")), BooleanClause.Occur.SHOULD); //$NON-NLS-1$
		query.add(new TermQuery(new Term(PageIndex.VIEW_RESTRICTION_ROLE, "editor")), BooleanClause.Occur.SHOULD); //$NON-NLS-1$

		task.call();

		verify(searcher).search(eq(query), Matchers.any(InaccessibleDocIdsCollector.class));
	}
}
