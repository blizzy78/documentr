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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import de.blizzy.documentr.AbstractDocumentrTest;

public class GetSearchHitTaskTest extends AbstractDocumentrTest {
	private IndexReader reader;
	private Directory directory;
	private GetSearchHitTask task;

	@Before
	public void setUp() throws IOException {
		directory = new RAMDirectory();

		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
		IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(directory, writerConfig);
		writer.addDocument(createDocument("project", "branch", "home", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new String[] { "tag1", "tag2" }, //$NON-NLS-1$ //$NON-NLS-2$
				"title", "some text")); //$NON-NLS-1$ //$NON-NLS-2$
		writer.commit();
		writer.close(true);

		reader = DirectoryReader.open(directory);

		Query query = new TermQuery(new Term("text", "some")); //$NON-NLS-1$ //$NON-NLS-2$
		task = new GetSearchHitTask(query, reader, 0, analyzer);
	}

	@After
	public void tearDown() {
		Closeables.closeQuietly(reader);
		Closeables.closeQuietly(directory);
	}

	@Test
	public void call() throws IOException {
		SearchHit hit = task.call();
		assertEquals("project", hit.getProjectName()); //$NON-NLS-1$
		assertEquals("branch", hit.getBranchName()); //$NON-NLS-1$
		assertEquals("home", hit.getPath()); //$NON-NLS-1$
		assertEquals("title", hit.getTitle()); //$NON-NLS-1$
		assertEquals("<strong>some</strong> text", hit.getTextHtml()); //$NON-NLS-1$
		assertEquals(Lists.newArrayList("tag1", "tag2"), hit.getTags()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Document createDocument(String projectName, String branchName, String path, String[] tags,
			String title, String text) {

		Document doc = new Document();
		doc.add(new StringField(PageIndex.PROJECT, projectName, Store.YES));
		doc.add(new StringField(PageIndex.BRANCH, branchName, Store.YES));
		doc.add(new StringField(PageIndex.PATH, path, Store.YES));
		for (String tag : tags) {
			doc.add(new StringField(PageIndex.TAG, tag, Store.YES));
		}
		doc.add(new TextField(PageIndex.TITLE, title, Store.YES));
		doc.add(new TextField(PageIndex.TEXT, text, Store.YES));
		return doc;
	}
}
