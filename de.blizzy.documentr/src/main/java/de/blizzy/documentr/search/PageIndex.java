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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.cyberneko.html.HTMLEntities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.inject.internal.Lists;

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
	private static final int HITS_PER_PAGE = 20;
	private static final int NUM_FRAGMENTS = 5;
	private static final int FRAGMENT_SIZE = 50;
	@SuppressWarnings("nls")
	private static final String[] REMOVE_HTML_TAGS = {
		"(<br(?: .*?)?(?:/)?>)", "\n$1",
		"(<p(?: .*?)?>)", "\n$1",
		"(<pre(?: .*?)?>)", "\n$1",
		"(<ol(?: .*?)?>)", "\n$1",
		"(<ul(?: .*?)?>)", "\n$1",
		"(<dl(?: .*?)?>)", "\n$1",
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
	private Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_36);
	private File pageIndexDir;
	private ExecutorService threadPool = Executors.newFixedThreadPool(1);
	private boolean created;
	
	@PostConstruct
	public void init() throws IOException {
		File indexDir = new File(settings.getDocumentrDataDir(), "index"); //$NON-NLS-1$
		pageIndexDir = new File(indexDir, "page"); //$NON-NLS-1$
		if (!pageIndexDir.isDirectory()) {
			created = true;
		}
		FileUtils.forceMkdir(pageIndexDir);
	}
	
	@PreDestroy
	public void destroy() {
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	public void addPage(final String projectName, final String branchName, final String path, final Page page) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					addPageInternal(projectName, branchName, path, page);
				} catch (IOException e) {
					// ignore
				}
			}
		};
		threadPool.submit(runnable);
	}
	
	private synchronized void addPageInternal(String projectName, String branchName, String path, Page page)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		Assert.notNull(page);
		Assert.isTrue(page.getData() instanceof PageTextData);

		Directory directory = null;
		IndexWriter writer = null;
		try {
			directory = FSDirectory.open(pageIndexDir);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
			config.setOpenMode(OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(directory, config);
	
			Document doc = new Document();
			String fullPath = projectName + "/" + branchName + "/" + Util.toURLPagePath(path); //$NON-NLS-1$ //$NON-NLS-2$
			doc.add(new Field("fullPath", fullPath, Store.NO, Index.NOT_ANALYZED, TermVector.NO)); //$NON-NLS-1$
			doc.add(new Field("project", projectName, Store.YES, Index.NOT_ANALYZED, TermVector.NO)); //$NON-NLS-1$
			doc.add(new Field("branch", branchName, Store.YES, Index.NOT_ANALYZED, TermVector.NO)); //$NON-NLS-1$
			doc.add(new Field("path", path, Store.YES, Index.NOT_ANALYZED, TermVector.NO)); //$NON-NLS-1$
			doc.add(new Field("title", page.getTitle(), Store.YES, Index.ANALYZED, TermVector.YES)); //$NON-NLS-1$
			String text = ((PageTextData) page.getData()).getText();
			Authentication authentication = authenticationFactory.create("dummy"); //$NON-NLS-1$
			text = markdownProcessor.markdownToHTML(text, projectName, branchName, path, authentication, false);
			text = removeHtmlTags(text);
			text = replaceHtmlEntities(text);
			doc.add(new Field("text", text, Store.YES, Index.ANALYZED, TermVector.YES)); //$NON-NLS-1$
			writer.updateDocument(new Term("fullPath", fullPath), doc); //$NON-NLS-1$
		} finally {
			IndexUtil.closeQuietly(writer);
			IndexUtil.closeQuietly(directory);
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

	public void deletePages(final String projectName, final String branchName, final Set<String> paths) {
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
		threadPool.submit(runnable);
	}

	private synchronized void deletePagesInternal(String projectName, String branchName, Set<String> paths)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.notEmpty(paths);

		Directory directory = null;
		IndexWriter writer = null;
		try {
			directory = FSDirectory.open(pageIndexDir);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
			config.setOpenMode(OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(directory, config);

			for (String path : paths) {
				String fullPath = projectName + "/" + branchName + "/" + Util.toURLPagePath(path); //$NON-NLS-1$ //$NON-NLS-2$
				writer.deleteDocuments(new Term("fullPath", fullPath)); //$NON-NLS-1$
			}
		} finally {
			IndexUtil.closeQuietly(writer);
			IndexUtil.closeQuietly(directory);
		}
	}

	public SearchResult findPages(String searchText, int page, Authentication authentication) throws ParseException, IOException {
		QueryParser titleParser = new QueryParser(Version.LUCENE_36, "title", analyzer); //$NON-NLS-1$
		Query titleQuery = titleParser.parse(searchText);
		QueryParser textParser = new QueryParser(Version.LUCENE_36, "text", analyzer); //$NON-NLS-1$
		Query textQuery = textParser.parse(searchText);
		
		BooleanQuery query = new BooleanQuery();
		query.add(titleQuery, Occur.SHOULD);
		query.add(textQuery, Occur.SHOULD);
		
		Directory directory = null;
		IndexReader reader = null;
		IndexSearcher searcher = null;
		List<SearchHit> hits = Lists.newArrayList();
		int totalHits;
		try {
			directory = FSDirectory.open(pageIndexDir);
			reader = IndexReader.open(directory);
			searcher = new IndexSearcher(reader);
			Filter filter = new PagePermissionFilter(authentication, Permission.VIEW, permissionEvaluator);
			TopDocs docs = searcher.search(query, filter, HITS_PER_PAGE * page);
			totalHits = docs.totalHits;
			
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
		} finally {
			IndexUtil.closeQuietly(searcher);
			IndexUtil.closeQuietly(reader);
			IndexUtil.closeQuietly(directory);
		}
		return new SearchResult(hits, totalHits, HITS_PER_PAGE);
	}
	
	private void cleanupFragments(String[] fragments) {
		for (int i = 0; i < fragments.length; i++) {
			fragments[i] = fragments[i].replaceAll("^[,\\.]+", StringUtils.EMPTY).trim(); //$NON-NLS-1$
		}
	}

	public boolean isCreated() {
		return created;
	}
}
