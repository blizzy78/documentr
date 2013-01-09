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
package de.blizzy.documentr.web;

import java.io.IOException;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.blizzy.documentr.access.OpenId;
import de.blizzy.documentr.access.RoleGrantedAuthority;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserNotFoundException;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.markdown.IPageRenderer;
import de.blizzy.documentr.markdown.MarkdownProcessor;
import de.blizzy.documentr.markdown.macro.IMacroDescriptor;
import de.blizzy.documentr.markdown.macro.MacroFactory;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageMetadata;
import de.blizzy.documentr.page.PageNotFoundException;
import de.blizzy.documentr.page.PageUtil;
import de.blizzy.documentr.page.PageVersion;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.subscription.SubscriptionStore;
import de.blizzy.documentr.system.SystemSettingsStore;
import de.blizzy.documentr.system.UpdateChecker;
import de.blizzy.documentr.util.FileLengthFormat;
import de.blizzy.documentr.util.Util;

@Component
public final class Functions {
	private static IPageStore pageStore;
	private static GlobalRepositoryManager repoManager;
	private static UserStore userStore;
	private static IPageRenderer pageRenderer;
	private static MarkdownProcessor markdownProcessor;
	private static MessageSource messageSource;
	private static MacroFactory macroFactory;
	private static SubscriptionStore subscriptionStore;
	private static SystemSettingsStore systemSettingsStore;
	private static UpdateChecker updateChecker;

	@Autowired
	private GlobalRepositoryManager wiredRepoManager;
	@Autowired
	private IPageStore wiredPageStore;
	@Autowired
	private UserStore wiredUserStore;
	@Autowired
	private IPageRenderer wiredPageRenderer;
	@Autowired
	private MarkdownProcessor wiredMarkdownProcessor;
	@Autowired
	private MessageSource wiredMessageSource;
	@Autowired
	private MacroFactory wiredMacroFactory;
	@Autowired
	private SubscriptionStore wiredSubscriptionStore;
	@Autowired
	private SystemSettingsStore wiredSystemSettingsStore;
	@Autowired
	private UpdateChecker wiredUpdateChecker;

	@PostConstruct
	public void init() {
		pageStore = wiredPageStore;
		repoManager = wiredRepoManager;
		userStore = wiredUserStore;
		pageRenderer = wiredPageRenderer;
		markdownProcessor = wiredMarkdownProcessor;
		messageSource = wiredMessageSource;
		macroFactory = wiredMacroFactory;
		subscriptionStore = wiredSubscriptionStore;
		systemSettingsStore = wiredSystemSettingsStore;
		updateChecker = wiredUpdateChecker;
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

	public static String getPageHtml(String projectName, String branchName, String path) throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String contextPath = request.getContextPath();
		String html = pageRenderer.getHtml(projectName, branchName, path, authentication, contextPath);
		return markdownProcessor.processNonCacheableMacros(html, projectName, branchName, path, authentication,
				contextPath);
	}

	public static String getPageHeaderHtml(String projectName, String branchName, String path) throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String contextPath = request.getContextPath();
		String html = pageRenderer.getHeaderHtml(projectName, branchName, path, authentication, contextPath);
		return markdownProcessor.processNonCacheableMacros(html, projectName, branchName, path, authentication,
				contextPath);
	}

	public static List<String> getPagePathHierarchy(String projectName, String branchName, String pagePath)
			throws IOException {

		return PageUtil.getPagePathHierarchy(projectName, branchName, Util.toRealPagePath(pagePath), pageStore);
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

	public static List<JspMacroDescriptor> getMacros() {
		List<IMacroDescriptor> descs = Lists.newArrayList(macroFactory.getDescriptors());
		final Locale locale = LocaleContextHolder.getLocale();
		final Collator collator = Collator.getInstance(locale);
		Collections.sort(descs, new Comparator<IMacroDescriptor>() {
			@Override
			public int compare(IMacroDescriptor d1, IMacroDescriptor d2) {
				String title1 = d1.getTitle(locale);
				String title2 = d2.getTitle(locale);
				return collator.compare(title1, title2);
			}
		});
		Function<IMacroDescriptor, JspMacroDescriptor> function = new Function<IMacroDescriptor, JspMacroDescriptor>() {
			@Override
			public JspMacroDescriptor apply(IMacroDescriptor descriptor) {
				return new JspMacroDescriptor(descriptor, locale);
			}
		};
		return Lists.transform(descs, function);
	}

	public static int floor(double d) {
		return (int) Math.floor(d);
	}

	public static String getLanguage() {
		Locale locale = LocaleContextHolder.getLocale();
		return locale.getLanguage();
	}

	public static String escapeJavaScript(String s) {
		return StringEscapeUtils.escapeEcmaScript(s);
	}

	public static boolean pageExists(String projectName, String branchName, String path) {
		try {
			return pageStore.getPageMetadata(projectName, branchName, path) != null;
		} catch (PageNotFoundException e) {
			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isSubscribed(String projectName, String branchName, String path) throws IOException {
		String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userStore.getUser(loginName);
		return subscriptionStore.isSubscribed(projectName, branchName, path, user);
	}

	public static String getSystemSetting(String key) {
		return StringUtils.defaultString(systemSettingsStore.getSetting(key));
	}

	public static String getLatestVersionForUpdate() {
		return updateChecker.isUpdateAvailable() ? updateChecker.getLatestVersion() : null;
	}

	public static List<String> getGroovyMacros() {
		return macroFactory.listGroovyMacros();
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
