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

import com.google.common.io.Closeables;

public class AllDocIdsCollectorTest {
	private Directory directory;
	private IndexReader reader;

	@Before
	public void setUp() throws IOException {
		directory = new RAMDirectory();

		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
		IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(directory, writerConfig);
		writer.addDocument(createDocument());
		writer.addDocument(createDocument());
		writer.addDocument(createDocument());
		writer.commit();
		writer.close(true);

		reader = DirectoryReader.open(directory);
	}

	@After
	public void tearDown() {
		Closeables.closeQuietly(reader);
		Closeables.closeQuietly(directory);
	}

	@Test
	public void collect() throws IOException {
		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = new TermQuery(new Term("text", "text")); //$NON-NLS-1$ //$NON-NLS-2$
		AllDocIdsCollector collector = new AllDocIdsCollector();

		searcher.search(query, collector);
		BitSet docIds = collector.getDocIds();
		assertEquals(3, docIds.cardinality());
		for (int i = 0; i <= 2; i++) {
			assertTrue(docIds.get(i));
		}
	}

	private Document createDocument() {
		Document doc = new Document();
		doc.add(new StringField("text", "text", Store.NO)); //$NON-NLS-1$ //$NON-NLS-2$
		return doc;
	}
}
