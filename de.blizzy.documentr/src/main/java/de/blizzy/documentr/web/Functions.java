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
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.inject.internal.Lists;

import de.blizzy.documentr.FileLengthFormat;
import de.blizzy.documentr.Util;
import de.blizzy.documentr.access.OpenId;
import de.blizzy.documentr.access.RoleGrantedAuthority;
import de.blizzy.documentr.access.UserNotFoundException;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageMetadata;
import de.blizzy.documentr.page.PageUtil;
import de.blizzy.documentr.page.PageVersion;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.web.markdown.IPageRenderer;
import de.blizzy.documentr.web.markdown.MarkdownProcessor;
import de.blizzy.documentr.web.markdown.macro.MacroDescriptor;
import de.blizzy.documentr.web.markdown.macro.MacroFactory;

@Component
public final class Functions {
	private static IPageStore pageStore;
	private static GlobalRepositoryManager repoManager;
	private static UserStore userStore;
	private static IPageRenderer pageRenderer;
	private static MarkdownProcessor markdownProcessor;
	private static MessageSource messageSource;
	private static  MacroFactory macroFactory;
	
	@Autowired
	private GlobalRepositoryManager _repoManager;
	@Autowired
	private IPageStore _pageStore;
	@Autowired
	private UserStore _userStore;
	@Autowired
	private IPageRenderer _pageRenderer;
	@Autowired
	private MarkdownProcessor _markdownProcessor;
	@Autowired
	private MessageSource _messageSource;
	@Autowired
	private MacroFactory _macroFactory;
	
	@PostConstruct
	public void init() {
		pageStore = _pageStore;
		repoManager = _repoManager;
		userStore = _userStore;
		pageRenderer = _pageRenderer;
		markdownProcessor = _markdownProcessor;
		messageSource = _messageSource;
		macroFactory = _macroFactory;
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
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String html = pageRenderer.getHtml(projectName, branchName, path, authentication);
		return markdownProcessor.processNonCacheableMacros(html, projectName, branchName, path, authentication);
	}

	public static String getPageHeaderHTML(String projectName, String branchName, String path) throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String html = pageRenderer.getHeaderHtml(projectName, branchName, path, authentication);
		return markdownProcessor.processNonCacheableMacros(html, projectName, branchName, path, authentication);
	}
	
	public static List<String> getPagePathHierarchy(String projectName, String branchName, String pagePath)
			throws IOException {
		
		return PageUtil.getPagePathHierarchy(projectName, branchName, pagePath, pageStore);
	}
	
	public static PageMetadata getPageMetadata(String projectName, String branchName, String path) throws IOException {
		return pageStore.getPageMetadata(projectName, branchName, path);
	}
	
	public static PageMetadata getAttachmentMetadata(String projectName, String branchName, String pagePath,
			String name) throws IOException {
		
		return pageStore.getAttachmentMetadata(projectName, branchName, pagePath, name);
	}

	public static List<String> listRoles() throws IOException {
		return userStore.listRoles();
	}

	public static List<RoleGrantedAuthority> getUserAuthorities(String loginName) throws IOException {
		try {
			return userStore.getUserAuthorities(loginName);
		} catch (UserNotFoundException e) {
			return Collections.emptyList();
		}
	}
	
	public static String formatSize(long size) {
		Locale locale = LocaleContextHolder.getLocale();
		FileLengthFormat format = new FileLengthFormat(messageSource, locale);
		return format.format(size);
	}
	
	public static List<PageVersion> listPageVersions(String projectName, String branchName, String path) throws IOException {
		return pageStore.listPageVersions(projectName, branchName, Util.toRealPagePath(path));
	}
	
	public static List<OpenId> listMyOpenIds() throws IOException {
		String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
		List<OpenId> openIds = Lists.newArrayList(userStore.getUser(loginName).getOpenIds());
		Collections.sort(openIds, new Comparator<OpenId>() {
			@Override
			public int compare(OpenId id1, OpenId id2) {
				return id1.getDelegateId().compareToIgnoreCase(id2.getDelegateId());
			}
		});
		return openIds;
	}
	
	public static List<MacroDescriptor> getMacros() {
		List<MacroDescriptor> descs = Lists.newArrayList(macroFactory.getDescriptors());
		final Locale locale = LocaleContextHolder.getLocale();
		final Collator collator = Collator.getInstance(locale);
		Collections.sort(descs, new Comparator<MacroDescriptor>() {
			@Override
			public int compare(MacroDescriptor d1, MacroDescriptor d2) {
				String titleKey1 = d1.getTitleKey();
				String titleKey2 = d2.getTitleKey();
				String title1 = messageSource.getMessage(titleKey1, null, locale);
				String title2 = messageSource.getMessage(titleKey2, null, locale);
				return collator.compare(title1, title2);
			}
		});
		return descs;
	}
	
	public static int floor(double d) {
		return (int) Math.floor(d);
	}
	
	public static String getLanguage() {
		Locale locale = LocaleContextHolder.getLocale();
		return locale.getLanguage();
	}
	
	static void setGlobalRepositoryManager(GlobalRepositoryManager repoManager) {
		Functions.repoManager = repoManager;
	}

	static void setPageStore(IPageStore pageStore) {
		Functions.pageStore = pageStore;
	}

	static void setUserStore(UserStore userStore) {
		Functions.userStore = userStore;
	}

	static void setPageRenderer(IPageRenderer pageRenderer) {
		Functions.pageRenderer = pageRenderer;
	}

	static void setMarkdownProcessor(MarkdownProcessor markdownProcessor) {
		Functions.markdownProcessor = markdownProcessor;
	}

	static void setMessageSource(MessageSource messageSource) {
		Functions.messageSource = messageSource;
	}

	static void setMacroFactory(MacroFactory macroFactory) {
		Functions.macroFactory = macroFactory;
	}
}
