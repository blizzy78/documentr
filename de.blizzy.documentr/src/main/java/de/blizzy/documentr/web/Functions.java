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
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.pagestore.PageUtil;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.web.markdown.MarkdownProcessor;
import de.blizzy.documentr.web.markdown.macro.MacroFactory;

@Component
public final class Functions {
	private static PageStore pageStore;
	private static GlobalRepositoryManager repoManager;
	private static UserStore userStore;
	private static MacroFactory macroFactory;
	
	@Autowired
	private GlobalRepositoryManager _repoManager;
	@Autowired
	private PageStore _pageStore;
	@Autowired
	private UserStore _userStore;
	@Autowired
	private MacroFactory _macroFactory;
	
	@PostConstruct
	public void init() {
		pageStore = _pageStore;
		repoManager = _repoManager;
		userStore = _userStore;
		macroFactory = _macroFactory;
	}

	public static List<String> listProjects() {
		return repoManager.listProjects();
	}
	
	public static List<String> listProjectBranches(String projectName) {
		try {
			return repoManager.listProjectBranches(projectName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<String> listPageAttachments(String projectName, String branchName, String path) {
		try {
			return pageStore.listPageAttachments(projectName, branchName, path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getPageTitle(String projectName, String branchName, String path) {
		try {
			Page page = pageStore.getPage(projectName, branchName, path);
			return page.getTitle();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<String> getBranchesPageIsSharedWith(String projectName, String branchName, String path) {
		try {
			return pageStore.getBranchesPageIsSharedWith(projectName, branchName, path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<String> listUsers() {
		try {
			return userStore.listUsers();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String markdownToHTML(String markdown, String projectName, String branchName, String path) {
		MarkdownProcessor proc = new MarkdownProcessor(projectName, branchName, path, macroFactory);
		return proc.markdownToHTML(markdown);
	}

	public static List<String> getPagePathHierarchy(String projectName, String branchName, String pagePath) {
		try {
			return PageUtil.getPagePathHierarchy(projectName, branchName, pagePath, pageStore);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
