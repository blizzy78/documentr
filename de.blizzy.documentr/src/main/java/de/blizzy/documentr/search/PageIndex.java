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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.ReaderManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.spell.DirectSpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.search.spell.SuggestWord;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.cyberneko.html.HTMLEntities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.blizzy.documentr.Settings;
import de.blizzy.documentr.Util;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageTextData;
import de.blizzy.documentr.web.markdown.MarkdownProcessor;

@Component
public class PageIndex {
	private static final class WordPosition {
		String word;
		int start;
		int end;

		WordPosition(String word, int start, int end) {
			this.word = word;
			this.start = start;
			this.end = end;
		}
	}
	
	private static final int HITS_PER_PAGE = 20;
	private static final int NUM_FRAGMENTS = 5;
	private static final int FRAGMENT_SIZE = 50;
	private static final int REFRESH_INTERVAL = 30; // seconds
	@SuppressWarnings("nls")
	private static final String[] REMOVE_HTML_TAGS = {
		"(<br(?: .*?)?(?:/)?>)", "\n$1",
		"(<p(?: .*?)?>)", "\n$1",
		"(<pre(?: .*?)?>)", "\n$1",
		"(<div(?: .*?)?>)", "\n$1",
		"(<ol(?: .*?)?>)", "\n$1",
		"(<ul(?: .*?)?>)", "\n$1",
		"(<dl(?: .*?)?>)", "\n$1",
		"(<td(?: .*?)?>)", "\n$1",
		"(<h[0-9]+(?: .*?)?>)", "\n$1",
		"<script.*?>.*?</script>", StringUtils.EMPTY,
		"<.*?>", StringUtils.EMPTY
	};
	
	@Autowired
	private Settings settings;
	@Autowired
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Autowired
	private MarkdownProcessor markdownProcessor;
	@Autowired
	private DocumentrAnonymousAuthenticationFactory authenticationFactory;
	private Analyzer defaultAnalyzer;
	private Analyzer analyzer;
	private File pageIndexDir;
	private ExecutorService threadPool = Executors.newFixedThreadPool(4);
	private Directory directory;
	private IndexWriter writer;
	private ReaderManager readerManager;
	private SearcherManager searcherManager;
	private Timer timer = new Timer();
	private boolean alwaysRefresh;
	
	@PostConstruct
	public void init() throws IOException {
		File indexDir = new File(settings.getDocumentrDataDir(), "index"); //$NON-NLS-1$
		pageIndexDir = new File(indexDir, "page"); //$NON-NLS-1$
		FileUtils.forceMkdir(pageIndexDir);
		
		directory = FSDirectory.open(pageIndexDir);

		defaultAnalyzer = new EnglishAnalyzer(Version.LUCENE_40);
		Map<String, Analyzer> fieldAnalyzers = Maps.newHashMap();
		fieldAnalyzers.put("allText", new StandardAnalyzer(Version.LUCENE_40)); //$NON-NLS-1$
		analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, fieldAnalyzers);

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		writer = new IndexWriter(directory, config);
		writer.commit();
		
		readerManager = new ReaderManager(directory);
		searcherManager = new SearcherManager(directory, null);
		
