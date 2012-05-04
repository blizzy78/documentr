package de.blizzy.documentr.web;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.Page;
import de.blizzy.documentr.repository.PageStore;

@Component
public final class Functions {
	private static final int PEGDOWN_OPTIONS = Extensions.ALL -
			Extensions.QUOTES - Extensions.SMARTS - Extensions.SMARTYPANTS;

	private static PageStore pageStore;
	private static GlobalRepositoryManager repoManager;
	
	@Autowired
	private GlobalRepositoryManager _repoManager;
	@Autowired
	private PageStore _pageStore;
	
	@PostConstruct
	public void init() {
		pageStore = _pageStore;
		repoManager = _repoManager;
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
	
	public static boolean isPageSharedWithOtherBranches(String projectName, String branchName, String path) {
		try {
			return pageStore.isPageSharedWithOtherBranches(projectName, branchName, path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String markdownToHTML(String markdown) {
		PegDownProcessor proc = new PegDownProcessor(PEGDOWN_OPTIONS);
		RootNode rootNode = proc.parseMarkdown(markdown.toCharArray());
		return new HtmlSerializer().toHtml(rootNode);
	}
}
