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
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.access.UserStore;

class GetInaccessibleDocIdsTask implements Callable<BitSet> {
	private IndexSearcher searcher;
	private Permission permission;
	private Authentication authentication;
	private UserStore userStore;
	private DocumentrPermissionEvaluator permissionEvaluator;

	GetInaccessibleDocIdsTask(IndexSearcher searcher, Permission permission, Authentication authentication,
			UserStore userStore, DocumentrPermissionEvaluator permissionEvaluator) {

		this.searcher = searcher;
		this.permission = permission;
		this.authentication = authentication;
		this.userStore = userStore;
		this.permissionEvaluator = permissionEvaluator;
	}

	@Override
	public BitSet call() throws IOException {
		List<String> roles = userStore.listRoles();
		BooleanQuery allRolesQuery = new BooleanQuery();
		for (String role : roles) {
			TermQuery roleQuery = new TermQuery(new Term(PageIndex.VIEW_RESTRICTION_ROLE, role));
			allRolesQuery.add(roleQuery, BooleanClause.Occur.SHOULD);
		}
		AbstractDocIdsCollector collector = new InaccessibleDocIdsCollector(
				permission, authentication, permissionEvaluator);
		searcher.search(allRolesQuery, collector);
		return collector.getDocIds();
	}
}
