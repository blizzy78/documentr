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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.ReaderManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.cyberneko.html.HTMLEntities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListeningExecutorService;

import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.markdown.MarkdownProcessor;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageChangedEvent;
import de.blizzy.documentr.page.PageTextData;
import de.blizzy.documentr.page.PagesDeletedEvent;
import de.blizzy.documentr.repository.BranchCreatedEvent;
import de.blizzy.documentr.repository.IGlobalRepositoryManager;
import de.blizzy.documentr.repository.ProjectBranchDeletedEvent;
import de.blizzy.documentr.repository.ProjectBranchRenamedEvent;
import de.blizzy.documentr.repository.ProjectDeletedEvent;
import de.blizzy.documentr.repository.ProjectRenamedEvent;
import de.blizzy.documentr.util.Replacement;
import de.blizzy.documentr.util.Util;

@Component
@Slf4j
public class PageIndex {
	static final String PROJECT = "project"; //$NON-NLS-1$
	static final String BRANCH = "branch"; //$NON-NLS-1$
	static final String PATH = "path"; //$NON-NLS-1$
	static final String ALL_TEXT = "allText"; //$NON-NLS-1$
	static final String TAG = "tag"; //$NON-NLS-1$
	static final String TITLE = "title"; //$NON-NLS-1$
	static final String TEXT = "text"; //$NON-NLS-1$
	static final String VIEW_RESTRICTION_ROLE = "viewRestrictionRole"; //$NON-NLS-1$
	static final String ALL_TEXT_SUGGESTIONS = "allTextSuggestions"; //$NON-NLS-1$

	private static final String FULL_PATH = "fullPath"; //$NON-NLS-1$
	private static final int REFRESH_INTERVAL = 30; // seconds
	@SuppressWarnings("nls")
	private static final List<Replacement> REMOVE_HTML_TAGS = Lists.newArrayList(
		Replacement.dotAllNoCase("(<br(?: .*?)?(?:/)?>)", "\n$1"),
		Replacement.dotAllNoCase("(<p(?: .*?)?>)", "\n$1"),
		Replacement.dotAllNoCase("(<pre(?: .*?)?>)", "\n$1"),
		Replacement.dotAllNoCase("(<div(?: .*?)?>)", "\n$1"),
		Replacement.dotAllNoCase("(<ol(?: .*?)?>)", "\n$1"),
		Replacement.dotAllNoCase("(<ul(?: .*?)?>)", "\n$1"),
		Replacement.dotAllNoCase("(<dl(?: .*?)?>)", "\n$1"),
		Replacement.dotAllNoCase("(<td(?: .*?)?>)", "\n$1"),
		Replacement.dotAllNoCase("(<h[0-9]+(?: .*?)?>)", "\n$1"),
		Replacement.dotAllNoCase("<script.*?>.*?</script>", StringUtils.EMPTY),
		Replacement.dotAllNoCase("<.*?>", StringUtils.EMPTY)
	);

	@Autowired
	private Settings settings;
	@Autowired
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Autowired
	private MarkdownProcessor markdownProcessor;
	@Autowired
	private DocumentrAnonymousAuthenticationFactory authenticationFactory;
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private IGlobalRepositoryManager repoManager;
	@Autowired
	private UserStore userStore;
	@Autowired
	private ListeningExecutorService taskExecutor;
	@Autowired
	private IGlobalRepositoryManager globalRepositoryManager;
	private Analyzer analyzer;
	private Directory directory;
	private IndexWriter writer;
	private ReaderManager readerManager;
	private SearcherManager searcherManager;
	private AtomicBoolean dirty = new AtomicBoolean();

	@PostConstruct
	public void init() throws IOException {
		File indexDir = new File(settings.getDocumentrDataDir(), "index"); //$NON-NLS-1$
		File pageIndexDir = new File(indexDir, "page"); //$NON-NLS-1$
		FileUtils.forceMkdir(pageIndexDir);

		directory = FSDirectory.open(pageIndexDir);

		Analyzer defaultAnalyzer = new EnglishAnalyzer(Version.LUCENE_42);
		Map<String, Analyzer> fieldAnalyzers = Maps.newHashMap();
		fieldAnalyzers.put(ALL_TEXT_SUGGESTIONS, new StandardAnalyzer(Version.LUCENE_42));
		analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, fieldAnalyzers);

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		writer = new IndexWriter(directory, config);
		writer.commit();

		readerManager = new ReaderManager(directory);
		searcherManager = new SearcherManager(directory, null);

