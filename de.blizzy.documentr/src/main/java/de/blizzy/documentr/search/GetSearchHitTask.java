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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import de.blizzy.documentr.util.Util;

class GetSearchHitTask implements Callable<SearchHit> {
	private static final int NUM_FRAGMENTS = 5;
	private static final int FRAGMENT_SIZE = 50;

	private Query query;
	private IndexReader reader;
	private int docId;
	private Analyzer analyzer;

	GetSearchHitTask(Query query, IndexReader reader, int docId, Analyzer analyzer) {
		this.query = query;
		this.reader = reader;
		this.docId = docId;
		this.analyzer = analyzer;
	}

	@Override
	public SearchHit call() throws IOException {
		Formatter formatter = new SimpleHTMLFormatter("<strong>", "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$
		Scorer scorer = new QueryScorer(query);
		Highlighter highlighter = new Highlighter(formatter, scorer);
		highlighter.setTextFragmenter(new SimpleFragmenter(FRAGMENT_SIZE));
		highlighter.setEncoder(new SimpleHTMLEncoder());

		Document doc = reader.document(docId);
		String projectName = doc.get(PageIndex.PROJECT);
		String branchName = doc.get(PageIndex.BRANCH);
		String path = doc.get(PageIndex.PATH);
		String title = doc.get(PageIndex.TITLE);
		String text = doc.get(PageIndex.TEXT);
		String[] tagsArray = doc.getValues(PageIndex.TAG);
		List<String> tags = Lists.newArrayList(tagsArray);
		Collections.sort(tags);
		TokenStream tokenStream = null;
		String highlightedText = StringUtils.EMPTY;
		try {
			tokenStream = TokenSources.getAnyTokenStream(reader, docId, PageIndex.TEXT, doc, analyzer);
			String[] fragments = highlighter.getBestFragments(tokenStream, text, NUM_FRAGMENTS);
			cleanupFragments(fragments);
			highlightedText = Util.join(fragments, " <strong>...</strong> "); //$NON-NLS-1$
		} catch (InvalidTokenOffsetsException e) {
			// ignore
		} finally {
			Closeables.closeQuietly(tokenStream);
		}
		return new SearchHit(projectName, branchName, path, title, highlightedText, tags);
	}

	private void cleanupFragments(String[] fragments) {
		for (int i = 0; i < fragments.length; i++) {
			fragments[i] = fragments[i].replaceAll("^[,\\.]+", StringUtils.EMPTY).trim(); //$NON-NLS-1$
		}
	}
}
