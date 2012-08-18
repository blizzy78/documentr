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
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.ReaderManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.DirectSpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.search.spell.SuggestWord;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.DocIdBitSet;
import org.apache.lucene.util.Version;
import org.cyberneko.html.HTMLEntities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import de.blizzy.documentr.Settings;
import de.blizzy.documentr.Util;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageChangedEvent;
import de.blizzy.documentr.page.PageTextData;
import de.blizzy.documentr.page.PagesDeletedEvent;
import de.blizzy.documentr.repository.BranchCreatedEvent;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
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
	
	static final String PROJECT = "project"; //$NON-NLS-1$
	static final String BRANCH = "branch"; //$NON-NLS-1$
	static final String PATH = "path"; //$NON-NLS-1$
	static final String ALL_TEXT = "allText"; //$NON-NLS-1$
	static final String TAG = "tag"; //$NON-NLS-1$
	static final String TITLE = "title"; //$NON-NLS-1$
	static final String TEXT = "text"; //$NON-NLS-1$
	static final String VIEW_RESTRICTION_ROLE = "viewRestrictionRole"; //$NON-NLS-1$

	private static final String FULL_PATH = "fullPath"; //$NON-NLS-1$
	private static final String ALL_TEXT_SUGGESTIONS = "allTextSuggestions"; //$NON-NLS-1$
	private static final int HITS_PER_PAGE = 20;
	private static final int REFRESH_INTERVAL = 30; // seconds
	private static final int THREAD_POOL_THREADS = 8;
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
	@Autowired
	private EventBus eventBus;
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private UserStore userStore;
	private Analyzer defaultAnalyzer;
	private Analyzer analyzer;
	private File pageIndexDir;
	private ListeningExecutorService threadPool = MoreExecutors.listeningDecorator(
			Executors.newFixedThreadPool(THREAD_POOL_THREADS,
					new ThreadFactoryBuilder().setNameFormat("Page Index (%d)").build())); //$NON-NLS-1$
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
		fieldAnalyzers.put(ALL_TEXT_SUGGESTIONS, new StandardAnalyzer(Version.LUCENE_40));
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
		
		eventBus.register(this);
		
		if (getNumDocuments() == 0) {
			reindexEverything();
		}
	}
	
	@PreDestroy
	public void destroy() {
		eventBus.unregister(this);
		
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
	
	private void reindexEverything() throws IOException {
		for (String projectName : repoManager.listProjects()) {
			for (String branchName : repoManager.listProjectBranches(projectName)) {
				addPages(projectName, branchName);
			}
		}
	}
	
	@Subscribe
	public void addPage(PageChangedEvent event) {
		String projectName = event.getProjectName();
		String branchName = event.getBranchName();
		String path = event.getPath();
		addPage(projectName, branchName, path);
	}
	
	private void addPage(final String projectName, final String branchName, final String path) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					addPageAsync(projectName, branchName, path, true);
				} catch (IOException e) {
					// TODO: log exception
				} catch (RuntimeException e) {
					// TODO: log exception
				}
			}
		};
		threadPool.submit(runnable);
	}

	@Subscribe
	public void addPages(BranchCreatedEvent event) {
		String projectName = event.getProjectName();
		String branchName = event.getBranchName();
		addPages(projectName, branchName);
	}

	private void addPages(final String projectName, final String branchName) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				boolean dirty = false;
				try {
					List<String> paths = pageStore.listAllPagePaths(projectName, branchName);
					for (String path : paths) {
						addPageAsync(projectName, branchName, path, false);
						dirty = true;
					}
				} catch (IOException e) {
					// TODO: log exception
				} catch (RuntimeException e) {
					// TODO: log exception
				} finally {
					if (dirty) {
						try {
							writer.commit();
						} catch (IOException e) {
							// TODO: log exception
						}
					}
				}
			}
		};
		threadPool.submit(runnable);
	}

	private void addPageAsync(String projectName, String branchName, String path, boolean commit) throws IOException {
		Page page = pageStore.getPage(projectName, branchName, path, true);
		
		String fullPath = projectName + "/" + branchName + "/" + Util.toURLPagePath(path); //$NON-NLS-1$ //$NON-NLS-2$
		
		String text = ((PageTextData) page.getData()).getText();
		Authentication authentication = authenticationFactory.create(UserStore.ANONYMOUS_USER_LOGIN_NAME);
		text = markdownProcessor.markdownToHTML(text, projectName, branchName, path, authentication, false);
		text = removeHtmlTags(text);
		text = replaceHtmlEntities(text);

		Document doc = new Document();
		doc.add(new StringField(FULL_PATH, fullPath, Store.NO));
		doc.add(new StringField(PROJECT, projectName, Store.YES));
		doc.add(new StringField(BRANCH, branchName, Store.YES));
		doc.add(new StringField(PATH, path, Store.YES));
		for (String tag : page.getTags()) {
			doc.add(new StringField(TAG, tag, Store.YES));
		}
		String viewRestrictionRole = page.getViewRestrictionRole();
		if (StringUtils.isNotBlank(viewRestrictionRole)) {
			doc.add(new StringField(VIEW_RESTRICTION_ROLE, viewRestrictionRole, Store.NO));
		}
		doc.add(new TextField(TITLE, page.getTitle(), Store.YES));
		doc.add(new TextField(TEXT, text, Store.YES));
		doc.add(new TextField(ALL_TEXT, page.getTitle(), Store.NO));
		doc.add(new TextField(ALL_TEXT, text, Store.NO));
		for (String tag : page.getTags()) {
			doc.add(new TextField(ALL_TEXT, tag, Store.NO));
		}
		doc.add(new TextField(ALL_TEXT_SUGGESTIONS, page.getTitle(), Store.NO));
		doc.add(new TextField(ALL_TEXT_SUGGESTIONS, text, Store.NO));
		for (String tag : page.getTags()) {
			doc.add(new TextField(ALL_TEXT_SUGGESTIONS, tag, Store.NO));
		}

		writer.updateDocument(new Term(FULL_PATH, fullPath), doc);
		
		if (commit) {
			writer.commit();
		}
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

	@Subscribe
	public void deletePages(PagesDeletedEvent event) {
		deletePages(event.getProjectName(), event.getBranchName(), event.getPaths());
	}
	
	private void deletePages(final String projectName, final String branchName, final Set<String> paths) {
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
				writer.deleteDocuments(new Term(FULL_PATH, fullPath));
				dirty = true;
			}
		} finally {
			if (dirty) {
				writer.commit();
			}
		}
	}

	public SearchResult findPages(final String searchText, final int page, final Authentication authentication)
			throws ParseException, IOException {
		
		Assert.hasLength(searchText);
		Assert.isTrue(page >= 1);
		Assert.notNull(authentication);

		IndexSearcher searcher = null;
		try {
			if (alwaysRefresh) {
				refreshBlocking();
			}
			
			searcher = searcherManager.acquire();
			final IndexSearcher indexSearcher = searcher;

			Callable<SearchResult> findCallable = new Callable<SearchResult>() {
				@Override
				public SearchResult call() throws ParseException, IOException {
					return findPages(searchText, page, authentication, indexSearcher);
				}
			};
			Future<SearchResult> findFuture = threadPool.submit(findCallable);
			
			Callable<SearchTextSuggestion> suggestionCallable = new Callable<SearchTextSuggestion>() {
				@Override
				public SearchTextSuggestion call() throws ParseException, IOException {
					return getSearchTextSuggestion(searchText, authentication, indexSearcher);
				}
			};
			Future<SearchTextSuggestion> suggestionFuture = threadPool.submit(suggestionCallable);
			
			SearchResult result = findFuture.get();
			SearchTextSuggestion suggestion = suggestionFuture.get();
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
			} else {
				throw Util.toRuntimeException(cause);
			}
		} finally {
			if (searcher != null) {
				searcherManager.release(searcher);
			}
		}
	}
	
	private SearchResult findPages(String searchText, int page, Authentication authentication, IndexSearcher searcher)
			throws ParseException, IOException {
		
		Future<Query> queryFuture = threadPool.submit(new ParseQueryTask(searchText, analyzer));
		Future<Bits> visibleDocsFuture = threadPool.submit(new GetVisibleDocIdsTask(searcher, authentication, this));
		Query query;
		Bits visibleDocIds;
		try {
			query = queryFuture.get();
			visibleDocIds = visibleDocsFuture.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof ParseException) {
				throw (ParseException) cause;
			} else if (cause instanceof IOException) {
				throw (IOException) cause;
			} else {
				throw Util.toRuntimeException(cause);
			}
		}
		TopDocs docs = searcher.search(query, new PagePermissionFilter(visibleDocIds), HITS_PER_PAGE * page);

		int start = HITS_PER_PAGE * (page - 1);
		int end = Math.min(HITS_PER_PAGE * page, docs.scoreDocs.length);
		IndexReader reader = searcher.getIndexReader();
		List<ListenableFuture<SearchHit>> hitFutures = Lists.newArrayList();
		for (int i = start; i < end; i++) {
			ListenableFuture<SearchHit> hitFuture = threadPool.submit(new GetSearchHitTask(
					query, reader, docs.scoreDocs[i].doc, analyzer));
			hitFutures.add(hitFuture);
		}
		
		try {
			ListenableFuture<List<SearchHit>> allHitsFuture = Futures.allAsList(hitFutures);
			List<SearchHit> hits = allHitsFuture.get();
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
		}
	}

	private SearchTextSuggestion getSearchTextSuggestion(String searchText, Authentication authentication,
			IndexSearcher searcher) throws IOException, ParseException {
		
		List<WordPosition> words = Lists.newArrayList();
		
		TokenStream tokenStream = null;
		try {
			tokenStream = analyzer.tokenStream(ALL_TEXT_SUGGESTIONS, new StringReader(searchText));
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
			Term term = new Term(ALL_TEXT_SUGGESTIONS, word.word);
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

	public Set<String> getAllTags(Authentication authentication) throws IOException {
		IndexReader reader = null;
		IndexSearcher searcher = null;
		try {
			if (alwaysRefresh) {
				refreshBlocking();
			}

			searcher = searcherManager.acquire();
			Bits visibleDocs = getVisibleDocIds(searcher, authentication);
			Set<String> tags = Sets.newHashSet();
			if (visibleDocs.length() > 0) {
				reader = searcher.getIndexReader();
				Terms terms = MultiFields.getTerms(reader, TAG);
				if (terms != null) {
					TermsEnum termsEnum = terms.iterator(null);
					BytesRef ref;
					while ((ref = termsEnum.next()) != null) {
						DocsEnum docsEnum = termsEnum.docs(visibleDocs, null, 0);
						if (docsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
							tags.add(ref.utf8ToString());
						}
					}
				}
			}
			return tags;
		} finally {
			if (searcher != null) {
				searcherManager.release(searcher);
			}
		}
	}
	
	Bits getVisibleDocIds(IndexSearcher searcher, Authentication authentication) throws IOException {
		Set<String> branches = permissionEvaluator.getBranchesForPermission(authentication, Permission.VIEW);
		BitSet docIds = new BitSet();
		if (!branches.isEmpty()) {
			Future<BitSet> branchPagesFuture = threadPool.submit(new GetVisibleBranchDocIdsTask(branches, searcher));
			Future<BitSet> inaccessibleDocsFuture = threadPool.submit(new GetInaccessibleDocIdsTask(
					searcher, Permission.VIEW, authentication, userStore, permissionEvaluator));
			try {
				docIds.or(branchPagesFuture.get());
				docIds.andNot(inaccessibleDocsFuture.get());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof IOException) {
					throw (IOException) cause;
				} else {
					throw Util.toRuntimeException(cause);
				}
			}
		}
		return new DocIdBitSet(docIds);
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
		}
		
		try {
			searcherManager.maybeRefreshBlocking();
		} catch (IOException e) {
			// ignore
		}
	}
	
	int getNumDocuments() throws IOException {
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
