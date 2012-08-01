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
package de.blizzy.documentr.page;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.gitective.core.BlobUtils;
import org.gitective.core.CommitFinder;
import org.gitective.core.CommitUtils;
import org.gitective.core.PathFilterUtils;
import org.gitective.core.filter.commit.AndCommitFilter;
import org.gitective.core.filter.commit.CommitFilter;
import org.gitective.core.filter.commit.CommitLimitFilter;
import org.gitective.core.filter.commit.CommitListFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryUtil;
import de.blizzy.documentr.search.PageIndex;

@Component
class PageStore implements IPageStore {
	private static final String PARENT_PAGE_PATH = "parentPagePath"; //$NON-NLS-1$
	private static final String TITLE = "title"; //$NON-NLS-1$
	private static final String CONTENT_TYPE = "contentType"; //$NON-NLS-1$
	private static final String PAGE_DATA = "pageData"; //$NON-NLS-1$
	private static final String META_SUFFIX = ".meta"; //$NON-NLS-1$
	private static final String PAGE_SUFFIX = ".page"; //$NON-NLS-1$
	private static final String PAGES_DIR_NAME = "pages"; //$NON-NLS-1$
	private static final String ATTACHMENTS_DIR_NAME = "attachments"; //$NON-NLS-1$
	private static final String VERSION_LATEST = "latest"; //$NON-NLS-1$
	private static final String VERSION_PREVIOUS = "previous"; //$NON-NLS-1$
	private static final String VIEW_RESTRICTION_ROLE = "viewRestrictionRole"; //$NON-NLS-1$
	
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private PageIndex pageIndex;

	@PostConstruct
	public void init() throws IOException {
		if (pageIndex.getNumDocuments() == 0) {
			reindexAllPages();
		}
	}

	private void reindexAllPages() throws IOException {
		for (String projectName : repoManager.listProjects()) {
			reindexAllPages(projectName);
		}
	}

	@Override
	public void reindexAllPages(String projectName) throws IOException {
		for (String branchName : repoManager.listProjectBranches(projectName)) {
			reindexAllPages(projectName, branchName);
		}
	}

	@Override
	public void reindexAllPages(String projectName, String branchName) throws IOException {
		reindexPageAndChildPages(projectName, branchName, DocumentrConstants.HOME_PAGE_NAME);
	}
	
	private void reindexPageAndChildPages(String projectName, String branchName, String path) throws IOException {
		Page page = getPage(projectName, branchName, path, true);
		pageIndex.addPage(projectName, branchName, path, page);
		
		for (String childPagePath : listChildPagePaths(projectName, branchName, path)) {
			reindexPageAndChildPages(projectName, branchName, childPagePath);
		}
	}

	@Override
	public void savePage(String projectName, String branchName, String path, Page page, String baseCommit,
			User user) throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		Assert.notNull(user);
		
