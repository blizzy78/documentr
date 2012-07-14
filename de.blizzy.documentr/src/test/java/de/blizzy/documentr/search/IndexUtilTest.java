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

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;
import org.junit.Test.None;

public class IndexUtilTest {
	private Directory directory;

	@Before
	public void setUp() throws IOException {
		directory = new RAMDirectory();
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
		IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, writerConfig);
		indexWriter.close();
	}
	
	@Test(expected=None.class)
	public void closeIndexWriterQuietly() throws IOException {
		IndexWriter indexWriter = mock(IndexWriter.class);
		IndexUtil.closeQuietly(indexWriter);
		verify(indexWriter).close();
	}

	@Test(expected=None.class)
	public void closeIndexWriterQuietlyMustAcceptNull() {
		IndexUtil.closeQuietly((IndexWriter) null);
	}

	@Test(expected=None.class)
	public void closeIndexWriterQuietlyMustCatchException() throws IOException {
		IndexWriter indexWriter = mock(IndexWriter.class);
		doThrow(IOException.class).when(indexWriter).close();
		IndexUtil.closeQuietly(indexWriter);
	}

	@Test(expected=None.class)
	public void closeTokenStreamQuietly() throws IOException {
		TokenStream tokenStream = mock(TokenStream.class);
		IndexUtil.closeQuietly(tokenStream);
		verify(tokenStream).close();
	}
	
	@Test(expected=None.class)
	public void closeTokenStreamQuietlyMustAcceptNull() {
		IndexUtil.closeQuietly((TokenStream) null);
	}
	
	@Test(expected=None.class)
	public void closeTokenStreamQuietlyMustCatchException() throws IOException {
		TokenStream tokenStream = mock(TokenStream.class);
		doThrow(IOException.class).when(tokenStream).close();
		IndexUtil.closeQuietly(tokenStream);
	}
	
	@Test(expected=None.class)
	public void closeDirectoryQuietly() throws IOException {
		Directory directory = mock(Directory.class);
		IndexUtil.closeQuietly(directory);
		verify(directory).close();
	}
	
	@Test(expected=None.class)
	public void closeDirectoryQuietlyMustAcceptNull() {
		IndexUtil.closeQuietly((Directory) null);
	}
	
	@Test(expected=None.class)
	public void closeDirectoryQuietlyMustCatchException() throws IOException {
		Directory directory = mock(Directory.class);
		doThrow(IOException.class).when(directory).close();
		IndexUtil.closeQuietly(directory);
	}
}
