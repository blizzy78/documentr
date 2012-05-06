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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;

@Component
public final class Functions {
	private static final int PEGDOWN_OPTIONS = Extensions.ALL -
			Extensions.QUOTES - Extensions.SMARTS - Extensions.SMARTYPANTS;

	private static PageStore pageStore;
	private static GlobalRepositoryManager repoManager;
	private static UserStore userStore;
	
	@Autowired
	private GlobalRepositoryManager _repoManager;
	@Autowired
	private PageStore _pageStore;
	@Autowired
	private UserStore _userStore;
	
	@PostConstruct
	public void init() {
		pageStore = _pageStore;
		repoManager = _repoManager;
		userStore = _userStore;
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

	public static List<String> listPagePaths(String projectName, String branchName) {
		try {
			return pageStore.listPagePaths(projectName, branchName);
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
		HtmlSerializerContext context = new HtmlSerializerContext(projectName, branchName, path);
		return markdownToHTML(markdown, context);
	}

	public static String markdownToHTML(String markdown, HtmlSerializerContext context) {
		PegDownProcessor proc = new PegDownProcessor(PEGDOWN_OPTIONS);
		RootNode rootNode = proc.parseMarkdown(markdown.toCharArray());
		HtmlSerializer serializer = new HtmlSerializer(context);
		return serializer.toHtml(rootNode);
	}
	
	public static String join(Object o, String delimiter) {
		Collection<?> c;
		if (o instanceof Collection) {
			c = (Collection<?>) o;
		} else if (o.getClass().isArray()) {
			c = Arrays.asList((Object[]) o);
		} else {
			c = Collections.singleton(o);
		}
		return StringUtils.join(c, delimiter);
	}
}
