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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;

public class PagePermissionFilterTest extends AbstractDocumentrTest {
	@Mock
	private Authentication authentication;
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Mock
	private IndexReader indexReader;
	private PagePermissionFilter filter;

	@Before
	public void setUp() {
		filter = new PagePermissionFilter(authentication, Permission.VIEW, permissionEvaluator);
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void getDocIdSet() throws IOException {
		when(indexReader.numDocs()).thenReturn(2);
		when(indexReader.document(eq(0), Matchers.<FieldSelector>any()))
			.thenReturn(createDocument("project", "branch1", "home")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(indexReader.document(eq(1), Matchers.<FieldSelector>any()))
			.thenReturn(createDocument("project", "branch2", "home")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		when(permissionEvaluator.hasPagePermission(
				authentication, "project", "branch1", "home", Permission.VIEW)).thenReturn(false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(permissionEvaluator.hasPagePermission(
				authentication, "project", "branch2", "home", Permission.VIEW)).thenReturn(true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		DocIdSet docIdSet = filter.getDocIdSet(indexReader);
		Set<Integer> docIds = toSet(docIdSet);
		assertEquals(Sets.newHashSet(Integer.valueOf(1)), docIds);
	}
	
	private Set<Integer> toSet(DocIdSet docIdSet) throws IOException {
		Set<Integer> result = Sets.newHashSet();
		DocIdSetIterator iter = docIdSet.iterator();
		int docId;
		while ((docId = iter.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
			result.add(Integer.valueOf(docId));
		}
		return result;
	}
	
	private Document createDocument(String projectName, String branchName, String path) {
		Document doc = new Document();
		doc.add(new Field("project", projectName, Store.YES, Index.NOT_ANALYZED)); //$NON-NLS-1$
		doc.add(new Field("branch", branchName, Store.YES, Index.NOT_ANALYZED)); //$NON-NLS-1$
		doc.add(new Field("path", path, Store.YES, Index.NOT_ANALYZED)); //$NON-NLS-1$
		return doc;
	}
}
