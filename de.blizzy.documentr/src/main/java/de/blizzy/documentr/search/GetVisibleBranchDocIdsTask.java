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
package de.blizzy.documentr.search;

import java.io.IOException;
import java.util.BitSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;

class GetVisibleBranchDocIdsTask implements Callable<BitSet> {
	private Set<String> branches;
	private IndexSearcher searcher;

	GetVisibleBranchDocIdsTask(Set<String> branches, IndexSearcher searcher) {
		this.branches = branches;
		this.searcher = searcher;
	}

	@Override
	public BitSet call() throws IOException {
		BooleanQuery allBranchesQuery = new BooleanQuery();
		for (String projectAndBranch : branches) {
			String projectName = StringUtils.substringBefore(projectAndBranch, "/"); //$NON-NLS-1$
			String branchName = StringUtils.substringAfter(projectAndBranch, "/"); //$NON-NLS-1$
			TermQuery projectQuery = new TermQuery(new Term(PageIndex.PROJECT, projectName));
			TermQuery branchQuery = new TermQuery(new Term(PageIndex.BRANCH, branchName));
			BooleanQuery projectAndBranchQuery = new BooleanQuery();
			projectAndBranchQuery.add(projectQuery, BooleanClause.Occur.MUST);
			projectAndBranchQuery.add(branchQuery, BooleanClause.Occur.MUST);
			allBranchesQuery.add(projectAndBranchQuery, BooleanClause.Occur.SHOULD);
		}
		AbstractDocIdsCollector collector = new AllDocIdsCollector();
		searcher.search(allBranchesQuery, collector);
		return collector.getDocIds();
	}
}
