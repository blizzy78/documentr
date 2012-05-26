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
package de.blizzy.documentr.web;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.pagestore.IPageStore;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageUtil;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.web.markdown.IPageRenderer;

@Component
public final class Functions {
	private static IPageStore pageStore;
	private static GlobalRepositoryManager repoManager;
	private static UserStore userStore;
	private static IPageRenderer pageRenderer;
	
	@Autowired
	private GlobalRepositoryManager _repoManager;
	@Autowired
	private IPageStore _pageStore;
	@Autowired
	private UserStore _userStore;
	@Autowired
	private IPageRenderer _pageRenderer;
	
	@PostConstruct
	public void init() {
		pageStore = _pageStore;
		repoManager = _repoManager;
		userStore = _userStore;
		pageRenderer = _pageRenderer;
	}

	public static List<String> listProjects() {
		return repoManager.listProjects();
	}
	
	public static List<String> listProjectBranches(String projectName) throws IOException {
		return repoManager.listProjectBranches(projectName);
	}

	public static List<String> listPageAttachments(String projectName, String branchName, String path)
			throws IOException {
		
		return pageStore.listPageAttachments(projectName, branchName, path);
	}
	
	public static String getPageTitle(String projectName, String branchName, String path) throws IOException {
		Page page = pageStore.getPage(projectName, branchName, path, false);
		return page.getTitle();
	}
	
	public static List<String> getBranchesPageIsSharedWith(String projectName, String branchName, String path)
			throws IOException {
		
		return pageStore.getBranchesPageIsSharedWith(projectName, branchName, path);
	}
	
	public static List<String> listUsers() throws IOException {
		return userStore.listUsers();
	}
	
	public static String getPageHTML(String projectName, String branchName, String path) throws IOException {
		return pageRenderer.getHTML(projectName, branchName, path);
	}

	public static List<String> getPagePathHierarchy(String projectName, String branchName, String pagePath)
			throws IOException {
		
		return PageUtil.getPagePathHierarchy(projectName, branchName, pagePath, pageStore);
	}
}