		try {
			savePageInternal(projectName, branchName, path, PAGE_SUFFIX, page, baseCommit, PAGES_DIR_NAME, user);
			pageIndex.addPage(projectName, branchName, path, page);
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void saveAttachment(String projectName, String branchName, String pagePath, String name,
			Page attachment, User user) throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(pagePath);
		Assert.hasLength(name);
		Assert.notNull(attachment);
		Assert.notNull(user);
		// check if page exists by trying to load it
		getPage(projectName, branchName, pagePath, false);
		
		try {
			savePageInternal(projectName, branchName, pagePath + "/" + name, PAGE_SUFFIX, attachment, //$NON-NLS-1$
					null, ATTACHMENTS_DIR_NAME, user);
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}

	private void savePageInternal(String projectName, String branchName, String path, String suffix, Page page,
			String baseCommit, String rootDir, User user) throws IOException, GitAPIException {

		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			Git git = Git.wrap(repo.r());

			if (baseCommit != null) {
				RevCommit headCommit = CommitUtils.getHead(repo.r());
				if (headCommit.getName().equals(baseCommit)) {
					baseCommit = null;
				}
			}
			
			String editBranchName = "_edit_" + String.valueOf((long) (Math.random() * Long.MAX_VALUE)); //$NON-NLS-1$
			if (baseCommit != null) {
				git.branchCreate()
					.setName(editBranchName)
					.setStartPoint(baseCommit)
					.call();

				git.checkout()
					.setName(editBranchName)
					.call();
			}
			
			Map<String, Object> metaMap = new HashMap<String, Object>();
			metaMap.put(TITLE, page.getTitle());
			metaMap.put(CONTENT_TYPE, page.getContentType());
			metaMap.put(VIEW_RESTRICTION_ROLE, page.getViewRestrictionRole());
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(metaMap);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File pagesDir = new File(workingDir, rootDir);
			File workingFile = toFile(pagesDir, path + META_SUFFIX);
			FileUtils.write(workingFile, json, DocumentrConstants.ENCODING);

			PageData pageData = page.getData();
			if (pageData != null) {
				workingFile = toFile(pagesDir, path + suffix);
				FileUtils.writeByteArrayToFile(workingFile, pageData.getData());
			}
			
			AddCommand addCommand = git.add()
				.addFilepattern(rootDir + "/" + path + META_SUFFIX); //$NON-NLS-1$
			if (pageData != null) {
				addCommand.addFilepattern(rootDir + "/" + path + suffix); //$NON-NLS-1$
			}
			addCommand.call();

			PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(rootDir + "/" + path + suffix).call(); //$NON-NLS-1$

			if (baseCommit != null) {
				git.rebase()
					.setUpstream(branchName)
					.call();
				
				git.checkout()
					.setName(branchName)
					.call();
				
				git.merge()
					.include(repo.r().resolve(editBranchName))
					.call();
				
				git.branchDelete()
					.setBranchNames(editBranchName)
					.setForce(true)
					.call();
			}
			
			git.push().call();
			
			page.setParentPagePath(getParentPagePath(path, repo.r()));
			
			PageUtil.updateProjectEditTime(projectName);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
	
	private File toFile(File baseDir, String path) {
		File result = baseDir;
		for (String part : path.split("/")) { //$NON-NLS-1$
			result = new File(result, part);
		}
		return result;
	}
	
	@Override
	public Page getPage(String projectName, String branchName, String path, boolean loadData) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);

		try {
			Map<String, Object> pageMap = getPageData(projectName, branchName, path, PAGES_DIR_NAME, loadData);
			String parentPagePath = (String) pageMap.get(PARENT_PAGE_PATH);
			String title = (String) pageMap.get(TITLE);
			String contentType = (String) pageMap.get(CONTENT_TYPE);
			String viewRestrictionRole = (String) pageMap.get(VIEW_RESTRICTION_ROLE);
			PageData pageData = (PageData) pageMap.get(PAGE_DATA);
			Page page = new Page(title, contentType, pageData);
			page.setParentPagePath(parentPagePath);
			page.setViewRestrictionRole(viewRestrictionRole);
			return page;
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}

	private Map<String, Object> getPageData(String projectName, String branchName, String path, String rootDir,
		boolean loadData) throws IOException, GitAPIException {
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File pagesDir = new File(workingDir, rootDir);
			File workingFile = toFile(pagesDir, path + META_SUFFIX);
			if (!workingFile.isFile()) {
				throw new PageNotFoundException(projectName, branchName, path);
			}
			
			String json = FileUtils.readFileToString(workingFile, DocumentrConstants.ENCODING);
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			Map<String, Object> pageMap = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
			
			if (loadData) {
				workingFile = toFile(pagesDir, path + PAGE_SUFFIX);
				byte[] data = FileUtils.readFileToByteArray(workingFile);
				String contentType = (String) pageMap.get(CONTENT_TYPE);
				PageData pageData;
				if (contentType.equals(PageTextData.CONTENT_TYPE)) {
					pageData = PageTextData.fromBytes(data);
				} else {
					pageData = new PageData(data, contentType);
				}
				pageMap.put(PAGE_DATA, pageData);
			}
			
			String parentPagePath = getParentPagePath(path, repo.r());
			if (parentPagePath != null) {
				pageMap.put(PARENT_PAGE_PATH, parentPagePath);
			}
			
			return pageMap;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
	
	private String getParentPagePath(String path, Repository repo) {
		File workingDir = RepositoryUtil.getWorkingDir(repo);
		File pagesDir = new File(workingDir, PAGES_DIR_NAME);
		File pageFile = toFile(pagesDir, path + PAGE_SUFFIX);
		File dir = pageFile.getParentFile();
		StringBuilder buf = new StringBuilder();
		while (!dir.equals(pagesDir)) {
			if (buf.length() > 0) {
				buf.insert(0, '/');
			}
			buf.insert(0, dir.getName());
			dir = dir.getParentFile();
		}
		return (buf.length() > 0) ? buf.toString() : null;
	}

	@Override
	public Page getAttachment(String projectName, String branchName, String pagePath, String name)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(pagePath);
		Assert.hasLength(name);

		try {
			Map<String, Object> pageMap = getPageData(projectName, branchName, pagePath + "/" + name, //$NON-NLS-1$
					ATTACHMENTS_DIR_NAME, true);
			String parentPagePath = (String) pageMap.get(PARENT_PAGE_PATH);
			String contentType = (String) pageMap.get(CONTENT_TYPE);
			PageData pageData = (PageData) pageMap.get(PAGE_DATA);
			Page page = new Page(null, contentType, pageData);
			page.setParentPagePath(parentPagePath);
			return page;
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public List<String> listPageAttachments(String projectName, String branchName, String pagePath)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(pagePath);
		// check if page exists by trying to load it
		getPage(projectName, branchName, pagePath, false);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File attachmentsDir = new File(workingDir, ATTACHMENTS_DIR_NAME);
			File pageAttachmentsDir = toFile(attachmentsDir, pagePath);
			List<String> names = Collections.emptyList();
			if (pageAttachmentsDir.isDirectory()) {
				FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(File file) {
						return file.isFile() && file.getName().endsWith(META_SUFFIX);
					}
				};
				List<File> files = Lists.newArrayList(pageAttachmentsDir.listFiles(filter));
				Function<File, String> function = new Function<File, String>() {
					@Override
					public String apply(File file) {
						return StringUtils.substringBeforeLast(file.getName(), META_SUFFIX);
					}
				};
				names = Lists.newArrayList(Lists.transform(files, function));
				Collections.sort(names);
			}
			return names;
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
	
	private List<String> listPagePaths(File pagesDir, boolean recursive) {
		List<String> paths = listPagePathsInDir(pagesDir, recursive);
		String prefix = pagesDir.getAbsolutePath() + File.separator;
		final int prefixLen = prefix.length();
		final int pageSuffixLen = PAGE_SUFFIX.length();
		Function<String, String> function = new Function<String, String>() {
			@Override
			public String apply(String path) {
				path = path.substring(prefixLen, path.length() - pageSuffixLen);
				path = path.replace('\\', '/');
				return path;
			}
		};
		paths = Lists.newArrayList(Lists.transform(paths, function));
		Collections.sort(paths);
		return paths;
	}

	private List<String> listPagePathsInDir(File dir, boolean recursive) {
		List<String> result = Lists.newArrayList();
		if (dir.isDirectory()) {
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return (pathname.isFile() && pathname.getName().endsWith(PAGE_SUFFIX)) ||
							pathname.isDirectory();
				}
			};
			File[] files = dir.listFiles(filter);
			for (File file : files) {
				if (file.isDirectory()) {
					if (recursive) {
						result.addAll(listPagePathsInDir(file, true));
					}
				} else {
					result.add(file.getAbsolutePath());
				}
			}
		}
		return result;
	}
	
