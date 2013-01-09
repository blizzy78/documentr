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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;

public class GetVisibleBranchDocIdsTaskTest extends AbstractDocumentrTest {
	@Mock
	private IndexSearcher searcher;
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Mock
	private Authentication authentication;
	@InjectMocks
	private GetVisibleBranchDocIdsTask task;

	@Test
	public void call() throws IOException {
		List<String> branches = Lists.newArrayList("project1/branch1", "project1/branch2", "project2/branch"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(permissionEvaluator.getBranchesForPermission(authentication, Permission.VIEW))
			.thenReturn(Sets.newHashSet(branches));

		BooleanQuery query = new BooleanQuery();
		for (String projectAndBranch : branches) {
			BooleanQuery q = new BooleanQuery();
			String projectName = StringUtils.substringBefore(projectAndBranch, "/"); //$NON-NLS-1$
			String branchName = StringUtils.substringAfter(projectAndBranch, "/"); //$NON-NLS-1$
			q.add(new TermQuery(new Term(PageIndex.PROJECT, projectName)), BooleanClause.Occur.MUST);
			q.add(new TermQuery(new Term(PageIndex.BRANCH, branchName)), BooleanClause.Occur.MUST);
			query.add(q, BooleanClause.Occur.SHOULD);
		}

		task.call();

		verify(searcher).search(eq(query), Matchers.any(AllDocIdsCollector.class));
	}
}