		log.info("checking if index is empty"); //$NON-NLS-1$
		if (getNumDocuments() == 0) {
			reindexEverything();
		}
	}

	@PreDestroy
	public void destroy() {
		Util.closeQuietly(searcherManager);
		Util.closeQuietly(readerManager);
		Util.closeQuietly(writer);
		Util.closeQuietly(directory);
	}

	private void reindexEverything() throws IOException {
		log.info("reindexing everything"); //$NON-NLS-1$

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
		submitAddPageTask(projectName, branchName, path);
	}

	private void submitAddPageTask(final String projectName, final String branchName, final String path) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					addPageAsync(projectName, branchName, path);
				} catch (IOException e) {
					log.error(StringUtils.EMPTY, e);
				} catch (RuntimeException e) {
					log.error(StringUtils.EMPTY, e);
				}
			}
		};
		taskExecutor.submit(runnable);
	}

	@Subscribe
	public void addPages(BranchCreatedEvent event) {
		String projectName = event.getProjectName();
		String branchName = event.getBranchName();
		try {
			addPages(projectName, branchName);
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}

	private void addPages(String projectName, String branchName) throws IOException {
		List<String> paths = pageStore.listAllPagePaths(projectName, branchName);
		for (String path : paths) {
			submitAddPageTask(projectName, branchName, path);
		}
	}

	@Subscribe
	public void renameProject(ProjectRenamedEvent event) {
		String projectName = event.getProjectName();
		String newProjectName = event.getNewProjectName();
		try {
			submitRenameProjectTask(projectName, newProjectName);
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}

	private void submitRenameProjectTask(final String projectName, final String newProjectName) throws IOException {
		final Map<String, List<String>> branchPagePaths = Maps.newHashMap();
		List<String> branches = globalRepositoryManager.listProjectBranches(newProjectName);
		for (String branch : branches) {
			List<String> pagePaths = pageStore.listAllPagePaths(newProjectName, branch);
			branchPagePaths.put(branch, pagePaths);
		}

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					renameProjectAsync(projectName, newProjectName, branchPagePaths);
				} catch (IOException e) {
					log.error(StringUtils.EMPTY, e);
				} catch (RuntimeException e) {
					log.error(StringUtils.EMPTY, e);
				}
			}
		};
		taskExecutor.submit(runnable);
	}

	private void renameProjectAsync(String projectName, String newProjectName, Map<String, List<String>> branchPagePaths)
			throws IOException {

		deleteProjectInternal(projectName);

		for (Map.Entry<String, List<String>> entry : branchPagePaths.entrySet()) {
			String branch = entry.getKey();
			for (String pagePath : entry.getValue()) {
				submitAddPageTask(newProjectName, branch, pagePath);
			}
		}

	}

	private void addPageAsync(String projectName, String branchName, String path) throws IOException {
		String fullPath = projectName + "/" + branchName + "/" + Util.toUrlPagePath(path); //$NON-NLS-1$ //$NON-NLS-2$
		log.info("indexing page {}", fullPath); //$NON-NLS-1$

		Page page = pageStore.getPage(projectName, branchName, path, true);
		String text = ((PageTextData) page.getData()).getText();
		Authentication authentication = authenticationFactory.create(UserStore.ANONYMOUS_USER_LOGIN_NAME);
		text = markdownProcessor.markdownToHtml(text, projectName, branchName, path, authentication, null, false, null);
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
		for (String field : new String[] { ALL_TEXT, ALL_TEXT_SUGGESTIONS }) {
			doc.add(new TextField(field, projectName, Store.NO));
			doc.add(new TextField(field, branchName, Store.NO));
			doc.add(new TextField(field, page.getTitle(), Store.NO));
			doc.add(new TextField(field, text, Store.NO));
			for (String tag : page.getTags()) {
				doc.add(new TextField(field, tag, Store.NO));
			}
		}

		writer.updateDocument(new Term(FULL_PATH, fullPath), doc);
		dirty.set(true);
	}

	private String removeHtmlTags(String html) {
		for (Replacement replacement : REMOVE_HTML_TAGS) {
			html = replacement.replaceAll(html);
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
					log.error(StringUtils.EMPTY, e);
				}
			}
		};
		Future<?> future = taskExecutor.submit(runnable);
		try {
			future.get();
		} catch (InterruptedException e) {
			// ignore
		} catch (ExecutionException e) {
			log.warn(StringUtils.EMPTY, e.getCause());
		}
	}

	private void deletePagesInternal(String projectName, String branchName, Set<String> paths) throws IOException {
		boolean dirty = false;
		try {
			for (String path : paths) {
				String fullPath = projectName + "/" + branchName + "/" + Util.toUrlPagePath(path); //$NON-NLS-1$ //$NON-NLS-2$
				log.info("deleting page {}", fullPath); //$NON-NLS-1$
				writer.deleteDocuments(new Term(FULL_PATH, fullPath));
				dirty = true;
			}
		} finally {
			if (dirty) {
				this.dirty.set(true);
			}
		}
	}

	@Subscribe
	public void deleteProject(ProjectDeletedEvent event) {
		String projectName = event.getProjectName();
		submitDeleteProjectTask(projectName);
	}

	private void submitDeleteProjectTask(final String projectName) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					deleteProjectInternal(projectName);
				} catch (IOException e) {
					log.error(StringUtils.EMPTY, e);
				} catch (RuntimeException e) {
					log.error(StringUtils.EMPTY, e);
				}
			}
		};
		taskExecutor.submit(runnable);
	}

	private void deleteProjectInternal(String projectName) throws IOException {
		boolean dirty = false;
		try {
			log.info("deleting project {}", projectName); //$NON-NLS-1$
			writer.deleteDocuments(new Term(PROJECT, projectName));
			dirty = true;
		} finally {
			if (dirty) {
				this.dirty.set(true);
			}
		}
	}

	@Subscribe
	public void deleteProjectBranch(ProjectBranchDeletedEvent event) {
		String projectName = event.getProjectName();
		String branchName = event.getBranchName();
		submitDeleteProjectBranchTask(projectName, branchName);
	}

	private void submitDeleteProjectBranchTask(final String projectName, final String branchName) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					deleteProjectBranchInternal(projectName, branchName);
				} catch (IOException e) {
					log.error(StringUtils.EMPTY, e);
				} catch (RuntimeException e) {
					log.error(StringUtils.EMPTY, e);
				}
			}
		};
		taskExecutor.submit(runnable);
	}

	private void deleteProjectBranchInternal(String projectName, String branchName) throws IOException {
		boolean dirty = false;
		try {
			log.info("deleting branch {}/{}", projectName, branchName); //$NON-NLS-1$
			BooleanQuery bq = new BooleanQuery();
			bq.add(new TermQuery(new Term(PROJECT, projectName)), BooleanClause.Occur.MUST);
			bq.add(new TermQuery(new Term(BRANCH, branchName)), BooleanClause.Occur.MUST);
			writer.deleteDocuments(bq);
			dirty = true;
		} finally {
			if (dirty) {
				this.dirty.set(true);
			}
		}
	}

	@Subscribe
	public void renameProjectBranch(ProjectBranchRenamedEvent event) {
		String projectName = event.getProjectName();
		String branchName = event.getBranchName();
		String newBranchName = event.getNewBranchName();
		try {
			submitRenameProjectBranchTask(projectName, branchName, newBranchName);
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}

	private void submitRenameProjectBranchTask(final String projectName, final String branchName, final String newBranchName)
			throws IOException {

		final List<String> paths = pageStore.listAllPagePaths(projectName, newBranchName);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					renameProjectBranchAsync(projectName, branchName, newBranchName, paths);
				} catch (IOException e) {
					log.error(StringUtils.EMPTY, e);
				} catch (RuntimeException e) {
					log.error(StringUtils.EMPTY, e);
				}
			}
		};
		taskExecutor.submit(runnable);
	}

	private void renameProjectBranchAsync(String projectName, String branchName, String newBranchName, List<String> newPagePaths)
			throws IOException {

		deleteProjectBranchInternal(projectName, branchName);

		for (String pagePath : newPagePaths) {
			submitAddPageTask(projectName, newBranchName, pagePath);
		}
	}

	public SearchResult findPages(String searchText, int page, Authentication authentication)
			throws ParseException, IOException, TimeoutException {

		Stopwatch stopwatch = new Stopwatch().start();
		PageFinder pageFinder = new PageFinder(searcherManager, analyzer, taskExecutor, userStore, permissionEvaluator);
		SearchResult result = pageFinder.findPages(searchText, page, authentication);
		log.trace("finding pages took {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)); //$NON-NLS-1$
		return result;
	}

	public Set<String> getAllTags(Authentication authentication) throws IOException, TimeoutException {
		Stopwatch stopwatch = new Stopwatch().start();
		TagFinder tagFinder = new TagFinder(searcherManager, taskExecutor, userStore, permissionEvaluator);
		Set<String> tags = tagFinder.getAllTags(authentication);
		log.trace("getting all tags took {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)); //$NON-NLS-1$
		return tags;
	}

	@Scheduled(fixedDelay=REFRESH_INTERVAL * 1000)
	void refresh() {
		try {
			readerManager.maybeRefresh();
		} catch (IOException e) {
			log.warn(StringUtils.EMPTY, e);
		}

		try {
			searcherManager.maybeRefresh();
		} catch (IOException e) {
			log.warn(StringUtils.EMPTY, e);
		}
	}

	@Scheduled(fixedDelay=REFRESH_INTERVAL * 1000)
	void commit() {
		if (dirty.getAndSet(false)) {
			try {
				writer.commit();
			} catch (IOException e) {
				log.error(StringUtils.EMPTY, e);
			}
		}
	}

	int getNumDocuments() throws IOException {
		DirectoryReader reader = null;
		try {
			reader = readerManager.acquire();
			return reader.numDocs();
		} finally {
			readerManager.release(reader);
		}
	}
}