		TimerTask refreshTask = new TimerTask() {
			@Override
			public void run() {
				refresh();
			}
		};
		timer.schedule(refreshTask, 0, REFRESH_INTERVAL * 1000);
	}
	
	@PreDestroy
	public void destroy() {
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// ignore
		}
		timer.cancel();
		try {
			searcherManager.close();
		} catch (IOException e) {
			// ignore
		}
		try {
			readerManager.close();
		} catch (IOException e) {
			// ignore
		}
		IndexUtil.closeQuietly(writer);
		IndexUtil.closeQuietly(directory);
	}
	
	public void addPage(final String projectName, final String branchName, final String path, final Page page) {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		Assert.notNull(page);
		Assert.isTrue(page.getData() instanceof PageTextData);

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					addPageInternal(projectName, branchName, path, page);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		};
		threadPool.submit(runnable);
	}
	
	private void addPageInternal(String projectName, String branchName, String path, Page page) throws IOException {
		String fullPath = projectName + "/" + branchName + "/" + Util.toURLPagePath(path); //$NON-NLS-1$ //$NON-NLS-2$
		String text = ((PageTextData) page.getData()).getText();
		Authentication authentication = authenticationFactory.create("dummy"); //$NON-NLS-1$
		text = markdownProcessor.markdownToHTML(text, projectName, branchName, path, authentication, false);
		text = removeHtmlTags(text);
		text = replaceHtmlEntities(text);

		Document doc = new Document();
		doc.add(new StringField("fullPath", fullPath, Store.NO)); //$NON-NLS-1$
		doc.add(new StringField("project", projectName, Store.YES)); //$NON-NLS-1$
		doc.add(new StringField("branch", branchName, Store.YES)); //$NON-NLS-1$
		doc.add(new StringField("path", path, Store.YES)); //$NON-NLS-1$
		doc.add(new TextField("title", page.getTitle(), Store.YES)); //$NON-NLS-1$
		doc.add(new TextField("text", text, Store.YES)); //$NON-NLS-1$
		doc.add(new TextField("allText", page.getTitle(), Store.NO)); //$NON-NLS-1$
		doc.add(new TextField("allText", text, Store.NO)); //$NON-NLS-1$
		writer.updateDocument(new Term("fullPath", fullPath), doc); //$NON-NLS-1$

		writer.commit();
	}
	
	private String removeHtmlTags(String html) {
		for (int i = 0; i < REMOVE_HTML_TAGS.length; i += 2) {
			String re = REMOVE_HTML_TAGS[i];
			String replaceWith = REMOVE_HTML_TAGS[i + 1];
			Pattern pattern = Pattern.compile(re, Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(html);
			html = matcher.replaceAll(replaceWith);
		}
		return html;
	}
	
	private String replaceHtmlEntities(String html) {
		for (;;) {
			int pos = html.indexOf('&');
			if (pos < 0) {
				break;
			}
			int endPos = html.indexOf(';', pos + 1);
			if (endPos < 0) {
				break;
			}
			String entityName = html.substring(pos + 1, endPos);
			int c = HTMLEntities.get(entityName);
			html = StringUtils.replace(html, "&" + entityName + ";", //$NON-NLS-1$ //$NON-NLS-2$
					(c >= 0) ? String.valueOf((char) c) : StringUtils.EMPTY);
		}
		return html;
	}

	public void deletePages(final String projectName, final String branchName, final Set<String> paths) {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.notEmpty(paths);

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					deletePagesInternal(projectName, branchName, paths);
				} catch (IOException e) {
					// ignore
				}
			}
		};
		Future<?> future = threadPool.submit(runnable);
		try {
			future.get();
		} catch (InterruptedException e) {
			// ignore
		} catch (ExecutionException e) {
			// ignore
		}
	}

	private void deletePagesInternal(String projectName, String branchName, Set<String> paths) throws IOException {
		boolean dirty = false;
		try {
			for (String path : paths) {
				String fullPath = projectName + "/" + branchName + "/" + Util.toURLPagePath(path); //$NON-NLS-1$ //$NON-NLS-2$
				writer.deleteDocuments(new Term("fullPath", fullPath)); //$NON-NLS-1$
				dirty = true;
			}
		} finally {
			if (dirty) {
				writer.commit();
			}
		}
	}

	public SearchResult findPages(String searchText, int page, Authentication authentication) throws ParseException, IOException {
		Assert.hasLength(searchText);
		Assert.isTrue(page >= 1);
		Assert.notNull(authentication);

		IndexSearcher searcher = null;
		try {
			if (alwaysRefresh) {
				refreshBlocking();
			}
			searcher = searcherManager.acquire();
			SearchResult result = findPages(searchText, page, authentication, searcher);
			SearchTextSuggestion suggestion = getSearchTextSuggestion(searchText, authentication, searcher);
			result.setSuggestion(suggestion);
			return result;
		} finally {
			if (searcher != null) {
				searcherManager.release(searcher);
			}
		}
	}
	
	private SearchResult findPages(String searchText, int page, Authentication authentication, IndexSearcher searcher)
			throws ParseException, IOException {
		
		QueryParser titleParser = new QueryParser(Version.LUCENE_40, "title", analyzer); //$NON-NLS-1$
		Query titleQuery = titleParser.parse(searchText);
		QueryParser textParser = new QueryParser(Version.LUCENE_40, "text", analyzer); //$NON-NLS-1$
		Query textQuery = textParser.parse(searchText);
		
		BooleanQuery query = new BooleanQuery();
		query.add(titleQuery, Occur.SHOULD);
		query.add(textQuery, Occur.SHOULD);
		List<SearchHit> hits = Lists.newArrayList();
		IndexReader reader = searcher.getIndexReader();
		Filter filter = new PagePermissionFilter(authentication, Permission.VIEW, permissionEvaluator);
		TopDocs docs = searcher.search(query, filter, HITS_PER_PAGE * page);
		Formatter formatter = new SimpleHTMLFormatter("<strong>", "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$
		Scorer scorer = new QueryScorer(query);
		Highlighter highlighter = new Highlighter(formatter, scorer);
		highlighter.setTextFragmenter(new SimpleFragmenter(FRAGMENT_SIZE));
		highlighter.setEncoder(new SimpleHTMLEncoder());
		int start = HITS_PER_PAGE * (page - 1);
		int end = Math.min(HITS_PER_PAGE * page, docs.scoreDocs.length);
		for (int i = start; i < end; i++) {
			int docId = docs.scoreDocs[i].doc;
			Document doc = reader.document(docId);
			String projectName = doc.get("project"); //$NON-NLS-1$
			String branchName = doc.get("branch"); //$NON-NLS-1$
			String path = doc.get("path"); //$NON-NLS-1$
			String title = doc.get("title"); //$NON-NLS-1$
			String text = doc.get("text"); //$NON-NLS-1$
			TokenStream tokenStream = null;
			try {
				tokenStream = TokenSources.getAnyTokenStream(reader, docId, "text", doc, analyzer); //$NON-NLS-1$
				String[] fragments = highlighter.getBestFragments(tokenStream, text, NUM_FRAGMENTS);
				cleanupFragments(fragments);
				String highlightedText = Util.join(fragments, " <strong>...</strong> "); //$NON-NLS-1$
				SearchHit hit = new SearchHit(projectName, branchName, path, title, highlightedText);
				hits.add(hit);
			} catch (InvalidTokenOffsetsException e) {
				// ignore
			} finally {
				IndexUtil.closeQuietly(tokenStream);
			}
		}
		
		return new SearchResult(hits, docs.totalHits, HITS_PER_PAGE);
	}

	private SearchTextSuggestion getSearchTextSuggestion(String searchText, Authentication authentication,
			IndexSearcher searcher) throws IOException, ParseException {
		
		List<WordPosition> words = Lists.newArrayList();
		
		TokenStream tokenStream = null;
		try {
			tokenStream = analyzer.tokenStream("allText", new StringReader(searchText)); //$NON-NLS-1$
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
			if (tokenStream != null) {
				tokenStream.close();
			}
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
			Term term = new Term("allText", word.word); //$NON-NLS-1$
			SuggestWord[] suggestions = spellChecker.suggestSimilar(term, 1, reader, SuggestMode.SUGGEST_MORE_POPULAR);
			if (suggestions.length > 0) {
				String suggestedWord = suggestions[0].string;
				suggestedSearchText.replace(word.start, word.end, suggestedWord);
				suggestedSearchTextHtml.replace(word.start, word.end,
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

	private void cleanupFragments(String[] fragments) {
		for (int i = 0; i < fragments.length; i++) {
			fragments[i] = fragments[i].replaceAll("^[,\\.]+", StringUtils.EMPTY).trim(); //$NON-NLS-1$
		}
	}
	
	private void refresh() {
		try {
			readerManager.maybeRefresh();
		} catch (IOException e) {
			// ignore
		}
		
		try {
			searcherManager.maybeRefresh();
		} catch (IOException e) {
			// ignore
		}
	}
	
	private void refreshBlocking() {
		try {
			readerManager.maybeRefreshBlocking();
		} catch (IOException e) {
			// ignore
		} catch (InterruptedException e) {
			// ignore
		}
		
		try {
			searcherManager.maybeRefreshBlocking();
		} catch (IOException e) {
			// ignore
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	public int getNumDocuments() throws IOException {
		DirectoryReader reader = null;
		try {
			if (alwaysRefresh) {
				refreshBlocking();
			}
			reader = readerManager.acquire();
			return reader.numDocs();
		} finally {
			readerManager.release(reader);
		}
	}
	
	void setAlwaysRefresh(boolean alwaysRefresh) {
		this.alwaysRefresh = alwaysRefresh;
	}
}