	@Override
	public boolean isPageSharedWithOtherBranches(String projectName, String branchName, String path) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		
		List<String> branches = getBranchesPageIsSharedWith(projectName, branchName, path);
		return branches.size() >= 2;
	}

	@Override
	public List<String> getBranchesPageIsSharedWith(String projectName, String branchName, String path)
			throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);

		List<String> allBranches = repoManager.listProjectBranches(projectName);
		ILockedRepository centralRepo = null;
		Set<String> branchesWithCommit = Collections.emptySet();
		try {
			centralRepo = repoManager.getProjectCentralRepository(projectName);
			String repoPath = PAGES_DIR_NAME + "/" + path + PAGE_SUFFIX; //$NON-NLS-1$
			RevCommit commit = CommitUtils.getLastCommit(centralRepo.r(), branchName, repoPath);
			if (commit != null) {
				// get all branches where this commit is in their history
				branchesWithCommit = getBranchesWithCommit(commit, allBranches, centralRepo.r());
				if (branchesWithCommit.size() >= 2) {
					// remove all branches where the previous commit is no longer visible
					// due to newer commits on those branches
					for (Iterator<String> iter = branchesWithCommit.iterator(); iter.hasNext();) {
						String branch = iter.next();
						RevCommit c = CommitUtils.getLastCommit(centralRepo.r(), branch, repoPath);
						if (!c.equals(commit)) {
							iter.remove();
						}
					}
				}
			}
		} finally {
			RepositoryUtil.closeQuietly(centralRepo);
		}
		
		List<String> branches = Lists.newArrayList(branchesWithCommit);
		if (!branches.contains(branchName)) {
			branches.add(branchName);
		}
		Collections.sort(branches);
		return branches;
	}
	
	private Set<String> getBranchesWithCommit(final RevCommit commit, List<String> allBranches, Repository centralRepo) {
		final Set<String> result = Sets.newHashSet();
		for (final String branch : allBranches) {
			CommitFilter matcher = new CommitFilter() {
				@Override
				public boolean include(RevWalk revWalk, RevCommit revCommit) {
					if (revCommit.equals(commit)) {
						result.add(branch);
						throw StopWalkException.INSTANCE;
					}
					return true;
				}
			};
			CommitFinder finder = new CommitFinder(centralRepo);
			finder.setMatcher(matcher);
			finder.findFrom(branch);
		}
		return result;
	}
	
	@Override
	public List<String> listChildPagePaths(String projectName, String branchName, final String path) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);

		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File pagesDir = toFile(new File(workingDir, PAGES_DIR_NAME), path);
			List<String> paths = Lists.newArrayList(listPagePaths(pagesDir, false));
			Function<String, String> function = new Function<String, String>() {
				@Override
				public String apply(String childName) {
					return path + "/" + childName; //$NON-NLS-1$
				}
			};
			paths = Lists.transform(paths, function);
			return paths;
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	@Override
	public void deletePage(String projectName, String branchName, final String path, User user) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		Assert.notNull(user);
		
		ILockedRepository repo = null;
		List<String> oldPagePaths;
		boolean deleted = false;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			
			File pagesDir = new File(workingDir, PAGES_DIR_NAME);

			File oldSubPagesDir = toFile(pagesDir, path);
			oldPagePaths = listPagePaths(oldSubPagesDir, true);
			oldPagePaths = Lists.newArrayList(Lists.transform(oldPagePaths, new Function<String, String>() {
				@Override
				public String apply(String p) {
					return path + "/" + p; //$NON-NLS-1$
				}
			}));
			oldPagePaths.add(path);

			File file = toFile(pagesDir, path + PAGE_SUFFIX);
			if (file.isFile()) {
				FileUtils.forceDelete(file);
				deleted = true;
			}
			file = toFile(pagesDir, path + META_SUFFIX);
			if (file.isFile()) {
				FileUtils.forceDelete(file);
				deleted = true;
			}
			file = toFile(pagesDir, path);
			if (file.isDirectory()) {
				FileUtils.forceDelete(file);
				deleted = true;
			}
			
			File attachmentsDir = new File(workingDir, ATTACHMENTS_DIR_NAME);
			file = toFile(attachmentsDir, path);
			if (file.isDirectory()) {
				FileUtils.forceDelete(file);
				deleted = true;
			}
			
			if (deleted) {
				Git git = Git.wrap(repo.r());
				PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
				git.commit()
					.setAuthor(ident)
					.setCommitter(ident)
					.setMessage("delete " + path) //$NON-NLS-1$
					.call();
				git.push().call();

				PageUtil.updateProjectEditTime(projectName);
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}

		if (deleted) {
			pageIndex.deletePages(projectName, branchName, Sets.newHashSet(oldPagePaths));
		}
	}
	
	@Override
	public PageMetadata getPageMetadata(String projectName, String branchName, String path) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		
		return getPageMetadataInternal(projectName, branchName, path, PAGES_DIR_NAME);
	}

	@Override
	public PageMetadata getAttachmentMetadata(String projectName, String branchName, String path, String name)
			throws IOException {

		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		Assert.hasLength(name);
		
		return getPageMetadataInternal(projectName, branchName, path + "/" + name, ATTACHMENTS_DIR_NAME); //$NON-NLS-1$
	}
	
	private PageMetadata getPageMetadataInternal(String projectName, String branchName, String path, String rootDir)
			throws IOException {
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);

			// FIXME: would love to use author details instead of committer, but JGit doesn't have getAuthoredTime()
			
			RevCommit metaCommit = CommitUtils.getLastCommit(repo.r(), rootDir + "/" + path + META_SUFFIX); //$NON-NLS-1$
			RevCommit pageCommit = CommitUtils.getLastCommit(repo.r(), rootDir + "/" + path + PAGE_SUFFIX); //$NON-NLS-1$
			RevCommit commit = getNewestCommit(metaCommit, pageCommit);
			
			PersonIdent committer = commit.getCommitterIdent();
			String lastEditedBy = null;
			if (committer != null) {
				lastEditedBy = committer.getName();
			}
			Date lastEdited = new Date(commit.getCommitTime() * 1000L);

			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File rootDirFile = new File(workingDir, rootDir);
			File file = toFile(rootDirFile, path + PAGE_SUFFIX);
			
			return new PageMetadata(lastEditedBy, lastEdited, file.length(), commit.getName());
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
	
	private RevCommit getNewestCommit(RevCommit... commits) {
		RevCommit newestCommit = null;
		int newestCommitTime = Integer.MIN_VALUE;
		for (RevCommit commit : commits) {
			if (commit != null) {
				int time = commit.getCommitTime();
				if (time > newestCommitTime) {
					newestCommit = commit;
				}
			}
		}
		return newestCommit;
	}
	
	@Override
	public void relocatePage(String projectName, String branchName, final String path, String newParentPagePath,
			User user) throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		Assert.hasLength(newParentPagePath);
		Assert.notNull(user);
		// check if pages exist by trying to load them
		getPage(projectName, branchName, path, false);
		getPage(projectName, branchName, newParentPagePath, false);
		
		ILockedRepository repo = null;
		List<String> oldPagePaths;
		List<String> deletedPagePaths;
		List<String> newPagePaths;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			String pageName = path.contains("/") ? StringUtils.substringAfterLast(path, "/") : path; //$NON-NLS-1$ //$NON-NLS-2$
			final String newPagePath = newParentPagePath + "/" + pageName; //$NON-NLS-1$

			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			
			File oldSubPagesDir = toFile(new File(workingDir, PAGES_DIR_NAME), path);
			oldPagePaths = listPagePaths(oldSubPagesDir, true);
			oldPagePaths = Lists.newArrayList(Lists.transform(oldPagePaths, new Function<String, String>() {
				@Override
				public String apply(String p) {
					return path + "/" + p; //$NON-NLS-1$
				}
			}));
			oldPagePaths.add(path);

			File deletedPagesSubDir = toFile(new File(workingDir, PAGES_DIR_NAME), newPagePath);
			deletedPagePaths = listPagePaths(deletedPagesSubDir, true);
			deletedPagePaths = Lists.newArrayList(Lists.transform(deletedPagePaths, new Function<String, String>() {
				@Override
				public String apply(String p) {
					return newPagePath + "/" + p; //$NON-NLS-1$
				}
			}));
			deletedPagePaths.add(newPagePath);

			Git git = Git.wrap(repo.r());
			AddCommand addCommand = git.add();

			for (String dirName : Sets.newHashSet(PAGES_DIR_NAME, ATTACHMENTS_DIR_NAME)) {
				File dir = new File(workingDir, dirName);
				
				File newSubPagesDir = toFile(dir, newPagePath);
				if (newSubPagesDir.exists()) {
					git.rm().addFilepattern(dirName + "/" + newPagePath).call(); //$NON-NLS-1$
				}
				File newPageFile = toFile(dir, newPagePath + PAGE_SUFFIX);
				if (newPageFile.exists()) {
					git.rm().addFilepattern(dirName + "/" + newPagePath + PAGE_SUFFIX).call(); //$NON-NLS-1$
				}
				File newMetaFile = toFile(dir, newPagePath + META_SUFFIX);
				if (newMetaFile.exists()) {
					git.rm().addFilepattern(dirName + "/" + newPagePath + META_SUFFIX).call(); //$NON-NLS-1$
				}
				
				File newParentPageDir = toFile(dir, newParentPagePath);
				File subPagesDir = toFile(dir, path);
				if (subPagesDir.exists()) {
					FileUtils.copyDirectoryToDirectory(subPagesDir, newParentPageDir);
					git.rm().addFilepattern(dirName + "/" + path).call(); //$NON-NLS-1$
					addCommand.addFilepattern(dirName + "/" + newParentPagePath + "/" + pageName); //$NON-NLS-1$ //$NON-NLS-2$
				}
				File pageFile = toFile(dir, path + PAGE_SUFFIX);
				if (pageFile.exists()) {
					FileUtils.copyFileToDirectory(pageFile, newParentPageDir);
					git.rm().addFilepattern(dirName + "/" + path + PAGE_SUFFIX).call(); //$NON-NLS-1$
					addCommand.addFilepattern(dirName + "/" + newParentPagePath + "/" + pageName + PAGE_SUFFIX); //$NON-NLS-1$ //$NON-NLS-2$
				}
				File metaFile = toFile(dir, path + META_SUFFIX);
				if (metaFile.exists()) {
					FileUtils.copyFileToDirectory(metaFile, newParentPageDir);
					git.rm().addFilepattern(dirName + "/" + path + META_SUFFIX).call(); //$NON-NLS-1$
					addCommand.addFilepattern(dirName + "/" + newParentPagePath + "/" + pageName + META_SUFFIX); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			addCommand.call();
			PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage("move " + path + " to " + newParentPagePath + "/" + pageName) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.call();
			git.push().call();

			newPagePaths = Lists.transform(oldPagePaths, new Function<String, String>() {
				@Override
				public String apply(String p) {
					return newPagePath + StringUtils.removeStart(p, path);
				}
			});

			PageUtil.updateProjectEditTime(projectName);
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}

		Set<String> allDeletedPagePaths = Sets.newHashSet(oldPagePaths);
		allDeletedPagePaths.addAll(deletedPagePaths);
		pageIndex.deletePages(projectName, branchName, Sets.newHashSet(allDeletedPagePaths));
		
		for (String newPath : newPagePaths) {
			Page page = getPage(projectName, branchName, newPath, true);
			pageIndex.addPage(projectName, branchName, newPath, page);
		}
	}
	
	@Override
	public Map<String, String> getMarkdown(String projectName, String branchName, String path, Set<String> versions)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		// check if page exists by trying to load it
		getPage(projectName, branchName, path, false);
		Assert.notEmpty(versions);

		Map<String, String> result = Maps.newHashMap();
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			String filePath = PAGES_DIR_NAME + "/" + path + PAGE_SUFFIX; //$NON-NLS-1$
			
			Set<String> realVersions = Sets.newHashSet();
			for (String version : versions) {
				if (version.equals(VERSION_LATEST)) {
					RevCommit latestCommit = CommitUtils.getLastCommit(repo.r(), filePath);
					String commitId = latestCommit.getName();
					result.put(VERSION_LATEST, commitId);
					realVersions.add(commitId);
				} else if (version.equals(VERSION_PREVIOUS)) {
					RevCommit latestCommit = CommitUtils.getLastCommit(repo.r(), filePath);
					if (latestCommit.getParentCount() > 0) {
						RevCommit parentCommit = latestCommit.getParent(0);
						RevCommit previousCommit = CommitUtils.getLastCommit(repo.r(), parentCommit.getName(), filePath);
						if (previousCommit != null) {
							String commitId = previousCommit.getName();
							result.put(VERSION_PREVIOUS, commitId);
							realVersions.add(commitId);
						}
					}
				} else {
					realVersions.add(version);
				}
			}
			
			for (String version : realVersions) {
				String markdown = BlobUtils.getContent(repo.r(), version, filePath);
				if (markdown != null) {
					result.put(version, markdown);
				}
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
		return result;
	}

	@Override
	public List<PageVersion> listPageVersions(String projectName, String branchName, String path) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		// check if page exists by trying to load it
		getPage(projectName, branchName, path, false);
		
		ILockedRepository repo = null;
		List<PageVersion> result = Lists.newArrayList();
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);

			CommitFinder finder = new CommitFinder(repo.r());
			TreeFilter pathFilter = PathFilterUtils.or(PAGES_DIR_NAME + "/" + path + PAGE_SUFFIX); //$NON-NLS-1$
			finder.setFilter(pathFilter);
			CommitListFilter commits = new CommitListFilter();
			finder.setMatcher(new AndCommitFilter(new CommitLimitFilter(50), commits));
			finder.find();
			
			for (RevCommit commit : commits.getCommits()) {
				result.add(toPageVersion(commit));
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
		return result;
	}
	
	private PageVersion toPageVersion(RevCommit commit) {
		PersonIdent committer = commit.getCommitterIdent();
		String lastEditedBy = null;
		if (committer != null) {
			lastEditedBy = committer.getName();
		}
		Date lastEdited = new Date(commit.getCommitTime() * 1000L);
		String commitName = commit.getName();
		return new PageVersion(commitName, lastEditedBy, lastEdited);
	}

	@Override
	public void deleteAttachment(String projectName, String branchName, String pagePath, String name, User user)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(pagePath);
		Assert.hasLength(name);
		Assert.notNull(user);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			
			File attachmentsDir = new File(workingDir, ATTACHMENTS_DIR_NAME);
			boolean deleted = false;
			File file = toFile(attachmentsDir, pagePath + "/" + name + PAGE_SUFFIX); //$NON-NLS-1$
			if (file.isFile()) {
				FileUtils.forceDelete(file);
				deleted = true;
			}
			file = toFile(attachmentsDir, pagePath + "/" + name + META_SUFFIX); //$NON-NLS-1$
			if (file.isFile()) {
				FileUtils.forceDelete(file);
				deleted = true;
			}
			
			if (deleted) {
				Git git = Git.wrap(repo.r());
				PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
				git.commit()
					.setAuthor(ident)
					.setCommitter(ident)
					.setMessage("delete " + pagePath + "/" + name) //$NON-NLS-1$ //$NON-NLS-2$
					.call();
				git.push().call();

				PageUtil.updateProjectEditTime(projectName);
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
	
	@Override
	public void restorePageVersion(String projectName, String branchName, String path, String version, User user)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		Assert.hasLength(version);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			String text = BlobUtils.getContent(repo.r(), version, PAGES_DIR_NAME + "/" + path + PAGE_SUFFIX); //$NON-NLS-1$
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File pagesDir = new File(workingDir, PAGES_DIR_NAME);
			File file = toFile(pagesDir, path + PAGE_SUFFIX);
			FileUtils.writeStringToFile(file, text, DocumentrConstants.ENCODING);
			
			Git git = Git.wrap(repo.r());
			git.add()
				.addFilepattern(PAGES_DIR_NAME + "/" + path + PAGE_SUFFIX) //$NON-NLS-1$
				.call();
			PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(PAGES_DIR_NAME + "/" + path) //$NON-NLS-1$
				.call();
			git.push().call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}

		Page page = getPage(projectName, branchName, path, true);
		pageIndex.addPage(projectName, branchName, path, page);
	}
	
	@Override
	public String getViewRestrictionRole(String projectName, String branchName, String path) throws IOException {
		return getPage(projectName, branchName, path, false).getViewRestrictionRole();
	}

	void setGlobalRepositoryManager(GlobalRepositoryManager repoManager) {
		this.repoManager = repoManager;
	}

	void setPageIndex(PageIndex pageIndex) {
		this.pageIndex = pageIndex;
	}
}
