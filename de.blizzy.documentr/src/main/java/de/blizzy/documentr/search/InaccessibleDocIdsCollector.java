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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Scorer;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Sets;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;

class InaccessibleDocIdsCollector extends AbstractDocIdsCollector {
	private static final Set<String> FIELDS = Sets.newHashSet(PageIndex.PROJECT, PageIndex.BRANCH, PageIndex.PATH);

	private Permission permission;
	private Authentication authentication;
	private DocumentrPermissionEvaluator permissionEvaluator;
	private AtomicReader reader;
	private int docBase;

	InaccessibleDocIdsCollector(Permission permission, Authentication authentication,
			DocumentrPermissionEvaluator permissionEvaluator) {

		this.permission = permission;
		this.authentication = authentication;
		this.permissionEvaluator = permissionEvaluator;
	}

	@Override
	public void setScorer(Scorer scorer) {
	}

	@Override
	public void setNextReader(AtomicReaderContext context) {
		reader = context.reader();
		docBase = context.docBase;
	}

	@Override
	public void collect(int doc) throws IOException {
		Document document = reader.document(doc, FIELDS);
		String projectName = document.get(PageIndex.PROJECT);
		String branchName = document.get(PageIndex.BRANCH);
		String path = document.get(PageIndex.PATH);
		if (!permissionEvaluator.hasPagePermission(authentication, projectName, branchName, path, permission)) {
			getDocIds().set(docBase + doc);
		}
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}
}
