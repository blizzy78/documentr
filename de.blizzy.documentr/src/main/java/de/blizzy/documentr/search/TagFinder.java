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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListeningExecutorService;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.UserStore;

class TagFinder {
	private SearcherManager searcherManager;
	private ListeningExecutorService taskExecutor;
	private UserStore userStore;
	private DocumentrPermissionEvaluator permissionEvaluator;

	TagFinder(SearcherManager searcherManager, ListeningExecutorService taskExecutor, UserStore userStore,
			DocumentrPermissionEvaluator permissionEvaluator) {

		this.searcherManager = searcherManager;
		this.taskExecutor = taskExecutor;
		this.userStore = userStore;
		this.permissionEvaluator = permissionEvaluator;

	}

	public Set<String> getAllTags(Authentication authentication) throws IOException, TimeoutException {
		IndexReader reader = null;
		IndexSearcher searcher = null;
		try {
			searcher = searcherManager.acquire();

			// no point in running the task asynchronously here
			GetVisibleDocIdsTask visibleDocIdsTask = new GetVisibleDocIdsTask(
					searcher, authentication, userStore, permissionEvaluator, taskExecutor);
			Bits visibleDocIds = visibleDocIdsTask.call();

			Set<String> tags = Sets.newHashSet();
			if (visibleDocIds.length() > 0) {
				reader = searcher.getIndexReader();
				Terms terms = MultiFields.getTerms(reader, PageIndex.TAG);
				if (terms != null) {
					TermsEnum termsEnum = terms.iterator(null);
					BytesRef ref;
					while ((ref = termsEnum.next()) != null) {
						DocsEnum docsEnum = termsEnum.docs(visibleDocIds, null, 0);
						if (docsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
							tags.add(ref.utf8ToString());
						}
					}
				}
			}
			return tags;
		} finally {
			if (searcher != null) {
				searcherManager.release(searcher);
			}
		}
	}
}
