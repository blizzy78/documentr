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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;

import com.google.common.io.Closeables;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;

public class InaccessibleDocIdsCollectorTest extends AbstractDocumentrTest {
	@Mock
	private Authentication authentication;
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	private IndexReader reader;
	private Directory directory;
	private InaccessibleDocIdsCollector collector;

	@Before
	public void setUp() throws IOException {
		directory = new RAMDirectory();

		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
		IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(directory, writerConfig);
		writer.addDocument(createDocument("project", "branch1", "home")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writer.addDocument(createDocument("project", "branch2", "home")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writer.commit();
		writer.close(true);

		reader = DirectoryReader.open(directory);

		collector = new InaccessibleDocIdsCollector(Permission.VIEW, authentication, permissionEvaluator);
	}

	@After
	public void tearDown() {
		Closeables.closeQuietly(reader);
		Closeables.closeQuietly(directory);
	}

	@Test
	public void getDocIdSet() throws IOException {
		when(permissionEvaluator.hasPagePermission(authentication,
				"project", "branch2", "home", Permission.VIEW)).thenReturn(true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = new TermQuery(new Term("path", "home")); //$NON-NLS-1$ //$NON-NLS-2$
		searcher.search(query, collector);
		BitSet docIds = collector.getDocIds();
		assertEquals(1, docIds.cardinality());
		assertTrue(docIds.get(0));
		assertFalse(docIds.get(1));
	}

	private Document createDocument(String projectName, String branchName, String path) {
		Document doc = new Document();
		doc.add(new StringField(PageIndex.PROJECT, projectName, Store.YES));
		doc.add(new StringField(PageIndex.BRANCH, branchName, Store.YES));
		doc.add(new StringField(PageIndex.PATH, path, Store.YES));
		return doc;
	}
}
