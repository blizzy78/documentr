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
import java.util.Collections;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.DocIdBitSet;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Sets;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;

class PagePermissionFilter extends Filter {
	private Authentication authentication;
	private Permission permission;
	private DocumentrPermissionEvaluator permissionEvaluator;

	PagePermissionFilter(Authentication authentication, Permission permission, DocumentrPermissionEvaluator permissionEvaluator) {
		this.authentication = authentication;
		this.permission = permission;
		this.permissionEvaluator = permissionEvaluator;
	}
	
	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		int numDocs = reader.numDocs();
		FieldSelector selector = new SetBasedFieldSelector(
				Sets.newHashSet("project", "branch", "path"), Collections.<String>emptySet()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BitSet bitSet = new BitSet(numDocs);
		for (int i = 0; i < numDocs; i++) {
			Document doc = reader.document(i, selector);
			String projectName = doc.get("project"); //$NON-NLS-1$
			String branchName = doc.get("branch"); //$NON-NLS-1$
			String path = doc.get("path"); //$NON-NLS-1$
			if (permissionEvaluator.hasPagePermission(authentication, projectName, branchName, path, permission)) {
				bitSet.set(i);
			}
		}
		return new DocIdBitSet(bitSet);
	}
}
