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
package de.blizzy.documentr.pagestore;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gitective.core.CommitFinder;
import org.gitective.core.CommitUtils;
import org.gitective.core.filter.commit.CommitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryUtil;

@Component
public class PageStore {
	private static final String PARENT_PAGE_PATH = "parentPagePath"; //$NON-NLS-1$
	private static final String TITLE = "title"; //$NON-NLS-1$
	private static final String CONTENT_TYPE = "contentType"; //$NON-NLS-1$
	private static final String PAGE_DATA = "pageData"; //$NON-NLS-1$
	private static final String META_SUFFIX = ".meta"; //$NON-NLS-1$
	private static final String PAGE_SUFFIX = ".page"; //$NON-NLS-1$
	private static final String PAGES_DIR_NAME = "pages"; //$NON-NLS-1$
	private static final String ATTACHMENTS_DIR_NAME = "attachments"; //$NON-NLS-1$
	
	@Autowired
	private GlobalRepositoryManager repoManager;
	
	public void savePage(String projectName, String branchName, String path, Page page)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		
		try {
			savePageInternal(projectName, branchName, path, PAGE_SUFFIX, page, PAGES_DIR_NAME);
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}

	public void saveAttachment(String projectName, String branchName, String pagePath, String name, Page attachment)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(pagePath);
		Assert.hasLength(name);
		// check if page exists by trying to load it
		getPage(projectName, branchName, pagePath, false);
		
		try {
			savePageInternal(projectName, branchName, pagePath + "/" + name, PAGE_SUFFIX, attachment, ATTACHMENTS_DIR_NAME); //$NON-NLS-1$
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}

	private void savePageInternal(String projectName, String branchName, String path, String suffix, Page page, String rootDir)
			throws IOException, GitAPIException {
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);

			Map<String, Object> metaMap = new HashMap<String, Object>();
			if (page.getParentPagePath() != null) {
				metaMap.put(PARENT_PAGE_PATH, page.getParentPagePath());
			}
			metaMap.put(TITLE, page.getTitle());
			metaMap.put(CONTENT_TYPE, page.getContentType());
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(metaMap);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File pagesDir = new File(workingDir, rootDir);
			File workingFile = toFile(pagesDir, path + META_SUFFIX);
			FileUtils.forceMkdir(workingFile.getParentFile());
			FileUtils.write(workingFile, json, "UTF-8"); //$NON-NLS-1$

			PageData pageData = page.getData();
			if (pageData != null) {
				workingFile = toFile(pagesDir, path + suffix);
				FileUtils.writeByteArrayToFile(workingFile, pageData.getData());
			}
			
			Git git = Git.wrap(repo.r());
			AddCommand addCommand = git.add()
				.addFilepattern(rootDir + "/" + path + META_SUFFIX); //$NON-NLS-1$
			if (pageData != null) {
				addCommand.addFilepattern(rootDir + "/" + path + suffix); //$NON-NLS-1$
			}
			addCommand.call();
			git.commit().setMessage(rootDir + "/" + path + suffix).call(); //$NON-NLS-1$
			git.push().call();
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
	
	public Page getPage(String projectName, String branchName, String path, boolean loadData) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);

		try {
			Map<String, Object> pageMap = getPageData(projectName, branchName, path, PAGE_SUFFIX, PAGES_DIR_NAME, loadData);
			String parentPagePath = (String) pageMap.get(PARENT_PAGE_PATH);
			String title = (String) pageMap.get(TITLE);
			String contentType = (String) pageMap.get(CONTENT_TYPE);
			PageData pageData = (PageData) pageMap.get(PAGE_DATA);
			return new Page(parentPagePath, title, contentType, pageData);
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}

	private Map<String, Object> getPageData(String projectName, String branchName, String path, String suffix, String rootDir,
		boolean loadData) throws IOException, GitAPIException {
		
		suffix = StringUtils.defaultString(suffix);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File pagesDir = new File(workingDir, rootDir);
			File workingFile = toFile(pagesDir, path + META_SUFFIX);
			if (!workingFile.isFile()) {
				throw new PageNotFoundException(projectName, branchName, path);
			}
			
			String json = FileUtils.readFileToString(workingFile, "UTF-8"); //$NON-NLS-1$
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
			
			return pageMap;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	public Page getAttachment(String projectName, String branchName, String pagePath, String name)
			throws IOException {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(pagePath);
		Assert.hasLength(name);

		try {
			Map<String, Object> pageMap = getPageData(projectName, branchName, pagePath + "/" + name, PAGE_SUFFIX, //$NON-NLS-1$
					ATTACHMENTS_DIR_NAME, true);
			String parentPagePath = (String) pageMap.get(PARENT_PAGE_PATH);
			String contentType = (String) pageMap.get(CONTENT_TYPE);
			PageData pageData = (PageData) pageMap.get(PAGE_DATA);
			return new Page(parentPagePath, null, contentType, pageData);
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}
	
	public List<String> listPagePaths(String projectName, String branchName) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);

		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File pagesDir = new File(workingDir, PAGES_DIR_NAME);
			List<String> paths = listPagePaths(pagesDir, true);
			return paths;
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

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
				List<File> files = Arrays.asList(pageAttachmentsDir.listFiles(filter));
				Function<File, String> function = new Function<File, String>() {
					@Override
					public String apply(File file) {
						return StringUtils.substringBeforeLast(file.getName(), META_SUFFIX);
					}
				};
				names = new ArrayList<String>(Lists.transform(files, function));
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
		paths = new ArrayList<String>(Lists.transform(paths, function));
		Collections.sort(paths);
		return paths;
	}

	private List<String> listPagePathsInDir(File dir, boolean recursive) {
		List<String> result = new ArrayList<String>();
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
	
	public boolean isPageSharedWithOtherBranches(String projectName, String branchName, String path) throws IOException {
		List<String> branches = getBranchesPageIsSharedWith(projectName, branchName, path);
		return branches.size() >= 2;
	}

	public List<String> getBranchesPageIsSharedWith(String projectName, String branchName, String path) throws IOException {
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
		
		List<String> branches = new ArrayList<String>(branchesWithCommit);
		if (!branches.contains(branchName)) {
			branches.add(branchName);
		}
		Collections.sort(branches);
		return branches;
	}
	
	private Set<String> getBranchesWithCommit(final RevCommit commit, List<String> allBranches, Repository centralRepo) {
		final Set<String> result = new HashSet<String>();
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
	
	void setGlobalRepositoryManager(GlobalRepositoryManager repoManager) {
		this.repoManager = repoManager;
	}

	public List<String> listChildPagePaths(String projectName, String branchName, final String path) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);

		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File pagesDir = toFile(new File(workingDir, PAGES_DIR_NAME), path);
			List<String> paths = new ArrayList<String>(listPagePaths(pagesDir, false));
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

	public void deletePage(String projectName, String branchName, String path) throws IOException {
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		Assert.hasLength(path);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, branchName);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File file = toFile(new File(workingDir, PAGES_DIR_NAME), path + PAGE_SUFFIX);
			if (file.isFile()) {
				FileUtils.forceDelete(file);
				Git git = Git.wrap(repo.r());
				git.commit().setMessage("delete " + path).call(); //$NON-NLS-1$
				git.push().call();
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
}
