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
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.DirectSpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.search.spell.SuggestWord;
import org.apache.lucene.util.Bits;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.util.Util;

class PageFinder {
	private static final int HITS_PER_PAGE = 20;

	private SearcherManager searcherManager;
	private Analyzer analyzer;
	private ListeningExecutorService taskExecutor;
	private UserStore userStore;
	private DocumentrPermissionEvaluator permissionEvaluator;

	PageFinder(SearcherManager searcherManager, Analyzer analyzer, ListeningExecutorService taskExecutor, UserStore userStore,
			DocumentrPermissionEvaluator permissionEvaluator) {

		this.searcherManager = searcherManager;
		this.analyzer = analyzer;
		this.taskExecutor = taskExecutor;
		this.userStore = userStore;
		this.permissionEvaluator = permissionEvaluator;
	}

	public SearchResult findPages(final String searchText, final int page, final Authentication authentication)
			throws ParseException, IOException, TimeoutException {

		Assert.hasLength(searchText);
		Assert.isTrue(page >= 1);
		Assert.notNull(authentication);

		IndexSearcher searcher = null;
		Future<SearchResult> findFuture = null;
		try {
			searcher = searcherManager.acquire();
			final IndexSearcher indexSearcher = searcher;

			Callable<SearchResult> findCallable = new Callable<SearchResult>() {
				@Override
				public SearchResult call() throws ParseException, IOException, TimeoutException {
					return findPages(searchText, page, authentication, indexSearcher);
				}
			};
			findFuture = taskExecutor.submit(findCallable);

			SearchTextSuggestion suggestion = getSearchTextSuggestion(searchText, authentication, indexSearcher);
			SearchResult result = findFuture.get(DocumentrConstants.INTERACTIVE_TIMEOUT, TimeUnit.SECONDS);
			result.setSuggestion(suggestion);
			return result;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof ParseException) {
				throw (ParseException) cause;
			} else if (cause instanceof IOException) {
				throw (IOException) cause;
			} else if (cause instanceof TimeoutException) {
				throw (TimeoutException) cause;
			} else {
				throw Util.toRuntimeException(cause);
			}
		} finally {
			if (findFuture != null) {
				findFuture.cancel(false);
			}
			if (searcher != null) {
				searcherManager.release(searcher);
			}
		}
	}

	private SearchResult findPages(String searchText, int page, Authentication authentication, IndexSearcher searcher)
			throws ParseException, IOException, TimeoutException {

		Future<Query> queryFuture = taskExecutor.submit(new ParseQueryTask(searchText, analyzer));
		ListenableFuture<Bits> visibleDocIdsFuture = taskExecutor.submit(new GetVisibleDocIdsTask(
				searcher, authentication, userStore, permissionEvaluator, taskExecutor));

		Query query;
		TopDocs docs;
		try {
			query = queryFuture.get(DocumentrConstants.INTERACTIVE_TIMEOUT, TimeUnit.SECONDS);
			Bits visibleDocIds = visibleDocIdsFuture.get(DocumentrConstants.INTERACTIVE_TIMEOUT, TimeUnit.SECONDS);
			docs = searcher.search(query, new PagePermissionFilter(visibleDocIds), HITS_PER_PAGE * page);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof ParseException) {
				throw (ParseException) cause;
			} else {
				throw Util.toRuntimeException(cause);
			}
		} finally {
			queryFuture.cancel(false);
			visibleDocIdsFuture.cancel(false);
		}

		int start = HITS_PER_PAGE * (page - 1);
		int end = Math.min(HITS_PER_PAGE * page, docs.scoreDocs.length);
		IndexReader reader = searcher.getIndexReader();
		List<ListenableFuture<SearchHit>> hitFutures = Lists.newArrayList();
		for (int i = start; i < end; i++) {
			ListenableFuture<SearchHit> hitFuture = taskExecutor.submit(new GetSearchHitTask(
					query, reader, docs.scoreDocs[i].doc, analyzer));
			hitFutures.add(hitFuture);
		}

		try {
			ListenableFuture<List<SearchHit>> allHitsFuture = Futures.allAsList(hitFutures);
			List<SearchHit> hits = allHitsFuture.get(DocumentrConstants.INTERACTIVE_TIMEOUT, TimeUnit.SECONDS);
			return new SearchResult(hits, docs.totalHits, HITS_PER_PAGE);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			} else {
				throw Util.toRuntimeException(cause);
			}
		} finally {
			for (ListenableFuture<SearchHit> hitFuture : hitFutures) {
				hitFuture.cancel(false);
			}
		}
	}

	private SearchTextSuggestion getSearchTextSuggestion(String searchText, Authentication authentication,
			IndexSearcher searcher) throws IOException, ParseException, TimeoutException {

		List<WordPosition> words = Lists.newArrayList();

		TokenStream tokenStream = null;
		try {
			tokenStream = analyzer.tokenStream(PageIndex.ALL_TEXT_SUGGESTIONS, new StringReader(searchText));
			tokenStream.addAttribute(CharTermAttribute.class);
			tokenStream.addAttribute(OffsetAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				CharTermAttribute charTerm = tokenStream.getAttribute(CharTermAttribute.class);
				String text = charTerm.toString();
				if (StringUtils.isNotBlank(text)) {
					OffsetAttribute offset = tokenStream.getAttribute(OffsetAttribute.class);
					WordPosition word = new WordPosition(text, offset.startOffset(), offset.endOffset());
					words.add(word);
				}
			}
			tokenStream.end();
		} finally {
			Util.closeQuietly(tokenStream);
		}

		Collections.reverse(words);

		StringBuilder suggestedSearchText = new StringBuilder(searchText);
		StringBuilder suggestedSearchTextHtml = new StringBuilder(searchText);
		boolean foundSuggestions = false;
		String now = String.valueOf(System.currentTimeMillis());
		String startMarker = "__SUGGESTION-" + now + "__"; //$NON-NLS-1$ //$NON-NLS-2$
		String endMarker = "__/SUGGESTION-" + now + "__"; //$NON-NLS-1$ //$NON-NLS-2$
		DirectSpellChecker spellChecker = new DirectSpellChecker();
		IndexReader reader = searcher.getIndexReader();
		for (WordPosition word : words) {
			Term term = new Term(PageIndex.ALL_TEXT_SUGGESTIONS, word.getWord());
			SuggestWord[] suggestions = spellChecker.suggestSimilar(term, 1, reader, SuggestMode.SUGGEST_MORE_POPULAR);
			if (suggestions.length > 0) {
				String suggestedWord = suggestions[0].string;
				int start = word.getStart();
				int end = word.getEnd();
				suggestedSearchText.replace(start, end, suggestedWord);
				suggestedSearchTextHtml.replace(start, end,
						startMarker + StringEscapeUtils.escapeHtml4(suggestedWord) + endMarker);

				foundSuggestions = true;
			}
		}

		if (foundSuggestions) {
			String suggestion = suggestedSearchText.toString();
			SearchResult suggestionResult = findPages(suggestion, 1, authentication, searcher);
			int suggestionTotalHits = suggestionResult.getTotalHits();
			if (suggestionTotalHits > 0) {
				String html = StringEscapeUtils.escapeHtml4(suggestedSearchTextHtml.toString())
						.replaceAll(startMarker + "(.*?)" + endMarker, "<strong><em>$1</em></strong>"); //$NON-NLS-1$ //$NON-NLS-2$
				return new SearchTextSuggestion(suggestedSearchText.toString(), html, suggestionTotalHits);
			}
		}

		return null;
	}
}
